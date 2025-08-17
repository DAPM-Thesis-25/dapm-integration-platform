package com.dapm.security_service.services;

import com.dapm.security_service.models.ConfirmationResponse;
import com.dapm.security_service.models.dtos.peer.RequestResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrgARequestService {
    private final RestTemplate restTemplate = new RestTemplate();

    private final String ORG_A_BASE_URL = "http://orga:8080/api/peer/pipeline-node-requests/approve";

    public ConfirmationResponse sendResponseToOrgA(RequestResponse response){
        return restTemplate.postForObject(
                ORG_A_BASE_URL,
                response,
                ConfirmationResponse.class
        );
    }
}
