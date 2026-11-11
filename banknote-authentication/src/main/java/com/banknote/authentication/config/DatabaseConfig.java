package com.banknote.authentication.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.banknote.authentication.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // Standard JPA and MySQL transaction management configuration
}

