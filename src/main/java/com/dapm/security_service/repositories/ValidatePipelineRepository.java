package com.dapm.security_service.repositories;

import candidate_validation.ValidatedPipeline;
import com.dapm.security_service.models.models2.ValidatedPipelineConfig;
import org.springframework.stereotype.Repository;
import pipeline.Pipeline;

import java.util.HashMap;
import java.util.Map;
@Repository
public class ValidatePipelineRepository {
    private final Map<String, ValidatedPipelineConfig> pipelines = new HashMap<>();

    public void storePipeline(String pipelineID, ValidatedPipelineConfig pipeline) {
        pipelines.put(pipelineID, pipeline);
    }

    public ValidatedPipelineConfig getPipeline(String pipelineID) {
        return pipelines.get(pipelineID);
    }

    public void removePipeline(String pipelineID) {
        pipelines.remove(pipelineID);
    }
    public boolean pipelineExists(String pipelineID) {
        return pipelines.containsKey(pipelineID);
    }

}

