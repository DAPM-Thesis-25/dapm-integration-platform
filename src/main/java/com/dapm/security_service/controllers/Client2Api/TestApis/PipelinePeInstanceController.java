package com.dapm.security_service.controllers.Client2Api.TestApis;


import com.dapm.security_service.models.PipelineProcessingElementInstance;
import com.dapm.security_service.models.dtos2.PipelinePeInstanceDto;
import com.dapm.security_service.repositories.PipelinePeInstanceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pipeline/instances")
public class PipelinePeInstanceController {

    @Autowired
    private PipelinePeInstanceRepo instanceRepo;

    @GetMapping("/all")
    public ResponseEntity<List<PipelinePeInstanceDto>> getAllInstances() {
        List<PipelineProcessingElementInstance> entities = instanceRepo.findAll();

        List<PipelinePeInstanceDto> dtos = entities.stream()
                .map(e -> new PipelinePeInstanceDto(
                        e.getPipeline().getName(),
                        e.getProcessingElement().getTemplateId(),
                        e.getInstanceNumber()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
