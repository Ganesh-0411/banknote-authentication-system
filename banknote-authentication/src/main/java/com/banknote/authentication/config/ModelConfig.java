package com.banknote.authentication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@ConfigurationProperties(prefix = "banknote")
public class ModelConfig {

    public static final int TARGET_WIDTH = 224;
    public static final int TARGET_HEIGHT = 224;
    public static final int TARGET_CHANNELS = 3;

    private ModelSettings model = new ModelSettings();
    private String uploadDir = "uploads/";
    private String reconstructedDir = "uploads/reconstructed/";
    private String heatmapDir = "uploads/heatmaps/";

    public static class ModelSettings {
        private String path = "models/banknote-autoencoder.zip";
        private String thresholdPath = "models/threshold.json";
        private double defaultThreshold = 0.01850;
        private int imageWidth = 224;
        private int imageHeight = 224;
        private int channels = 3;
        private double blurVarianceThreshold = 100.0;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getThresholdPath() {
            return thresholdPath;
        }

        public void setThresholdPath(String thresholdPath) {
            this.thresholdPath = thresholdPath;
        }

        public double getDefaultThreshold() {
            return defaultThreshold;
        }

        public void setDefaultThreshold(double defaultThreshold) {
            this.defaultThreshold = defaultThreshold;
        }

        public int getImageWidth() {
            return imageWidth;
        }

        public void setImageWidth(int imageWidth) {
            this.imageWidth = imageWidth;
        }

        public int getImageHeight() {
            return imageHeight;
        }

        public void setImageHeight(int imageHeight) {
            this.imageHeight = imageHeight;
        }

        public int getChannels() {
            return channels;
        }

        public void setChannels(int channels) {
            this.channels = channels;
        }

        public double getBlurVarianceThreshold() {
            return blurVarianceThreshold;
        }

        public void setBlurVarianceThreshold(double blurVarianceThreshold) {
            this.blurVarianceThreshold = blurVarianceThreshold;
        }
    }

    public ModelSettings getModel() {
        return model;
    }

    public void setModel(ModelSettings model) {
        this.model = model;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getReconstructedDir() {
        return reconstructedDir;
    }

    public void setReconstructedDir(String reconstructedDir) {
        this.reconstructedDir = reconstructedDir;
    }

    public String getHeatmapDir() {
        return heatmapDir;
    }

    public void setHeatmapDir(String heatmapDir) {
        this.heatmapDir = heatmapDir;
    }

    public void initDirectories() {
        new File(uploadDir).mkdirs();
        new File(reconstructedDir).mkdirs();
        new File(heatmapDir).mkdirs();
        new File("models/").mkdirs();
        new File("dataset/train/genuine/").mkdirs();
        new File("dataset/validation/genuine/").mkdirs();
        new File("dataset/test/genuine/").mkdirs();
    }
}

