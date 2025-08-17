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
    @EntityGraph(value = "Pipeline.processingElementsAndTokens", type = EntityGraph.EntityGraphType.FETCH)
    List<Pipeline> findAll();

    // Fetch all pipelines, including processingElements & tokens, in one go
    @Query("SELECT DISTINCT p FROM Pipeline p " +
            "LEFT JOIN FETCH p.processingElements " +
            "LEFT JOIN FETCH p.tokens")
    List<Pipeline> findAllWithProcessingElementsAndTokens();

    // Fetch a single pipeline by ID, including processingElements & tokens
    @Query("SELECT DISTINCT p FROM Pipeline p " +
            "LEFT JOIN FETCH p.processingElements " +
            "LEFT JOIN FETCH p.tokens " +
            "WHERE p.id = :id")
    Optional<Pipeline> findByIdWithProcessingElementsAndTokens(@Param("id") UUID id);

    Optional<Pipeline> findByName(String pipelineName);
}
