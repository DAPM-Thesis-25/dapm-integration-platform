package com.dapm.security_service.repositories;

import com.dapm.security_service.models.PublisherOrganization;
import com.dapm.security_service.models.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VoucherRepository extends JpaRepository<Voucher, UUID> {
    Optional<Voucher> findByCode(String name);
}
