package com.dapm.security_service.repositories;

import com.dapm.security_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserDetailsRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
}
