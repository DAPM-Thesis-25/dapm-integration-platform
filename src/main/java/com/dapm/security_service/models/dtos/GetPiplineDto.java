package com.dapm.security_service.models.dtos;


import com.dapm.security_service.models.Channel;
import com.dapm.security_service.models.Pipeline;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetPiplineDto {
    private String name;
    private String project;

    private List<String> processingElements;

    private List<List<String>> channels;
    //Cannot resolve constructor 'PipelineDesignDto(Pipeline)'
    public GetPiplineDto(Pipeline pipeline) {
        this.name = pipeline.getName();
        this.project = pipeline.getProject().getName();
        this.processingElements = pipeline.getProcessingElements().stream()
                .map(pe -> pe.getTemplateId() + " (" +
                        (pe.getOwnerOrganization() == null
                                ? pe.getOwnerPartnerOrganization().getName()
                                : pe.getOwnerOrganization().getName())
                        + ")")
                .toList();
        this.channels = pipeline.getChannels().stream()
                .map(channel -> List.of(channel.getSource().toString(), channel.getTarget().toString()))
                .toList();
    }

}
