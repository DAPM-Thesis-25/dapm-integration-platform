package com.dapm.security_service.models.dtos2;
import lombok.Data;

@Data
public class SubscriptionRequestDto {
    private String orgName;
    private String voucher;
    private String token;
}
