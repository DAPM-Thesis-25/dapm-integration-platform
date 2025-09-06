package com.dapm.security_service.models.dtos2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PipelinePeInstanceDto {
    private String pipelineName;
    private String processingElementTemplateId;
    private Integer instanceNumber;
}

