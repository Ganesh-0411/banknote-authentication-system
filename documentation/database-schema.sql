-- ===================================================================
-- Banknote Authentication System - MySQL 8 Production Database Schema
-- ===================================================================

CREATE DATABASE IF NOT EXISTS banknote_authentication
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE banknote_authentication;

-- 1. USERS Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
) ENGINE=InnoDB;

-- 2. MODEL_INFORMATION Table
CREATE TABLE IF NOT EXISTS model_information (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL,
    model_version VARCHAR(50) NOT NULL UNIQUE,
    model_path VARCHAR(255) NOT NULL,
    training_dataset VARCHAR(255),
    threshold DOUBLE NOT NULL,
    genuine_mse_mean DOUBLE,
    genuine_mse_std_dev DOUBLE,
    architecture_details TEXT,
    input_shape VARCHAR(50) DEFAULT '224x224x3',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    training_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_model_version (model_version)
) ENGINE=InnoDB;

-- 3. BANKNOTE_RECORDS Table
CREATE TABLE IF NOT EXISTS banknote_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    original_filename VARCHAR(255),
    image_path VARCHAR(255) NOT NULL,
    reconstructed_image_path VARCHAR(255),
    heatmap_path VARCHAR(255),
    denomination VARCHAR(20) DEFAULT 'UNKNOWN',
    mse_score DOUBLE NOT NULL,
    anomaly_threshold DOUBLE NOT NULL,
    screening_result VARCHAR(50) NOT NULL,
    processing_time_ms BIGINT,
    blur_score DOUBLE,
    model_version VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_record_user (user_id),
    INDEX idx_record_result (screening_result),
    INDEX idx_record_denomination (denomination),
    CONSTRAINT fk_record_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

