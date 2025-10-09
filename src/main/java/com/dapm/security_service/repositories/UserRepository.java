package com.dapm.security_service.repositories;

import com.dapm.security_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    // find all admins
//    java.util.List<User> findByIsAdminTrue();
}
