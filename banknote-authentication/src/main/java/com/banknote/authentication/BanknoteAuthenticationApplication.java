package com.banknote.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot Application for Deep Learning-Based Banknote Authentication
 * System.
 * Integrates OpenCV Java, DeepLearning4J, ND4J, and Spring Data JPA.
 */
@SpringBootApplication
@EnableAsync

public class BanknoteAuthenticationApplication {

    public static void main(String[] args) {
        SpringApplication.run(BanknoteAuthenticationApplication.class, args);
    }
}
