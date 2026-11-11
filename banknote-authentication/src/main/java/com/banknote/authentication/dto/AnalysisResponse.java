package com.banknote.authentication.dto;

import com.banknote.authentication.entity.ScreeningResult;

public class AnalysisResponse {

    private Long id;
    private ScreeningResult screeningResult;
    private String resultDisplayName;
    private String resultColor;
    private String resultDescription;
    private Double mseScore;
    private Double anomalyThreshold;
    private Double deviationPercentage;
    private Long processingTimeMs;
    private Double blurScore;
    private boolean qualityAcceptable;
    private String denomination;
    private String originalImageUrl;
    private String reconstructedImageUrl;
    private String heatmapImageUrl;
    private String modelVersion;
    private String timestamp;
    private String disclaimer = "This application uses a Deep Learning-based image screening approach trained on genuine banknote images. " +
            "The result indicates similarity to learned genuine image patterns and does not establish official banknote authenticity. " +
            "An anomaly requires further verification.";

    public AnalysisResponse() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ScreeningResult getScreeningResult() {
        return screeningResult;
    }

    public void setScreeningResult(ScreeningResult screeningResult) {
        this.screeningResult = screeningResult;
        if (screeningResult != null) {
            this.resultDisplayName = screeningResult.getDisplayName();
            this.resultColor = screeningResult.getColorHex();
            this.resultDescription = screeningResult.getDescription();
        }
    }

    public String getResultDisplayName() {
        return resultDisplayName;
    }

    public void setResultDisplayName(String resultDisplayName) {
        this.resultDisplayName = resultDisplayName;
    }

    public String getResultColor() {
        return resultColor;
    }

    public void setResultColor(String resultColor) {
        this.resultColor = resultColor;
    }

    public String getResultDescription() {
        return resultDescription;
    }

    public void setResultDescription(String resultDescription) {
        this.resultDescription = resultDescription;
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

    public Double getDeviationPercentage() {
        return deviationPercentage;
    }

    public void setDeviationPercentage(Double deviationPercentage) {
        this.deviationPercentage = deviationPercentage;
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

    public boolean isQualityAcceptable() {
        return qualityAcceptable;
    }

    public void setQualityAcceptable(boolean qualityAcceptable) {
        this.qualityAcceptable = qualityAcceptable;
    }

    public String getDenomination() {
        return denomination;
    }

    public void setDenomination(String denomination) {
        this.denomination = denomination;
    }

    public String getOriginalImageUrl() {
        return originalImageUrl;
    }

    public void setOriginalImageUrl(String originalImageUrl) {
        this.originalImageUrl = originalImageUrl;
    }

    public String getReconstructedImageUrl() {
        return reconstructedImageUrl;
    }

    public void setReconstructedImageUrl(String reconstructedImageUrl) {
        this.reconstructedImageUrl = reconstructedImageUrl;
    }

    public String getHeatmapImageUrl() {
        return heatmapImageUrl;
    }

    public void setHeatmapImageUrl(String heatmapImageUrl) {
        this.heatmapImageUrl = heatmapImageUrl;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}

