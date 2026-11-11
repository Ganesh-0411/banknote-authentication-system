package com.banknote.authentication.deepLearning;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Service for serializing and deserializing DeepLearning4J CNN Autoencoder models
 * and accompanying calibration parameters.
 */
@Service
public class ModelPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(ModelPersistenceService.class);

    /**
     * Save trained DL4J model to ZIP archive.
     */
    public synchronized void saveModel(MultiLayerNetwork model, String targetFilePath, boolean saveUpdater) throws IOException {
        File targetFile = new File(targetFilePath);
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        logger.info("Serializing DL4J CNN Autoencoder to: {}", targetFile.getAbsolutePath());
        ModelSerializer.writeModel(model, targetFile, saveUpdater);
        logger.info("Model saved successfully! File size: {} bytes", targetFile.length());
    }

    /**
     * Load trained DL4J model from ZIP archive.
     */
    public synchronized MultiLayerNetwork loadModel(String modelFilePath) throws IOException {
        File modelFile = new File(modelFilePath);
        if (!modelFile.exists()) {
            throw new IOException("Model archive not found at path: " + modelFile.getAbsolutePath());
        }
        logger.info("Restoring DL4J CNN Autoencoder from: {}", modelFile.getAbsolutePath());
        MultiLayerNetwork network = ModelSerializer.restoreMultiLayerNetwork(modelFile);
        logger.info("Model restored successfully. Number of layers: {}", network.getnLayers());
        return network;
    }

    /**
     * Check if a model file exists and is readable.
     */
    public boolean modelExists(String modelFilePath) {
        File file = new File(modelFilePath);
        return file.exists() && file.isFile() && file.length() > 0;
    }

    /**
     * Save calibrated threshold statistics to JSON.
     */
    public void saveThresholdMetadata(String jsonPath, double threshold, double mean, double stdDev, int sampleCount) {
        try {
            File f = new File(jsonPath);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            String json = String.format("{\n" +
                    "  \"threshold\": %.6f,\n" +
                    "  \"genuineMean\": %.6f,\n" +
                    "  \"genuineStdDev\": %.6f,\n" +
                    "  \"sampleCount\": %d,\n" +
                    "  \"calibratedAt\": \"%s\"\n" +
                    "}", threshold, mean, stdDev, sampleCount, java.time.LocalDateTime.now());

            try (FileWriter writer = new FileWriter(f)) {
                writer.write(json);
            }
            logger.info("Saved calibration threshold metadata to: {}", f.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to write threshold metadata to {}: {}", jsonPath, e.getMessage());
        }
    }
}

