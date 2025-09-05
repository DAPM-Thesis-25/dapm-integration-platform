package com.dapm.security_service.runtime;

import candidate_validation.ValidatedPipeline;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pipeline.PipelineBuilder;
import pipeline.service.PipelineExecutionService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/config-schemas")
@Tag(name = "Config Schemas")
public class ConfigSchemaController {

    // configurable in compose: runtime.configs.root=/runtime-configs
    @Value("${runtime.configs.root:/runtime-configs}")
    private String rootDir;

    private final PipelineBuilder pipelineBuilder;
    private final PipelineExecutionService executionService;

    public ConfigSchemaController(PipelineBuilder pipelineBuilder, PipelineExecutionService executionService) {
        this.pipelineBuilder = pipelineBuilder;
        this.executionService=executionService;
    }


    private Path root() {
        return Path.of(rootDir).toAbsolutePath().normalize();
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a JSON Schema file; saved exactly as its original filename")
    public ResponseEntity<String> upload(@RequestPart("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded.");
        }
        String originalName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalName)) {
            return ResponseEntity.badRequest().body("Filename is missing.");
        }

        // ensure root exists
        Files.createDirectories(root());

        // protect against path traversal (../) while still keeping the original name
        Path dest = root().resolve(Paths.get(originalName).getFileName().toString()).normalize();
        if (!dest.startsWith(root())) {
            return ResponseEntity.badRequest().body("Invalid filename.");
        }

        // overwrite if exists
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        return ResponseEntity.ok("Saved: " + dest.toString());
    }

    @GetMapping(value = "/{filename}")
    @Operation(summary = "Download a schema file by filename")
    public ResponseEntity<Resource> get(@PathVariable String filename) throws Exception {
        Path p = root().resolve(Paths.get(filename).getFileName().toString()).normalize();
        if (!p.startsWith(root()) || !Files.exists(p)) {
            return ResponseEntity.notFound().build();
        }
        Resource r = new InputStreamResource(Files.newInputStream(p));
        String contentType = filename.endsWith(".json")
                ? "application/schema+json"
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=300")
                .eTag(String.valueOf(Files.getLastModifiedTime(p).toMillis()))
                .contentType(MediaType.parseMediaType(contentType))
                .body(r);
    }

    @GetMapping
    @Operation(summary = "List all schema filenames")
    public List<String> list() throws Exception {
        if (!Files.exists(root())) return List.of();
        try (Stream<Path> s = Files.list(root())) {
            return s.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());
        }
    }

    @RequestMapping(path = "/{filename}", method = RequestMethod.HEAD)
    @Operation(summary = "Check if a schema filename exists (HEAD 200/404)")
    public ResponseEntity<Void> exists(@PathVariable String filename) throws Exception {
        Path p = root().resolve(Paths.get(filename).getFileName().toString()).normalize();
        return (p.startsWith(root()) && Files.exists(p))
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/validate")
    @Operation(summary = "Try to validate pipeline")
    public String get2() throws Exception {
        String cfgRoot = System.getenv().getOrDefault("runtime.configs.root", "/runtime-configs");
        java.net.URI configURI = java.nio.file.Paths.get(cfgRoot).toUri();

        String pipelineID = "orgC_pipeline";
        String contents;
        try (InputStream is = new ClassPathResource("simple_pipeline.json").getInputStream()) {
            contents = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("simple_pipeline.json not found on classpath", e);
        }

        System.out.println(contents+" I am ii config");

        System.out.println(configURI);
        ValidatedPipeline validatedPipeline = new ValidatedPipeline(contents, configURI);
        System.out.println(validatedPipeline+ "I am here");

        pipelineBuilder.buildPipeline(pipelineID, validatedPipeline);
        System.out.println(validatedPipeline+ "I am here");

        executionService.start(pipelineID);
        try {
            Thread.sleep(30000); // 1 minute = 60,000 ms
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt(); // restore interrupt flag
        }
        executionService.terminate(pipelineID);
        System.out.println("Terminate");


        return "Executed for 30s";
    }
}
