package com.dapm.security_service.controllers.Client2Api;

import com.dapm.security_service.models.Pipeline;
import com.dapm.security_service.models.dtos.ProjectDto;
import com.dapm.security_service.models.dtos2.PipelineDto;
import com.dapm.security_service.repositories.PipelineRepositoryy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pipelines/")
public class PipelineController {
    @Autowired
    PipelineRepositoryy pipelineRepositoryy;

    // get all pipelines
    @GetMapping("/all")
    public List<PipelineDto> getAllPipelines() {
        return pipelineRepositoryy.findAll()
                .stream()
                .map(PipelineDto::new)
                .toList();
    }
}
