package com.dapm.security_service.controllers.PeerApi;

import com.dapm.security_service.services.TokenVerificationService;
import communication.API.request.PEInstanceRequest;
import communication.API.response.PEInstanceResponse;
import communication.ConsumerFactory;
import communication.ProducerFactory;
import communication.config.ConsumerConfig;
import communication.config.ProducerConfig;
import communication.message.Message;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pipeline.processingelement.Sink;
import pipeline.processingelement.operator.Operator;
import pipeline.processingelement.source.Source;
import repository.PEInstanceRepository;
import repository.TemplateRepository;
import utils.IDGenerator;
import utils.JsonUtil;

import java.util.Map;

@RestController
@RequestMapping("/pipelineBuilder")
public class PipelineBuilderController {
    @Value("${organization.broker.port}")
    private String brokerURL;

    private final ProducerFactory producerFactory;
    private final ConsumerFactory consumerFactory;
    private final TemplateRepository templateRepository;
    private final PEInstanceRepository peInstanceRepository;
    @Autowired private TokenVerificationService tokenVerificationService;

    @Autowired
    public PipelineBuilderController(TemplateRepository templateRepository,
                                     PEInstanceRepository peInstanceRepository,
                                     ConsumerFactory consumerFactory,
                                     ProducerFactory producerFactory) {
        this.templateRepository = templateRepository;
        this.peInstanceRepository = peInstanceRepository;
        this.consumerFactory = consumerFactory;
        this.producerFactory = producerFactory;
    }

    @PostMapping("/source/templateID/{templateID}")
    public ResponseEntity<?> configureSource(@PathVariable("templateID") String templateID, @RequestBody PEInstanceRequest requestBody) {
//        boolean isValid = tokenVerificationService.verifyTokenIssuedByMe(requestBody.getToken());
//        if (!isValid) {
//            return ResponseEntity.status(401)
//                    .body(Map.of("error", "Invalid or expired token"));
//        }
        Claims claims;
        try {
            claims = tokenVerificationService.verifyAndExtractClaims(requestBody.getToken());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }

        // ✅ enforce that this token approves this specific template
        String approvedTemplateId = claims.get("peTemplateId", String.class);
        String decodedTemplateID = JsonUtil.decode(templateID);
        if (!decodedTemplateID.equals(approvedTemplateId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Token not valid for this PE"));
        }

        Source<Message> source = templateRepository.createInstanceFromTemplate(
                decodedTemplateID,
                requestBody.getConfiguration());
        System.out.println(source);

        if (source != null) {
            String topic = IDGenerator.generateTopic();
            ProducerConfig producerConfig = new ProducerConfig(brokerURL, topic);
            System.out.println(producerConfig.brokerURL());
            producerFactory.registerProducer(source, producerConfig);
            String instanceID = peInstanceRepository.storeInstance(source);

            return ResponseEntity.ok(new PEInstanceResponse
                    .Builder(decodedTemplateID, instanceID)
                    .producerConfig(producerConfig)
                    .build());
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping("/operator/templateID/{templateID}")
    public ResponseEntity<?> createOperator(@PathVariable("templateID") String templateID, @RequestBody PEInstanceRequest requestBody) {
        Claims claims;
        try {
            claims = tokenVerificationService.verifyAndExtractClaims(requestBody.getToken());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }

        // ✅ enforce that this token approves this specific template
        String approvedTemplateId = claims.get("peTemplateId", String.class);
        String decodedTemplateID = JsonUtil.decode(templateID);
        if (!decodedTemplateID.equals(approvedTemplateId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Token not valid for this PE"));
        }
        Operator<Message, Message> operator = templateRepository.createInstanceFromTemplate(
                decodedTemplateID,
                requestBody.getConfiguration());

        if (operator != null) {
            for (ConsumerConfig config : requestBody.getConsumerConfigs()) {
                consumerFactory.registerConsumer(operator, config);
            }
            String topic = IDGenerator.generateTopic();
            ProducerConfig producerConfig = new ProducerConfig(brokerURL, topic);
            producerFactory.registerProducer(operator, producerConfig);

            String instanceID = peInstanceRepository.storeInstance(operator);
            return ResponseEntity.ok(new PEInstanceResponse
                    .Builder(decodedTemplateID, instanceID)
                    .producerConfig(producerConfig)
                    .build());
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping("/sink/templateID/{templateID}")
    public ResponseEntity<?> createSink(@PathVariable("templateID") String templateID, @RequestBody PEInstanceRequest requestBody) {
        Claims claims;
        try {
            claims = tokenVerificationService.verifyAndExtractClaims(requestBody.getToken());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }

        // ✅ enforce that this token approves this specific template
        String approvedTemplateId = claims.get("peTemplateId", String.class);
        String decodedTemplateID = JsonUtil.decode(templateID);
        if (!decodedTemplateID.equals(approvedTemplateId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Token not valid for this PE"));
        }
        Sink sink = templateRepository.createInstanceFromTemplate(
                decodedTemplateID,
                requestBody.getConfiguration());

        if (sink != null) {
            for (ConsumerConfig config : requestBody.getConsumerConfigs()) {
                consumerFactory.registerConsumer(sink, config);
            }
            String instanceID = peInstanceRepository.storeInstance(sink);
            return ResponseEntity.ok(new PEInstanceResponse
                    .Builder(decodedTemplateID, instanceID)
                    .build());
        }
        return ResponseEntity.badRequest().body(null);
    }
}
