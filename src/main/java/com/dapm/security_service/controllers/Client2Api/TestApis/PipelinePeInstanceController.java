package com.dapm.security_service.controllers.Client2Api.TestApis;


import com.dapm.security_service.models.PipelineProcessingElementInstance;
import com.dapm.security_service.models.dtos2.PipelinePeInstanceDto;
import com.dapm.security_service.repositories.PetriNetRepository;
import com.dapm.security_service.repositories.PipelinePeInstanceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pipeline.Pipeline;
import repository.PEInstanceRepository;
import pipeline.processingelement.ProcessingElement;
import repository.PipelineRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pipeline/instances")
public class PipelinePeInstanceController {

    @Autowired
    private PipelinePeInstanceRepo instanceRepo;

    @Autowired private PetriNetRepository petriNetRepository;
    @Autowired private PipelineRepository pipelineRepository;

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

//    @GetMapping("/{instanceId}/petri-net")
//    public ResponseEntity<String> getPetriNet(@PathVariable int instanceId) {
//        return petriNetRepository.get(instanceId)
//                .map(svg -> ResponseEntity.ok(svg))
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }

//    @GetMapping("/petri-status")
//    public ResponseEntity<String> getPetriStatus() {
//        String petriStatus = petriNetRepository.getPetri();
//        return ResponseEntity.ok(petriStatus);
//    }

    @Autowired
    private PEInstanceRepository peInstanceRepository;

    @GetMapping("/{instanceId}/petri-nett")
    public ResponseEntity<String> getPetriNet(@PathVariable String instanceId) {
        System.out.println("Looking up instanceId: " + instanceId);

        var pe = peInstanceRepository.getInstance(instanceId);

        try {
            var method = pe.getClass().getMethod("getLatestSvg");
            Object svg = method.invoke(pe);
            return ResponseEntity.ok(svg != null ? svg.toString() : "");
        } catch (NoSuchMethodException e) {
            return ResponseEntity.badRequest().body("Instance does not support getLatestSvg()");
        } catch (Exception e) {
            throw new RuntimeException("Error calling getLatestSvg()", e);
        }
    }


    // get pipeline name by name
    @GetMapping("/pipeline/{pipelineId}/petrinet-instance")
    public ResponseEntity<String> getPetriNetSinkInstanceId(@PathVariable String pipelineId) {
        Pipeline pipeline = pipelineRepository.getPipeline(pipelineId);

        if (pipeline == null) {
            return ResponseEntity.notFound().build();
        }

        // Find the ProcessingElementReference for PetriNetSink
        return pipeline.getProcessingElements().entrySet().stream()
                .filter(entry -> "PetriNetSink".equals(entry.getValue().getTemplateID()))
                .map(Map.Entry::getKey) // instanceId
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{pipelineId}/petri-net")
    public ResponseEntity<String> getPipelinePetriNet(@PathVariable String pipelineId) {
        Pipeline pipeline = pipelineRepository.getPipeline(pipelineId);

        if (pipeline == null) {
            return ResponseEntity.notFound().build();
        }

        // Find the PetriNetSink instanceId in this pipeline
        String instanceId = pipeline.getProcessingElements().entrySet().stream()
                .filter(entry -> "PetriNetSink".equals(entry.getValue().getTemplateID()))
                .map(Map.Entry::getKey) // take instanceId
                .findFirst()
                .orElse(null);

        if (instanceId == null) {
            return ResponseEntity.notFound().build();
        }

        // Use the same logic as your /{instanceId}/petri-net endpoint
        var pe = peInstanceRepository.getInstance(instanceId);

        try {
            var method = pe.getClass().getMethod("getLatestSvg");
            Object svg = method.invoke(pe);
            return ResponseEntity.ok(svg != null ? svg.toString() : "");
        } catch (NoSuchMethodException e) {
            return ResponseEntity.badRequest().body("Instance does not support getLatestSvg()");
        } catch (Exception e) {
            throw new RuntimeException("Error calling getLatestSvg()", e);
        }
    }




}
