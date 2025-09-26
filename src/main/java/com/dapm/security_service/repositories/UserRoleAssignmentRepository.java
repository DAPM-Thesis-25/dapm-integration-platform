package com.dapm.security_service.repositories;

import java.util.List;
import java.util.Optional;

import com.dapm.security_service.models.Project;
import com.dapm.security_service.models.User;
import com.dapm.security_service.models.UserRoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, Long> {
    Optional<UserRoleAssignment> findByUserAndProject(User user, Project project);
    List<UserRoleAssignment> findByUser(User user);

    List<UserRoleAssignment> findByProject(Project project);


}
