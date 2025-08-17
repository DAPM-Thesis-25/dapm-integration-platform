package com.dapm.security_service.services;

import com.dapm.security_service.models.PipelineProcessingElementRequest;
import com.dapm.security_service.models.dtos.peer.PipelineProcessingElementRequestOutboundDto;
import com.dapm.security_service.models.dtos.peer.RequestResponse;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class OrgBRequestService {

    private final RestTemplate restTemplate = new RestTemplate();

    // In a real app, load this from a config property, e.g., "orgB.peerApi.baseUrl"
  //private final String ORG_B_BASE_URL = "http://localhost:8082/api/peer/pipeline-node-requests";
    private final String ORG_B_BASE_URL = "http://orgb:8080/api/peer/pipeline-node-requests";
    /**
     * Send a new request to OrgB's PeerApi.http://orgb:8080/api
     */

    public String testOrgBConnection() {
        String url = ORG_B_BASE_URL + "/hello";
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            return "Failed to connect to OrgB: " + e.getMessage();
        }
    }
    public RequestResponse sendRequestToOrgB(PipelineProcessingElementRequestOutboundDto requestDto) {
        // Send the DTO to OrgB and expect the same DTO type in response
        return restTemplate.postForObject(
                ORG_B_BASE_URL,
                requestDto,
                RequestResponse.class
        );
    }


    /**
     * Poll OrgB's PeerApi for the status of a request.
     */
    public AccessRequestStatus getRequestStatusFromOrgB(UUID requestId) {
        String url = ORG_B_BASE_URL + "/" + requestId + "/status";
        return restTemplate.getForObject(url, AccessRequestStatus.class);
    }

    /**
     * Retrieve the entire request record from OrgB, which may include an approvalToken if approved.
     */
    public PipelineProcessingElementRequest getRequestDetailsFromOrgB(UUID requestId) {
        String url = ORG_B_BASE_URL + "/" + requestId;
        return restTemplate.getForObject(url, PipelineProcessingElementRequest.class);
    }
}