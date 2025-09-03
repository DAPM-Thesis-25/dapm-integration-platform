package com.dapm.security_service.controllers.Client2Api;

import candidate_validation.ValidatedPipeline;
import com.dapm.security_service.models.ProcessingElement;
import com.dapm.security_service.models.dtos2.designpipeline.DesignPipelineDto;
import com.dapm.security_service.models.dtos2.designpipeline.DesignProcessingElementDto;
import com.dapm.security_service.models.dtos2.validatepipeline.ValidateChannelDto;
import com.dapm.security_service.models.dtos2.validatepipeline.ValidatePipelineDto;
import com.dapm.security_service.models.dtos2.validatepipeline.ValidateProcessingElementDto;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/designPipeline")
public class ValidatePipelineController {

    @Autowired private ProcessingElementRepository processingElementRepository;
    @Value("${runtime.configs.root:/runtime-configs}")
    private String rootDir;

    @PostMapping("/design")
    public ResponseEntity<ValidatePipelineDto> createProject(
            @RequestBody DesignPipelineDto designPipelineDto
    ) throws JsonProcessingException {
        System.out.println("Received pipeline design: " + designPipelineDto);
        ValidatePipelineDto validatePipelineDto = convertToValidatePipelineDto(designPipelineDto);
        String contents = convertToString(validatePipelineDto);

        System.out.println(contents+" I am ii validate");

        String cfgRoot = System.getenv().getOrDefault("runtime.configs.root", "/runtime-configs");
        java.net.URI configURI = java.nio.file.Paths.get(cfgRoot).toUri();
        String pipelineID = "orgC_pipeline";

        ValidatedPipeline validatedPipeline = new ValidatedPipeline(contents, configURI);
        System.out.println(validatedPipeline+ "I am here");
        return ResponseEntity.ok(validatePipelineDto);
    }

    private ValidatePipelineDto convertToValidatePipelineDto(DesignPipelineDto designPipelineDto) {
        // iterate over the prcessing elements to convert them
        List<ValidateProcessingElementDto> validateProcessingElements = designPipelineDto.getProcessingElements().stream()
                .map(this::convertToValidateProcessingElementDto)
                .toList();

        // 2. Convert Channels
        List<ValidateChannelDto> validateChannels = designPipelineDto.getChannels().stream()
                .map(channel -> {
                    ValidateChannelDto validateChannel = new ValidateChannelDto();

                    // convert publisher
                    validateChannel.setPublisher(
                            convertToValidateProcessingElementDto(channel.getPublisher())
                    );

                    // convert subscribers
                    List<ValidateChannelDto.SubscriberDto> subscribers = channel.getSubscribers().stream()
                            .map(sub -> {
                                ValidateChannelDto.SubscriberDto s = new ValidateChannelDto.SubscriberDto();
                                s.setPortNumber(sub.getPortNumber());
                                s.setProcessingElement(
                                        convertToValidateProcessingElementDto(sub.getProcessingElement())
                                );
                                return s;
                            })
                            .toList();

                    validateChannel.setSubscribers(subscribers);
                    return validateChannel;
                })
                .toList();


        ValidatePipelineDto validatePipelineDto = new ValidatePipelineDto();
        validatePipelineDto.setProcessingElements(validateProcessingElements);
        validatePipelineDto.setChannels(validateChannels);
        // Conversion logic here
        return validatePipelineDto;
    }

    private ValidateProcessingElementDto convertToValidateProcessingElementDto(DesignProcessingElementDto designProcessingElementDto) {
        ProcessingElement pe = processingElementRepository.findByTemplateId(designProcessingElementDto.getTemplateID())
                .orElseThrow(() -> new RuntimeException("Processing element not found with template Id: " + designProcessingElementDto.getTemplateID()));

        ValidateProcessingElementDto validateProcessingElementDto= new ValidateProcessingElementDto();
        validateProcessingElementDto.setConfiguration(designProcessingElementDto.getConfiguration());
        validateProcessingElementDto.setTemplateID(designProcessingElementDto.getTemplateID());
        validateProcessingElementDto.setOutput(pe.getOutput());
        validateProcessingElementDto.setInputs(pe.getInputs());
        validateProcessingElementDto.setHostURL(pe.getHostURL());
        validateProcessingElementDto.setOrganizationID(
                pe.getOwnerOrganization() != null ?
                        pe.getOwnerOrganization().getName() :
                        pe.getOwnerPartnerOrganization().getName()
        );
        validateProcessingElementDto.setInstanceNumber(pe.getInstanceNumber()+1);

        return validateProcessingElementDto;
    }

    public String convertToString(ValidatePipelineDto validatePipelineDto) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(validatePipelineDto);
    }


}
