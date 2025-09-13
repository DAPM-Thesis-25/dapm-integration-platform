package com.dapm.security_service.controllers.Client2Api;

import com.dapm.security_service.models.Organization;
import com.dapm.security_service.models.Tiers;
import com.dapm.security_service.models.enums.Tier;
import com.dapm.security_service.repositories.OrganizationRepository;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import com.dapm.security_service.repositories.TiersRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pipeline.processingelement.Configuration;
import pipeline.processingelement.ProcessingElement;
import repository.TemplateRepository;

@RestController
@RequestMapping("/templates")
public class ProcessingElementUploadController {

    @Value("${runtime.templates.root:/runtime-templates}")
    private String rootDir;

    @Value("${dapm.defaultOrgName}")
    private String orgName;



    @Value("${runtime.configs.root:/runtime-configs}")
    private String condigDir;

    @Autowired
    private ProcessingElementRepository processingElementRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private TiersRepository tiersRepository;

    private final TemplateRepository repo;

    public ProcessingElementUploadController(TemplateRepository repo) {
        this.repo = repo;
    }

    private Path root() {
        return Path.of(condigDir).toAbsolutePath().normalize();
    }


    public static String getFileNameWithoutExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename(); // e.g., "example.txt"
        if (originalFilename == null) {
            return null;
        }

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) { // make sure there's at least one character before the dot
            return originalFilename.substring(0, dotIndex);
        } else {
            return originalFilename; // no extension found
        }
    }

    @PostMapping(
            value = "/uploadNewProcessingElement",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Upload a Java source file and register it as a ProcessingElement template")
    public ResponseEntity<String> uploadJavaFile(
            @RequestPart("template")
            @Parameter(
                    description = "Java source file (*.java)",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            MultipartFile file,
            @RequestPart("configSchema") MultipartFile file2,
            @RequestParam(value = "tier", required = true) Tier tier,
            @RequestParam(value = "riskLevel", required = true) String riskLevel,
            @RequestParam(value = "output", required = false) String output,
            @RequestParam(value = "inputs", required = false) Set<String> inputs

            ) throws Exception {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded.");
        }
        if (tier == null) {
            return ResponseEntity.badRequest().body("Tier is required.");
        }

        // upload configuration file
        if (file2 == null || file2.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded.");
        }
        String originalName = file2.getOriginalFilename();
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



        // Prepare directories
        Path srcRoot = Path.of(rootDir, "src");
        Path binRoot = Path.of(rootDir, "bin");
        Files.createDirectories(srcRoot);
        Files.createDirectories(binRoot);

        String templateID=getFileNameWithoutExtension(file);
        String fqcn="templates."+templateID;

        // Save uploaded file under src/<fqcn>.java
        Path javaPath = srcRoot.resolve(fqcn.replace('.', '/') + ".java");
        Files.createDirectories(javaPath.getParent());
        Files.copy(file.getInputStream(), javaPath, StandardCopyOption.REPLACE_EXISTING);

        // Compile with the JDK compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return ResponseEntity.badRequest().body("JDK compiler not available (run the app with a JDK, not a JRE).");
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, null, null)) {
            Iterable<? extends JavaFileObject> sources =
                    fm.getJavaFileObjectsFromFiles(List.of(javaPath.toFile()));

            // Build classpath for containerized Spring Boot layout (and for local dev)
            String classpath = resolveCompileClasspath();

            JavaCompiler.CompilationTask task = compiler.getTask(
                    null, fm, diagnostics,
                    List.of("-d", binRoot.toString(), "-cp", classpath),
                    null, sources
            );

            boolean ok = task.call();
            if (!ok) {
                String diag = diagnostics.getDiagnostics().stream()
                        .map(d -> d.getKind() + " " + d.getSource() + ":" + d.getLineNumber() + " - " + d.getMessage(null))
                        .collect(Collectors.joining("\n"));
                return ResponseEntity.badRequest().body("Compilation failed:\n" + diag);
            }
        }

        // Load the compiled class
        Class<?> cls;
        try (URLClassLoader cl = new URLClassLoader(
                new URL[]{binRoot.toUri().toURL()},
                this.getClass().getClassLoader()
        )) {
            cls = Class.forName(fqcn, true, cl);
        }

        // Validate ProcessingElement type and constructor
        if (!ProcessingElement.class.isAssignableFrom(cls)) {
            return ResponseEntity.badRequest().body("Class does not extend ProcessingElement: " + fqcn);
        }
        // Must declare ctor(Configuration)
        cls.getDeclaredConstructor(Configuration.class);
        Files.copy(file2.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);


        // Register in TemplateRepository
        @SuppressWarnings("unchecked")
        Class<? extends ProcessingElement> peClass = (Class<? extends ProcessingElement>) cls;
        repo.storeTemplate(templateID, peClass);

        // save configuration file
        Files.copy(file2.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        Organization org=organizationRepository.findByName(orgName)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + orgName));
        if(org == null) {
            return ResponseEntity.badRequest().body("Organization not found: " + orgName);
        }

//        Tiers tierEntity=tiersRepository.findByName(tier)
//                .orElseThrow(() -> new IllegalArgumentException("Tier not found: " + tier));
        // create a new ProcessingElement
        com.dapm.security_service.models.ProcessingElement peB = com.dapm.security_service.models.ProcessingElement.builder()
                .id(UUID.randomUUID())
                .ownerOrganization(org)
                .templateId(templateID)
                .instanceNumber(0)
                .hostURL("http://"+orgName.toLowerCase()+":8080")
                .inputs(inputs)
                .output(output)
                .tier(tier)
                .riskLevel(riskLevel)
                .build();
        // save the ProcessingElement to the repository
        processingElementRepository.save(peB);

        return ResponseEntity.ok("Uploaded, compiled, loaded, and registered: " + templateID + " -> " + fqcn);
    }

    /**
     * Attempts to construct a reasonable classpath both for local dev and for Spring Boot layers in containers.
     */
    private String resolveCompileClasspath() {
        String base = Objects.toString(System.getProperty("java.class.path"), "");
        String ps = System.getProperty("path.separator");

        String[] candidates = {
                // Spring Boot executable jar layout (local jar run)
                "BOOT-INF/classes",
                "BOOT-INF/lib/*",

                // Common container layouts (Boot build-image / Paketo)
                "/app/BOOT-INF/classes",
                "/app/BOOT-INF/lib/*",
                "/workspace/app/BOOT-INF/classes",
                "/workspace/app/BOOT-INF/lib/*",

                // (Optional fallbacks)
                "/app/application",
                "/app/dependencies/*",
                "/app/snapshot-dependencies/*"
        };

        String extras = Stream.of(candidates)
                .filter(p -> {
                    if (p.endsWith("/*")) {
                        Path dir = Path.of(p.substring(0, p.length() - 2));
                        return Files.exists(dir);
                    }
                    return Files.exists(Path.of(p));
                })
                .collect(Collectors.joining(ps));

        return extras.isBlank() ? base : (base.isBlank() ? extras : base + ps + extras);
    }

}
