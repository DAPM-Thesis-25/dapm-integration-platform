package com.dapm.security_service.controllers;
import com.dapm.security_service.models.*;
import com.dapm.security_service.models.dtos.PipelineProcessingElementRequestDto;
import com.dapm.security_service.models.dtos.peer.PipelineProcessingElementRequestOutboundDto;
import com.dapm.security_service.models.dtos.peer.RequestResponse;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import com.dapm.security_service.repositories.PipelineProcessingElementRequestRepository;
import com.dapm.security_service.repositories.PipelineRepositoryy;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import com.dapm.security_service.repositories.UserRepository;
import com.dapm.security_service.security.CustomUserDetails;
import com.dapm.security_service.services.OrgBRequestService;
import com.dapm.security_service.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/client/pipeline-processingElement")
public class PipelineProcessingElementRequestClientController {

    @Autowired private OrgBRequestService orgBRequestService;
    @Autowired private TokenService tokenService;

    @Autowired private PipelineRepositoryy pipelineRepository;

    @Autowired private PipelineProcessingElementRequestRepository pipelineNodeRequestRepository;
    @Autowired private ProcessingElementRepository processingElementRepositry;
    @Autowired private UserRepository userRepository;
     //  Receive a webhook notification from OrgB about request status changes.
    @PostMapping("/webhook")
    public String handleWebhookNotification(@RequestBody RequestResponse requestResponse) {
        UUID requestId = requestResponse.getRequestId();
        // Fetch the corresponding request from the database
        PipelineProcessingElementRequest request = pipelineNodeRequestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        // Update the status and other details based on the webhook response
        request.setStatus(requestResponse.getRequestStatus());
        //request.setApprovalToken(requestResponse.getToken());

        // Save the updated request
        //pipelineNodeRequestRepository.save(request);

        // Optionally log or send a response indicating the webhook was received successfully
        System.out.println("Webhook received by Org A for request ID: " + requestId);
        System.out.println(requestResponse);
        return "webhook received";
    }

    @PreAuthorize("@pipelineAccessEvaluator.hasPermission(#requestDto.getPiplineName(), authentication, 'ACCESS_REQUEST_PE')")
    @PostMapping("/request")
    public RequestResponse initiatePeerRequest(
            @RequestBody PipelineProcessingElementRequestDto requestDto
    ,@AuthenticationPrincipal CustomUserDetails userDetails) {
//        if (!pipelineAccessEvaluator.hasPermission(requestDto.getPiplineName(), authentication, "ACCESS_REQUEST_PE")) {
//            throw new AccessDeniedException("You do not have permission to access this pipeline.");
//        }
        PipelineProcessingElementRequest request = convertDtoToEntity(requestDto,userDetails.getUser());
        String webhookUrl = "http://orga:8080/api/client/pipeline-processingElement/webhook";
        request.setWebhookUrl(webhookUrl);

        // 2. Save locally
        PipelineProcessingElementRequest localRequest = pipelineNodeRequestRepository.save(request);

        System.out.println("my request is "+ localRequest);

        // 3. Convert entity → outbound DTO
        PipelineProcessingElementRequestOutboundDto outboundDto = toOutboundDto(localRequest);


        // 4. Send the outbound DTO to OrgB
        // (orgBRequestService should accept the outbound DTO instead of the entity)
        RequestResponse remoteResponseDto = orgBRequestService.sendRequestToOrgB(outboundDto);

//        // Update the local record with any details returned from OrgB (e.g., approval token, updated status).
//        localRequest.setApprovalToken(remoteResponseDto.getApprovalToken());
//        localRequest.setStatus(remoteResponseDto.getStatus());
//        localRequest.setDecisionTime(remoteResponseDto.getDecisionTime());
//        localRequest = pipelineNodeRequestRepository.save(localRequest);

        // Return the updated local record.
        return remoteResponseDto;
    }

    /**
     * Check the status of a request that was sent to OrgB.
     * This method calls OrgB's PeerApi to retrieve the latest status.
     */
    @GetMapping("/{id}/status")
    public AccessRequestStatus getRequestStatus(@PathVariable UUID id) {
        return orgBRequestService.getRequestStatusFromOrgB(id);
    }

    /**
     * Get the final approved request record from OrgB (which may contain the JWT token).
     */
    @GetMapping("/{id}/details")
    public PipelineProcessingElementRequest getRequestDetails(@PathVariable UUID id) {
        return orgBRequestService.getRequestDetailsFromOrgB(id);
    }

    private PipelineProcessingElementRequest convertDtoToEntity(PipelineProcessingElementRequestDto dto, User user) {
        ProcessingElement node = processingElementRepositry.findByTemplateId(dto.getProcessingElement())
                .orElseThrow(() -> new RuntimeException("Node not found: " + dto.getProcessingElement()));

        RequesterInfo requester = new RequesterInfo();
        requester.setRequesterId(user.getId());
        requester.setUsername(user.getUsername());
        requester.setOrganization(user.getOrganization().getName());
        requester.setToken(tokenService.generateTokenForPartnerOrgUser(user, 300));

        // get pilineId from pipline repository
        Pipeline pipeline= pipelineRepository.findByName(dto.getPiplineName())
                .orElseThrow(() -> new RuntimeException("Pipeline not found: " + dto.getPiplineName()));

        return PipelineProcessingElementRequest.builder()
                .id(UUID.randomUUID())
                .pipelineNode(node)
                .requesterInfo(requester)
                .pipelineId(pipeline.getId())
                .requestedExecutionCount(dto.getRequestedExecutionCount())
                .requestedDurationHours(dto.getRequestedDurationHours())
                .status(AccessRequestStatus.PENDING)
                .approvalToken("")
                .decisionTime(null)
                .build();
    }
    private PipelineProcessingElementRequestOutboundDto toOutboundDto(PipelineProcessingElementRequest entity) {
        PipelineProcessingElementRequestOutboundDto dto = new PipelineProcessingElementRequestOutboundDto();

        // 1) Top-level fields
        dto.setId(entity.getId());
        dto.setRequestedExecutionCount(entity.getRequestedExecutionCount());
        dto.setRequestedDurationHours(entity.getRequestedDurationHours());
        dto.setStatus(entity.getStatus());
        dto.setApprovalToken(entity.getApprovalToken());
        dto.setDecisionTime(entity.getDecisionTime());
        dto.setWebhookUrl(entity.getWebhookUrl());

        dto.setPipelineId(entity.getPipelineId());

        // 2) PipelineNode ID
        if (entity.getPipelineNode() != null) {
            dto.setProcessingElementName(entity.getPipelineNode().getTemplateId());
        }

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

}