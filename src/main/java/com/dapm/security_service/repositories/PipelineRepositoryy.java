package com.dapm.security_service.repositories;

import com.dapm.security_service.models.Pipeline;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PipelineRepositoryy extends JpaRepository<Pipeline, UUID> {

    // Using a defined EntityGraph (ensure you have updated the named entity graph in your Pipeline entity)
    Optional<Pipeline> findByName(String pipelineName);
}
