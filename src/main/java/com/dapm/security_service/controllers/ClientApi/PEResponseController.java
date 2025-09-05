package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.dtos.ConfirmationResponse;
import com.dapm.security_service.models.PipelineProcessingElementRequest;
import com.dapm.security_service.models.dtos.ApproveProcessingElementRequestDto;
import com.dapm.security_service.models.dtos.ProccessingElementAccessRequestDto;
import com.dapm.security_service.models.dtos.peer.RequestResponse;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import com.dapm.security_service.repositories.PipelineProcessingElementRequestRepository;
import com.dapm.security_service.services.OrgARequestService;
import com.dapm.security_service.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pipeline-processingElements-requests")
public class PEResponseController {

    @Autowired private PipelineProcessingElementRequestRepository requestRepository;
    @Autowired private TokenService tokenService;
    @Autowired private OrgARequestService orgARequestService;

    // Get all pipeline node requests.
    @GetMapping
    public List<ProccessingElementAccessRequestDto> getAllRequests() {
        return requestRepository.findAll()
                .stream()
                .map(ProccessingElementAccessRequestDto::new)
                .toList();

    }

    // Get a specific request by its ID.
    @GetMapping("/{id}")
    public PipelineProcessingElementRequest getRequestById(@PathVariable UUID id) {
        return requestRepository.findById(id).orElse(null);
    }




    @PreAuthorize("hasAuthority('APPROVE_REQUEST_PE')")
    @PostMapping("/approve")
    public String approveNodeRequest(@RequestBody ApproveProcessingElementRequestDto approveNodeRequestDto){
        PipelineProcessingElementRequest request = requestRepository.findById(approveNodeRequestDto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if(request.getStatus() != AccessRequestStatus.PENDING){
            throw new RuntimeException("Request already processed");
        }

        // convert hr - min - sec - ms
        request.setAllowedDurationHours(((approveNodeRequestDto.getAllowedDurationHours() * 60) * 60) * 1000);
        request.setAllowedNoExecutions(approveNodeRequestDto.getAllowedNoExecutions());

        //request.setApprovalToken(tokenService.generateTokenForNodeRequest(request));
        request.setStatus(AccessRequestStatus.APPROVED);

        requestRepository.save(request);

        // sending response to OrgA:
        var response = new RequestResponse();
        response.setRequestId(request.getId());
        response.setRequestStatus(request.getStatus());
        //response.setToken(request.getApprovalToken());
        ConfirmationResponse remoteResponse = orgARequestService.sendResponseToOrgA(response);

        // Send notification to the webhook URL provided in the request
        String webhookUrl = request.getWebhookUrl(); // Assuming the webhook URL is stored in the request entity
        String webhookResponseMessage = "Webhook URL is empty or not set";
        // Prepare the data to send to the webhook
        RequestResponse webhookResponse = new RequestResponse();
        webhookResponse.setRequestId(request.getId());
        webhookResponse.setRequestStatus(request.getStatus());
        //webhookResponse.setToken(request.getApprovalToken());

        // Use RestTemplate to send the notification to the webhook
        RestTemplate restTemplate = new RestTemplate();
        // Send a POST request to the webhook URL with the response data
        ResponseEntity<String> webhookResponseEntity = restTemplate.exchange(
                        webhookUrl,
                        HttpMethod.POST,
                        new org.springframework.http.HttpEntity<>(webhookResponse),
                        String.class
        );
        webhookResponseMessage = webhookResponseEntity.getBody();

        // update this part and find a something that Bob should see after approval: could be a 204
        return remoteResponse.isMessageReceived() + "\n "+ webhookResponseMessage;
    }

    @PreAuthorize("hasAuthority('APPROVE_REQUEST_PE')")
    @PostMapping("/reject")
    public String rejectNodeRequest(@RequestBody ApproveProcessingElementRequestDto approveNodeRequestDto){
        PipelineProcessingElementRequest request = requestRepository.findById(approveNodeRequestDto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if(request.getStatus() != AccessRequestStatus.PENDING){
            throw new RuntimeException("Request already processed");
        }

        // convert hr - min - sec - ms
        request.setAllowedDurationHours(((approveNodeRequestDto.getAllowedDurationHours() * 60) * 60) * 1000);
        request.setAllowedNoExecutions(approveNodeRequestDto.getAllowedNoExecutions());

        //request.setApprovalToken(tokenService.generateTokenForNodeRequest(request));
        request.setStatus(AccessRequestStatus.REJECTED);

        requestRepository.save(request);

        // sending response to OrgA:
        var response = new RequestResponse();
        response.setRequestId(request.getId());
        response.setRequestStatus(request.getStatus());
        //response.setToken(request.getApprovalToken());
        ConfirmationResponse remoteResponse = orgARequestService.sendResponseToOrgA(response);

        // Send notification to the webhook URL provided in the request
        String webhookUrl = request.getWebhookUrl(); // Assuming the webhook URL is stored in the request entity
        String webhookResponseMessage = "Webhook URL is empty or not set";
        // Prepare the data to send to the webhook
        RequestResponse webhookResponse = new RequestResponse();
        webhookResponse.setRequestId(request.getId());
        webhookResponse.setRequestStatus(request.getStatus());
        //webhookResponse.setToken(request.getApprovalToken());

        // Use RestTemplate to send the notification to the webhook
        RestTemplate restTemplate = new RestTemplate();
        // Send a POST request to the webhook URL with the response data
        ResponseEntity<String> webhookResponseEntity = restTemplate.exchange(
                webhookUrl,
                HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(webhookResponse),
                String.class
        );
        webhookResponseMessage = webhookResponseEntity.getBody();

        // update this part and find a something that Bob should see after approval: could be a 204
        return remoteResponse.isMessageReceived() + "\n "+ webhookResponseMessage;
    }




}
