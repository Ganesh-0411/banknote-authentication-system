package com.banknote.authentication.config;

import com.banknote.authentication.entity.ModelInformation;
import com.banknote.authentication.entity.User;
import com.banknote.authentication.repository.ModelInformationRepository;
import com.banknote.authentication.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Database Seeder that initializes the default Admin and Demo User accounts
 * and model metadata safely using JPA (compatible with both MySQL 8 and H2).
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final ModelInformationRepository modelInfoRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository,
                          ModelInformationRepository modelInfoRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelInfoRepository = modelInfoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Admin User
        if (!userRepository.existsByEmail("admin@banknote.com")) {
            User admin = new User();
            admin.setFullName("System Administrator");
            admin.setEmail("admin@banknote.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole("ROLE_ADMIN");
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
            logger.info("Default Admin account seeded: admin@banknote.com / Admin@123");
        }

        // 2. Seed Standard Demo User
        if (!userRepository.existsByEmail("user@banknote.com")) {
            User user = new User();
            user.setFullName("Officer Rajesh Sharma");
            user.setEmail("user@banknote.com");
            user.setPassword(passwordEncoder.encode("User@123"));
            user.setRole("ROLE_USER");
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
            logger.info("Default User account seeded: user@banknote.com / User@123");
        }

        // 3. Seed Default Model Information
        if (modelInfoRepository.findByModelVersion("v1.0.0").isEmpty()) {
            ModelInformation modelInfo = new ModelInformation();
            modelInfo.setModelName("Indian Banknote CNN Autoencoder");
            modelInfo.setModelVersion("v1.0.0");
            modelInfo.setModelPath("models/banknote-autoencoder.zip");
            modelInfo.setTrainingDataset("Genuine Indian Currency (10, 20, 50, 100, 200, 500)");
            modelInfo.setThreshold(0.01850);
            modelInfo.setGenuineMseMean(0.01210);
            modelInfo.setGenuineMseStdDev(0.00280);
            modelInfo.setArchitectureDetails("Encoder: 3 Conv/Downsample blocks. Decoder: 3 UpSample/Conv blocks, Sigmoid, MSE loss.");
            modelInfo.setInputShape("224x224x3");
            modelInfo.setStatus("ACTIVE");
            modelInfo.setTrainingDate(LocalDateTime.now());
            modelInfoRepository.save(modelInfo);
            logger.info("Default Model Information metadata seeded (v1.0.0).");
        }
    }
}

