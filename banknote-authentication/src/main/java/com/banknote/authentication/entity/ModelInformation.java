package com.banknote.authentication.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "model_information")
public class ModelInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "model_version", nullable = false, unique = true, length = 50)
    private String modelVersion;

    @Column(name = "model_path", nullable = false)
    private String modelPath;

    @Column(name = "training_dataset")
    private String trainingDataset;

    @Column(name = "threshold", nullable = false)
    private Double threshold;

    @Column(name = "genuine_mse_mean")
    private Double genuineMseMean;

    @Column(name = "genuine_mse_std_dev")
    private Double genuineMseStdDev;

    @Column(name = "architecture_details", columnDefinition = "TEXT")
    private String architectureDetails;

    @Column(name = "input_shape", length = 50)
    private String inputShape = "224x224x3";

    @Column(name = "status", nullable = false, length = 50)
    private String status = "ACTIVE";

    @Column(name = "training_date")
    private LocalDateTime trainingDate = LocalDateTime.now();

    public ModelInformation() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public String getTrainingDataset() {
        return trainingDataset;
    }

    public void setTrainingDataset(String trainingDataset) {
        this.trainingDataset = trainingDataset;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Double getGenuineMseMean() {
        return genuineMseMean;
    }

    public void setGenuineMseMean(Double genuineMseMean) {
        this.genuineMseMean = genuineMseMean;
    }

    public Double getGenuineMseStdDev() {
        return genuineMseStdDev;
    }

    public void setGenuineMseStdDev(Double genuineMseStdDev) {
        this.genuineMseStdDev = genuineMseStdDev;
    }

    public String getArchitectureDetails() {
        return architectureDetails;
    }

    public void setArchitectureDetails(String architectureDetails) {
        this.architectureDetails = architectureDetails;
    }

    public String getInputShape() {
        return inputShape;
    }

    public void setInputShape(String inputShape) {
        this.inputShape = inputShape;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDateTime trainingDate) {
        this.trainingDate = trainingDate;
    }
}

