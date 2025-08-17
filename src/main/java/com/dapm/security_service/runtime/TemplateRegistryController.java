package com.dapm.security_service.runtime;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pipeline.processingelement.ProcessingElement;
import repository.TemplateRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/templates")
@Tag(name = "Templates Registry")
public class TemplateRegistryController {

    private final TemplateRepository repo;

    public TemplateRegistryController(TemplateRepository repo) {
        this.repo = repo;
    }

    // --- DTOs ---

    @Schema(description = "Basic info about a registered template")
    public static class TemplateInfo {
        public String templateID;
        public String className;

        public TemplateInfo(String templateID, String className) {
            this.templateID = templateID;
            this.className = className;
        }
    }

    // --- Endpoints ---

    @GetMapping("/ids")
    @Operation(summary = "List all registered template IDs")
    public List<String> listTemplateIds() {
        return repo.getTemplates().keySet().stream().sorted().collect(Collectors.toList());
    }

    @GetMapping
    @Operation(summary = "List all registered templates with their class names")
    public List<TemplateInfo> listTemplates() {
        return repo.getTemplates().entrySet().stream()
                .map(e -> new TemplateInfo(e.getKey(), e.getValue().getName()))
                .sorted((a, b) -> a.templateID.compareToIgnoreCase(b.templateID))
                .collect(Collectors.toList());
    }

    @GetMapping("/{templateID}")
    @Operation(summary = "Get info about a single registered template")
    public ResponseEntity<TemplateInfo> getTemplate(@PathVariable String templateID) {
        Class<? extends ProcessingElement> cls = repo.getTemplates().get(templateID);
        if (cls == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(new TemplateInfo(templateID, cls.getName()));
    }

    @RequestMapping(path = "/{templateID}", method = RequestMethod.HEAD)
    @Operation(summary = "Check if a template ID exists (HEAD 200/404)")
    public ResponseEntity<Void> templateExists(@PathVariable String templateID) {
        return repo.getTemplates().containsKey(templateID)
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/count")
    @Operation(summary = "Get how many templates are registered")
    public Map<String, Integer> countTemplates() {
        return Map.of("count", repo.getTemplates().size());
    }
}
