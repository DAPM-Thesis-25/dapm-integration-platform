package com.dapm.security_service.models.dtos2;

import com.dapm.security_service.models.enums.Tier;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class CreatePEDto {
    private Tier tier;
    private Set<String> inputs;
    private String output;
}
