package com.banknote.authentication.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "banknote_records")
public class BanknoteRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "reconstructed_image_path")
    private String reconstructedImagePath;

    @Column(name = "heatmap_path")
    private String heatmapPath;

    @Column(name = "denomination", length = 20)
    private String denomination = "UNKNOWN";

    @Column(name = "mse_score", nullable = false)
    private Double mseScore;

    @Column(name = "anomaly_threshold", nullable = false)
    private Double anomalyThreshold;

    @Enumerated(EnumType.STRING)
    @Column(name = "screening_result", nullable = false, length = 50)
    private ScreeningResult screeningResult;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "blur_score")
    private Double blurScore;

    @Column(name = "model_version", length = 50)
    private String modelVersion = "v1.0.0";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public BanknoteRecord() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getReconstructedImagePath() {
        return reconstructedImagePath;
    }

    public void setReconstructedImagePath(String reconstructedImagePath) {
        this.reconstructedImagePath = reconstructedImagePath;
    }

    public String getHeatmapPath() {
        return heatmapPath;
    }

    public void setHeatmapPath(String heatmapPath) {
        this.heatmapPath = heatmapPath;
    }

    public String getDenomination() {
        return denomination;
    }

    public void setDenomination(String denomination) {
        this.denomination = denomination;
    }

    public Double getMseScore() {
        return mseScore;
    }

    public void setMseScore(Double mseScore) {
        this.mseScore = mseScore;
    }

    public Double getAnomalyThreshold() {
        return anomalyThreshold;
    }

    public void setAnomalyThreshold(Double anomalyThreshold) {
        this.anomalyThreshold = anomalyThreshold;
    }

    public ScreeningResult getScreeningResult() {
        return screeningResult;
    }

    public void setScreeningResult(ScreeningResult screeningResult) {
        this.screeningResult = screeningResult;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public Double getBlurScore() {
        return blurScore;
    }

    public void setBlurScore(Double blurScore) {
        this.blurScore = blurScore;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

