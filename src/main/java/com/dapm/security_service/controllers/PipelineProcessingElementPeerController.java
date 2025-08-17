
package com.dapm.security_service.controllers;

import com.dapm.security_service.models.ConfirmationResponse;
import com.dapm.security_service.models.PipelineProcessingElementRequest;
import com.dapm.security_service.models.ProcessingElement;
import com.dapm.security_service.models.RequesterInfo;
import com.dapm.security_service.models.dtos.peer.*;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import com.dapm.security_service.repositories.OrganizationRepository;
import com.dapm.security_service.repositories.PipelineProcessingElementRequestRepository;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import com.dapm.security_service.services.TokenVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/peer/pipeline-node-requests")
public class PipelineProcessingElementPeerController {

    @Autowired private PipelineProcessingElementRequestRepository requestRepository;
    @Autowired private ProcessingElementRepository processingElementRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private TokenVerificationService tokenVerificationService;

    /**
     * OrgA calls this endpoint to create a request in OrgB's DB.
     */
    @PostMapping
    public RequestResponse createRequest(@RequestBody PipelineProcessingElementRequestOutboundDto requestDto) {
        // Generate an ID if not provided
        if (requestDto.getId() == null) {
            requestDto.setId(UUID.randomUUID());
        }
        //verify verifyExternalUser using the token
        Boolean verified= tokenVerificationService.verifyExternalUser(
                requestDto.getRequesterInfo().getToken(),
                requestDto.getRequesterInfo().getOrganization()
        );
        System.out.println(
                "verification"+ verified
        );
        if (!verified) {
            throw new RuntimeException("External user verification failed for organization: " + requestDto.getRequesterInfo().getOrganization());
        }


        ProcessingElement processingElement= processingElementRepository.findByTemplateId(requestDto.getProcessingElementName())
                .orElseThrow(() -> new RuntimeException("Processing Element not found with Name: " + requestDto.getProcessingElementName()));
        processingElement.setOwnerOrganization(
                organizationRepository.findByName("OrgB")
                        .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + requestDto.getRequesterInfo().getOrganization()))
        );

        var request = PipelineProcessingElementRequest.builder()
                .id(requestDto.getId())
                .pipelineId(requestDto.getPipelineId())
                .pipelineNode(processingElement)
                .requesterInfo(requestDto.getRequesterInfo())
                .requestedExecutionCount(0)
                .requestedExecutionCount(0)
                .webhookUrl(requestDto.getWebhookUrl())
                .status(AccessRequestStatus.PENDING)
                .build();

        var savedRequest = requestRepository.save(request);

        var response = new RequestResponse();
        response.setRequestId(request.getId());
        response.setRequestStatus(AccessRequestStatus.PENDING);
        response.setToken("");

        return response;

    }

    /**
     * OrgA calls this endpoint to retrieve the entire request record (including the token if approved).
     */
    @GetMapping("/{id}")
    public PipelineProcessingElementRequest getRequestById(@PathVariable UUID id) {
        return requestRepository.findById(id).orElse(null);
    }

    /**
     * OrgA can call this to just get the status (if it doesn't want the entire request object).
     */
    @GetMapping("/{id}/status")
    public AccessRequestStatus getRequestStatus(@PathVariable UUID id) {
        PipelineProcessingElementRequest req = requestRepository.findById(id).orElse(null);
        return (req == null) ? null : req.getStatus();
    }

    // OrgB call this endpoint to send approval of the request.
    @Transactional
    @PostMapping("/approve")
    public ConfirmationResponse approveRequest(@RequestBody RequestResponse requestResponse){
        var request = requestRepository.findById(requestResponse.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if(request.getId() == null){
            var confirmationRespone = new ConfirmationResponse();
            confirmationRespone.setMessageReceived(false);
            return confirmationRespone;
        }

        //request.setApprovalToken(requestResponse.getToken());
        request.setStatus(requestResponse.getRequestStatus());

        requestRepository.save(request);
        // save them in cache so the configure can check them

        var confirmationResponse = new ConfirmationResponse();
        confirmationResponse.setMessageReceived(true);

        return confirmationResponse;
    }

}