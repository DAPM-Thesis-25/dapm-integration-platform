package com.dapm.security_service.controllers.Client2Api;

import com.dapm.security_service.models.SubscriberOrganization;
import com.dapm.security_service.models.User;
import com.dapm.security_service.models.dtos2.PipelineDto;
import com.dapm.security_service.models.dtos2.UserDto;
import com.dapm.security_service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired private UserRepository userRepository;
    // get all users
    @GetMapping("/all")
    public List<UserDto> getAllSubscriberOrganizations() {
        return userRepository.findAll()
                .stream()
                .map(UserDto::new)
                .toList();
    }
    // only get admins
    @GetMapping("/admins")
    public List<UserDto> getAllAdmins() {
        return userRepository.findAll()
                .stream()
            .filter(pe -> pe.getOrgRole().getName().equals("ADMIN")) // keep only admins
                .map(UserDto::new)
                .toList();
    }
}
