package com.dapm.security_service.controllers.Client2Api;

import com.dapm.security_service.models.*;
import com.dapm.security_service.models.dtos.ApproveProcessingElementRequestDto;
import com.dapm.security_service.models.dtos.ConfigureValidationDto;
import com.dapm.security_service.models.dtos.ConfirmationResponse;
import com.dapm.security_service.models.dtos.MissingPermissionsDto;
import com.dapm.security_service.models.dtos2.PipelineProcessingElementRequestDto;
import com.dapm.security_service.models.dtos.peer.RequestResponse;
import com.dapm.security_service.models.dtos2.PipelineProcessingElementRequestOutboundDto;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import com.dapm.security_service.models.enums.PipelinePhase;
import com.dapm.security_service.models.models2.ValidatedPipelineConfig;
import com.dapm.security_service.repositories.*;
import com.dapm.security_service.security.CustomUserDetails;
import com.dapm.security_service.services.OrgRequestService;
import com.dapm.security_service.services.OrgResponseService;
import com.dapm.security_service.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pipeline/configuration")

public class PipelineConfigurationController {
    @Autowired
    private PipelineProcessingElementRequestRepository pipelinePeReqRepo;
    @Autowired
    private PipelineRepositoryy pipelineRepo;
    @Autowired
    private ProcessingElementRepository peRepo;
    @Autowired
    private PipelinePeInstanceRepo pipelinePeInstanceRepo;

    @Autowired
    private ProcessingElementRepository processingElementRepositry;

    @Autowired
    private PipelineRepositoryy pipelineRepository;

    @Autowired
    private OrgRequestService orgRequestService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ValidatePipelineRepository validatePipelineRepository;

    @Autowired private OrgResponseService orgResponseService;
    @Value("${dapm.defaultOrgName}")
    private String orgName;

    @GetMapping("/{pipelineName}/configuration-status")
    public ResponseEntity<ConfigureValidationDto> checkConfiguration(@PathVariable String pipelineName) {
        ConfigureValidationDto configStatus = getConfiguration(pipelineName);

        return configStatus != null
                ? ResponseEntity.ok(configStatus)
                : ResponseEntity.notFound().build();
    }

    @PreAuthorize("@pipelineAccessEvaluator.hasPermission(#requestDto.getPipelineName(), authentication, 'ACCESS_REQUEST_PE')")
    @PostMapping("/request")
    public RequestResponse initiatePeerRequest(
            @RequestBody PipelineProcessingElementRequestDto requestDto
    ,@AuthenticationPrincipal CustomUserDetails userDetails) {


        ProcessingElement processingElement = processingElementRepositry.findByTemplateId(requestDto.getProcessingElement())
                .orElseThrow(() -> new RuntimeException("Node not found: " + requestDto.getProcessingElement()));

        PipelineProcessingElementRequest request = convertDtoToEntity(requestDto,userDetails.getUser());
        String webhookUrl = "http://"+orgName+":8080/api/client/pipeline-processingElement/webhook";
        request.setWebhookUrl(webhookUrl);

        PipelineProcessingElementRequest localRequest = pipelinePeReqRepo.save(request);

        PipelineProcessingElementRequestOutboundDto outboundDto = toOutboundDto(localRequest);

        RequestResponse remoteResponseDto = orgRequestService.sendRequestToOrg(outboundDto,processingElement.getOwnerPartnerOrganization().getName());

        // Update the local record with any details returned from OrgB (e.g., approval token, updated status).
        localRequest.setApprovalToken(remoteResponseDto.getToken());
        localRequest.setStatus(remoteResponseDto.getRequestStatus());
//        localRequest.setDecisionTime(remoteResponseDto.);
        localRequest = pipelinePeReqRepo.save(localRequest);

        // Return the updated local record.
//        return remoteResponseDto;

        if (remoteResponseDto.getRequestStatus() == AccessRequestStatus.APPROVED) {
            ConfigureValidationDto getConfiguration = getConfiguration(outboundDto.getPipelineName());
            if ("VALID".equals(getConfiguration.getStatus())) {
                Pipeline pipeline = pipelineRepository.findByName(outboundDto.getPipelineName())
                        .orElseThrow(() -> new IllegalArgumentException("Pipeline not found: " + outboundDto.getPipelineName()));
                pipeline.setPipelinePhase(PipelinePhase.CONFIGURED);
                pipelineRepository.save(pipeline);
            }
            ValidatedPipelineConfig config = validatePipelineRepository.getPipeline(outboundDto.getPipelineName());
            if (config != null) {
                config.getExternalPEsTokens().put(outboundDto.getProcessingElementName(), remoteResponseDto.getToken());
            } else {
                throw new IllegalArgumentException("Pipeline not found: " + outboundDto.getPipelineName());
            }
        }


        RequestResponse response = new RequestResponse();
        response.setRequestId(localRequest.getId());
        response.setRequestStatus(localRequest.getStatus());
        response.setToken(localRequest.getApprovalToken());
        return response;
}

// get requests
    @GetMapping("/external-requests")
    public List<PipelineRequestDto> getAllExternalRequests() {
        List<PipelineProcessingElementRequest> requests = pipelinePeReqRepo.findByRequesterInfo_OrganizationNot(orgName);

        return requests.stream()
                .map(PipelineRequestDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/requests")
    public List<PipelineRequestDto> getAllRequests() {
        List<PipelineProcessingElementRequest> requests = pipelinePeReqRepo.findByRequesterInfo_Organization(orgName);

        return requests.stream()
                .map(PipelineRequestDto::fromEntity)
                .collect(Collectors.toList());
    }





    @PreAuthorize("hasAuthority('APPROVE_REQUEST_PE')")
    @PostMapping("/request/take-decision")
    public ConfirmationResponse approveNodeRequest(@RequestBody ApproveProcessingElementRequestDto approveNodeRequestDto){
        PipelineProcessingElementRequest request = pipelinePeReqRepo.findById(approveNodeRequestDto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if(request.getStatus() != AccessRequestStatus.PENDING){
            throw new RuntimeException("Request already processed");
        }

        request.setAllowedDurationHours(request.getRequestedDurationHours());
        request.setStatus(approveNodeRequestDto.getStatus());
        if(approveNodeRequestDto.getStatus()==AccessRequestStatus.APPROVED) {

            request.setApprovalToken(tokenService.generateApprovalToken(toOutboundDto(request), request.getRequestedDurationHours()));
        }
        pipelinePeReqRepo.save(request);

        // sending response to Org:
        var response = new RequestResponse();
        response.setRequestId(request.getId());
        response.setRequestStatus(request.getStatus());
        System.out.println(approveNodeRequestDto.getStatus()==AccessRequestStatus.APPROVED);
        if (approveNodeRequestDto.getStatus()==AccessRequestStatus.APPROVED) {
            response.setToken(request.getApprovalToken());
            System.out.println("Generated token: " + request.getApprovalToken());
        }
        else
            response.setToken("Request was not approved, so no token is generated.");

        ConfirmationResponse remoteResponse = orgResponseService.sendResponseToOrg(response,
                request.getRequesterInfo().getOrganization());

//        // Send notification to the webhook URL provided in the request
//        String webhookUrl = request.getWebhookUrl(); // Assuming the webhook URL is stored in the request entity
//        String webhookResponseMessage = "Webhook URL is empty or not set";
//        // Prepare the data to send to the webhook
//        RequestResponse webhookResponse = new RequestResponse();
//        webhookResponse.setRequestId(request.getId());
//        webhookResponse.setRequestStatus(request.getStatus());
//        //webhookResponse.setToken(request.getApprovalToken());
//
//        // Use RestTemplate to send the notification to the webhook
//        RestTemplate restTemplate = new RestTemplate();
//        // Send a POST request to the webhook URL with the response data
//        ResponseEntity<String> webhookResponseEntity = restTemplate.exchange(
//                webhookUrl,
//                HttpMethod.POST,
//                new org.springframework.http.HttpEntity<>(webhookResponse),
//                String.class
//        );
//        webhookResponseMessage = webhookResponseEntity.getBody();

        return remoteResponse;
    }







    public ConfigureValidationDto getConfiguration(String pipelineName) {
        Pipeline pipeline = pipelineRepo.findByName(pipelineName).
                orElseThrow(() -> new IllegalArgumentException("Pipeline Not Found"));

        // Collect partner-owned processing elements with their org names
        var partnerElements = pipeline.getProcessingElements().stream()
                .filter(pe -> pe.getOwnerPartnerOrganization() != null)
                .map(pe -> new MissingPermissionsDto(
                        pe.getTemplateId(),
                        pe.getOwnerPartnerOrganization().getName()))
                .toList();

        ConfigureValidationDto validationDto = new ConfigureValidationDto();
        if (partnerElements.isEmpty()) {
            validationDto.setStatus("VALID");
            validationDto.setMissingPermissions(List.of());
        } else {
            // Load actual partner-owned elements
            var partnerElementsEntities = pipeline.getProcessingElements().stream()
                    .filter(pe -> pe.getOwnerPartnerOrganization() != null)
                    .collect(Collectors.toList());

            // Collect missing elements: those with no PENDING request
            List<MissingPermissionsDto> missingPermissions = partnerElementsEntities.stream()
                    .filter(pe -> !pipelinePeReqRepo
                            .existsByPipelineNameAndPipelineProcessingElementInstanceAndStatus(
                                    pipeline.getName(),          // matches column pipeline_id
                                    pipelinePeInstanceRepo.findByPipelineAndProcessingElement(pipeline, pe)
                                            .orElseThrow(() -> new IllegalStateException("Pipeline PE Instance not found")),      // match the specific instance
                                    AccessRequestStatus.APPROVED))
                    .map(pe -> new MissingPermissionsDto(
                            pe.getTemplateId(),
                            pe.getOwnerPartnerOrganization().getName()))
                    .collect(Collectors.toList());

            if (missingPermissions.isEmpty()) {
                validationDto.setStatus("VALID");
                validationDto.setMissingPermissions(List.of());
            } else {
                validationDto.setStatus("INVALID");
                validationDto.setMissingPermissions(missingPermissions);
            }
        }

        return validationDto;


    }

    private PipelineProcessingElementRequest convertDtoToEntity(PipelineProcessingElementRequestDto dto, User user) {
        ProcessingElement node = processingElementRepositry.findByTemplateId(dto.getProcessingElement())
                .orElseThrow(() -> new RuntimeException("Node not found: " + dto.getProcessingElement()));

        RequesterInfo requester = new RequesterInfo();
        requester.setRequesterId(user.getId());
        requester.setUsername(user.getUsername());
        requester.setOrganization(user.getOrganization().getName());
        requester.setToken(tokenService.generateTokenForPartnerOrgUser(user, 300));

        PipelineProcessingElementInstance peInstance = pipelinePeInstanceRepo.findByPipelineAndProcessingElement(
                pipelineRepo.findByName(dto.getPipelineName())
                        .orElseThrow(() -> new RuntimeException("Pipeline not found: " + dto.getPipelineName())),
                node)
                .orElseThrow(() -> new RuntimeException("Pipeline PE Instance not found for pipeline: " + dto.getPipelineName() + " and processing element: " + dto.getProcessingElement()));

        return PipelineProcessingElementRequest.builder()
                .id(UUID.randomUUID())
                .processingElement(node)
                .requesterInfo(requester)
                .pipelineName(dto.getPipelineName())
                .requestedDurationHours(dto.getRequestedDurationHours())
                .status(AccessRequestStatus.PENDING)
                .approvalToken("")
                .instanceNumber(peInstance.getInstanceNumber())
                .decisionTime(null)
                .pipelineProcessingElementInstance(peInstance)
                .build();
    }


        private PipelineProcessingElementRequestOutboundDto toOutboundDto(PipelineProcessingElementRequest entity) {
        PipelineProcessingElementRequestOutboundDto dto = new PipelineProcessingElementRequestOutboundDto();

        // 1) Top-level fields
        dto.setId(entity.getId());
        dto.setRequestedDurationHours(entity.getRequestedDurationHours());
        dto.setWebhookUrl(entity.getWebhookUrl());
        dto.setPipelineName(entity.getPipelineName());
        dto.setProcessingElementName(entity.getProcessingElement().getTemplateId());
        dto.setInstanceNumber(entity.getInstanceNumber());

        // 3) RequesterInfo
        if (entity.getRequesterInfo() != null) {
            RequesterInfo infoDto = new RequesterInfo();
            infoDto.setRequesterId(entity.getRequesterInfo().getRequesterId());
            infoDto.setUsername(entity.getRequesterInfo().getUsername());
            infoDto.setOrganization(entity.getRequesterInfo().getOrganization());
            infoDto.setToken(entity.getRequesterInfo().getToken());
            dto.setRequesterInfo(infoDto);
        }

        return dto;
    }

    public record PipelineRequestDto(
            UUID id,
            String pipelineName,
            String organization,
            AccessRequestStatus status,
            int requestedDurationHours,
            String approvalToken

    ) {
        public static PipelineRequestDto fromEntity(PipelineProcessingElementRequest entity) {
            return new PipelineRequestDto(
                    entity.getId(),
                    entity.getPipelineName(),
                    entity.getRequesterInfo().getOrganization(),
                    entity.getStatus(),
                    entity.getRequestedDurationHours(),
                    entity.getApprovalToken()
            );
        }
    }


}
