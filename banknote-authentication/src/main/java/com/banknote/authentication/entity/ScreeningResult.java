package com.banknote.authentication.entity;

/**
 * Standard screening outcome states for One-Class Banknote Anomaly Detection.
 * Note: An anomaly indicates visual deviation from learned genuine banknote patterns;
 * it is not an automatic declaration of counterfeiting.
 */
public enum ScreeningResult {
    GENUINE_LIKE("Genuine-Like", "Image is consistent with learned genuine banknote patterns", "#16A34A"),
    ANOMALY_DETECTED("Anomaly Detected", "Image exhibits significant deviation from learned genuine banknote patterns", "#DC2626"),
    INCONCLUSIVE("Inconclusive", "Image quality is blurry, poorly illuminated, or unsupported for reliable analysis", "#EAB308");

    private final String displayName;
    private final String description;
    private final String colorHex;

    ScreeningResult(String displayName, String description, String colorHex) {
        this.displayName = displayName;
        this.description = description;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getColorHex() {
        return colorHex;
    }
}

