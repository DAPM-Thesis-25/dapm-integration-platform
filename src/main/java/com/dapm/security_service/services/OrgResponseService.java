package com.dapm.security_service.services;

import com.dapm.security_service.models.dtos.ConfirmationResponse;
import com.dapm.security_service.models.dtos.peer.RequestResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrgResponseService {
    private final RestTemplate restTemplate = new RestTemplate();


    private String GET_ORG_BASE_URL(String orgName) {
        return "http://"+orgName.toLowerCase()+":8080/api/peer/request-access/approve";
    }

    public ConfirmationResponse sendResponseToOrg(RequestResponse response, String orgName) {
        return restTemplate.postForObject(
                GET_ORG_BASE_URL(orgName),
                response,
                ConfirmationResponse.class
        );
    }
}
