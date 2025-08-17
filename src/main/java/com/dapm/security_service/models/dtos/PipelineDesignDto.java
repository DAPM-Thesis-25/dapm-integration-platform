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
public class PipelineDesignDto {
    private String name;
    private String project;

    private List<ProcessingElementDto> processingElements;

    private List<List<String>> channels;
    //Cannot resolve constructor 'PipelineDesignDto(Pipeline)'
    public PipelineDesignDto(Pipeline pipeline) {
        this.name = pipeline.getName();
        this.project = pipeline.getProject().getName();
        this.processingElements = pipeline.getProcessingElements().stream()
                .map(ProcessingElementDto::new)
                .toList();
        this.channels = pipeline.getChannels().stream()
                .map(channel -> List.of(channel.getSource().toString(), channel.getTarget().toString()))
                .toList();
    }

}
