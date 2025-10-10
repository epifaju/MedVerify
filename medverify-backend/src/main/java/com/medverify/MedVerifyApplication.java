package com.medverify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application principale MedVerify
 * API Backend pour vérification d'authenticité de médicaments
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class MedVerifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedVerifyApplication.class, args);
    }
}

