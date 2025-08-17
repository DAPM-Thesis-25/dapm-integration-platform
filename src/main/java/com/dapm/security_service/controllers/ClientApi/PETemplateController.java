package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.ProcessingElement;
import com.dapm.security_service.models.dtos.ProcessingElementDto;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/processingElements")
public class PETemplateController {
    @Autowired
    private ProcessingElementRepository processingElementRepository;

    @GetMapping
    public List<ProcessingElementDto> getAllProcessingElements() {
        var processingElements = processingElementRepository.findAll();
        return processingElements.stream().map(ProcessingElementDto::new).toList();
    }

    @GetMapping("/{id}")
    public ProcessingElementDto getProcessingElementById(@PathVariable UUID id) {
        return processingElementRepository.findById(id).map(ProcessingElementDto::new).orElse(null);
    }

    @PostMapping
    public ProcessingElementDto createProcessingElement(@RequestBody ProcessingElement processingElement) {
        if (processingElement.getId() == null) {
            processingElement.setId(UUID.randomUUID());
        }


        ProcessingElement savedProcessingElement = processingElementRepository.save(processingElement);
        return new ProcessingElementDto(savedProcessingElement);
    }

    @PutMapping("/{id}")
    public ProcessingElementDto updateProcessingElement(@PathVariable UUID id, @RequestBody ProcessingElement processingElement) {
        processingElement.setId(id);
        return new ProcessingElementDto(processingElementRepository.save(processingElement));
    }

    @DeleteMapping("/{id}")
    public void deleteProcessingElement(@PathVariable UUID id) {
        processingElementRepository.deleteById(id);
    }
}
