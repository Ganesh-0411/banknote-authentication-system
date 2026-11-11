package com.banknote.authentication.deepLearning;

import com.banknote.authentication.anomalyDetection.ThresholdCalibrationService;
import com.banknote.authentication.config.ModelConfig;
import com.banknote.authentication.entity.ModelInformation;
import com.banknote.authentication.repository.ModelInformationRepository;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.dataset.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service to manage the end-to-end training pipeline for the DL4J CNN Autoencoder
 * on genuine Indian banknote images.
 */
@Service
public class BanknoteModelTrainer {

    private static final Logger logger = LoggerFactory.getLogger(BanknoteModelTrainer.class);

    private final BanknoteAutoencoder autoencoderArchitecture;
    private final BanknoteDatasetLoader datasetLoader;
    private final ModelPersistenceService persistenceService;
    private final ThresholdCalibrationService calibrationService;
    private final ModelConfig modelConfig;
    private final ModelInformationRepository modelInfoRepository;

    public BanknoteModelTrainer(BanknoteAutoencoder autoencoderArchitecture,
                                BanknoteDatasetLoader datasetLoader,
                                ModelPersistenceService persistenceService,
                                ThresholdCalibrationService calibrationService,
                                ModelConfig modelConfig,
                                ModelInformationRepository modelInfoRepository) {
        this.autoencoderArchitecture = autoencoderArchitecture;
        this.datasetLoader = datasetLoader;
        this.persistenceService = persistenceService;
        this.calibrationService = calibrationService;
        this.modelConfig = modelConfig;
        this.modelInfoRepository = modelInfoRepository;
    }

    public static class TrainingSummary {
        private final boolean success;
        private final String message;
        private final int epochsCompleted;
        private final double finalLoss;
        private final double calibratedThreshold;
        private final String modelPath;

        public TrainingSummary(boolean success, String message, int epochsCompleted,
                               double finalLoss, double calibratedThreshold, String modelPath) {
            this.success = success;
            this.message = message;
            this.epochsCompleted = epochsCompleted;
            this.finalLoss = finalLoss;
            this.calibratedThreshold = calibratedThreshold;
            this.modelPath = modelPath;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getEpochsCompleted() {
            return epochsCompleted;
        }

        public double getFinalLoss() {
            return finalLoss;
        }

        public double getCalibratedThreshold() {
            return calibratedThreshold;
        }

        public String getModelPath() {
            return modelPath;
        }
    }

    /**
     * Train CNN Autoencoder on genuine banknote images.
     *
     * @param trainDir directory of genuine training banknotes
     * @param validationDir directory of genuine validation banknotes
     * @param epochs number of training epochs
     * @param learningRate Adam optimizer learning rate
     * @param modelVersion version tag (e.g. v1.0.1)
     */
    public TrainingSummary train(File trainDir, File validationDir, int epochs, double learningRate, String modelVersion) {
        logger.info("Beginning DL4J CNN Autoencoder training pipeline on genuine banknote images...");

        List<File> trainFiles = datasetLoader.collectImageFiles(trainDir);
        if (trainFiles.isEmpty()) {
            return new TrainingSummary(false, "No genuine banknote training images found in: " + trainDir.getAbsolutePath(),
                    0, 0, 0, null);
        }

        try {
            // 1. Load training dataset
            DataSet trainData = datasetLoader.loadAsDataSet(trainFiles, -1);

            // 2. Build CNN Autoencoder
            MultiLayerNetwork model = autoencoderArchitecture.buildNetwork(learningRate, 12345L);
            model.setListeners(new ScoreIterationListener(10));

            // 3. Training Loop
            double bestScore = Double.MAX_VALUE;
            logger.info("Starting training loop for {} epochs on {} samples...", epochs, trainFiles.size());

            for (int epoch = 1; epoch <= epochs; epoch++) {
                model.fit(trainData);
                double currentScore = model.score();
                logger.info("Epoch {}/{} completed. Reconstruction MSE Score: {}", epoch, epochs, String.format("%.6f", currentScore));
                if (currentScore < bestScore) {
                    bestScore = currentScore;
                }
            }

            // 4. Save Model Checkpoint
            String savePath = modelConfig.getModel().getPath();
            persistenceService.saveModel(model, savePath, true);

            // 5. Threshold Calibration using genuine validation dataset
            ThresholdCalibrationService.CalibrationResult calibResult =
                    calibrationService.calibrateFromValidationSet(model, validationDir, 2.5);

            // 6. Record Model Metadata in Database
            ModelInformation modelInfo = new ModelInformation();
            modelInfo.setModelName("Indian Banknote CNN Autoencoder");
            modelInfo.setModelVersion(modelVersion != null ? modelVersion : "v" + System.currentTimeMillis());
            modelInfo.setModelPath(savePath);
            modelInfo.setTrainingDataset(String.format("Genuine Banknotes (%d samples)", trainFiles.size()));
            modelInfo.setThreshold(calibResult.getThreshold());
            modelInfo.setGenuineMseMean(calibResult.getMeanMse());
            modelInfo.setGenuineMseStdDev(calibResult.getStdDevMse());
            modelInfo.setArchitectureDetails("CNN Autoencoder: 3 Conv/Downsample blocks + 3 UpSample/Conv blocks, MSE Loss, Adam.");
            modelInfo.setStatus("ACTIVE");
            modelInfo.setTrainingDate(LocalDateTime.now());
            modelInfoRepository.save(modelInfo);

            logger.info("Training and calibration pipeline successfully completed!");
            return new TrainingSummary(true, "Training and threshold calibration completed successfully.",
                    epochs, bestScore, calibResult.getThreshold(), savePath);

        } catch (Exception e) {
            logger.error("Error occurred during model training: {}", e.getMessage(), e);
            return new TrainingSummary(false, "Training failed: " + e.getMessage(), 0, 0, 0, null);
        }
    }
}

