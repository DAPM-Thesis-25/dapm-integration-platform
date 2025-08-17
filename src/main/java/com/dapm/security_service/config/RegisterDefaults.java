package com.dapm.security_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegisterDefaults {

    @Bean
    public Object printDefaultOnStartup() {
        return new Object() {
            {
                // This is just to prove the default is present
                try {
                    Class<?> clazz = Class.forName("com.templates.HelloDefault");
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    clazz.getMethod("sayHello").invoke(instance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
