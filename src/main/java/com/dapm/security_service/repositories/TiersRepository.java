package com.dapm.security_service.repositories;

import com.dapm.security_service.models.Organization;
import com.dapm.security_service.models.Tiers;

import com.dapm.security_service.models.enums.Tier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface TiersRepository extends JpaRepository<Tiers, UUID> {
    Optional<Tiers> findByName(Tier name);
}
