package com.banknote.authentication.dto;

import java.util.Map;

public class DashboardResponse {
    private long totalScans;
    private long genuineLikeScans;
    private long anomalyScans;
    private long inconclusiveScans;
    private double genuineRate;
    private double averageProcessingTimeMs;
    private Map<String, Long> denominationBreakdown;
    private Map<String, Long> resultBreakdown;

    public DashboardResponse() {}

    public long getTotalScans() {
        return totalScans;
    }

    public void setTotalScans(long totalScans) {
        this.totalScans = totalScans;
    }

    public long getGenuineLikeScans() {
        return genuineLikeScans;
    }

    public void setGenuineLikeScans(long genuineLikeScans) {
        this.genuineLikeScans = genuineLikeScans;
    }

    public long getAnomalyScans() {
        return anomalyScans;
    }

    public void setAnomalyScans(long anomalyScans) {
        this.anomalyScans = anomalyScans;
    }

    public long getInconclusiveScans() {
        return inconclusiveScans;
    }

    public void setInconclusiveScans(long inconclusiveScans) {
        this.inconclusiveScans = inconclusiveScans;
    }

    public double getGenuineRate() {
        return genuineRate;
    }

    public void setGenuineRate(double genuineRate) {
        this.genuineRate = genuineRate;
    }

    public double getAverageProcessingTimeMs() {
        return averageProcessingTimeMs;
    }

    public void setAverageProcessingTimeMs(double averageProcessingTimeMs) {
        this.averageProcessingTimeMs = averageProcessingTimeMs;
    }

    public Map<String, Long> getDenominationBreakdown() {
        return denominationBreakdown;
    }

    public void setDenominationBreakdown(Map<String, Long> denominationBreakdown) {
        this.denominationBreakdown = denominationBreakdown;
    }

    public Map<String, Long> getResultBreakdown() {
        return resultBreakdown;
    }

    public void setResultBreakdown(Map<String, Long> resultBreakdown) {
        this.resultBreakdown = resultBreakdown;
    }
}

