package com.dapm.security_service;

import candidate_validation.ValidatedPipeline;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.core.io.ClassPathResource;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;

//@EnableAsync
//@SpringBootApplication
//@EntityScan(basePackages = "com.dapm.security_service.models")
//@ComponentScan(basePackages = {"controller", "pipeline", "communication", "repository"})
@EnableAsync
@SpringBootApplication(scanBasePackages = {
		"com.dapm.security_service",   // keep scanning your app so Swagger & controllers load
		"candidate_validation",
		"communication",
		"controller",
		"exceptions",
		"pipeline",
		"repository",
		"utils"
})
@EntityScan(basePackages = {
		"com.dapm.security_service.models"  // add more here only if your JPA entities live elsewhere
})
public class SecurityServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityServiceApplication.class, args);




	}



}
