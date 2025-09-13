package com.dapm.security_service.services;

import com.dapm.security_service.models.PipelineProcessingElementRequest;
import com.dapm.security_service.models.dtos2.PipelineProcessingElementRequestOutboundDto;
import com.dapm.security_service.models.dtos.peer.RequestResponse;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class OrgRequestService {

    private final RestTemplate restTemplate = new RestTemplate();

    private String getORG_B_BASE_URL(String orgName) {
        return "http://"+orgName.toLowerCase()+":8080/api/peer/request-access";
    }
    public RequestResponse sendRequestToOrg(PipelineProcessingElementRequestOutboundDto requestDto,String orgName) {
        // Send the DTO to OrgB and expect the same DTO type in response
        return restTemplate.postForObject(
                getORG_B_BASE_URL(orgName),
                requestDto,
                RequestResponse.class
        );
    }

}