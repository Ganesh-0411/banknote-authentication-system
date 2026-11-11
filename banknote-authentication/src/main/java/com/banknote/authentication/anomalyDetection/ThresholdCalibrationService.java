package com.banknote.authentication.anomalyDetection;

import com.banknote.authentication.config.ModelConfig;
import com.banknote.authentication.deepLearning.BanknoteDatasetLoader;
import com.banknote.authentication.deepLearning.ModelPersistenceService;
import com.banknote.authentication.entity.ModelInformation;
import com.banknote.authentication.repository.ModelInformationRepository;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service for statistically calibrating the anomaly threshold
 * using genuine validation banknote image reconstruction errors.
 */
@Service
public class ThresholdCalibrationService {

    private static final Logger logger = LoggerFactory.getLogger(ThresholdCalibrationService.class);

    private final MSECalculator mseCalculator;
    private final BanknoteDatasetLoader datasetLoader;
    private final ModelPersistenceService persistenceService;
    private final ModelConfig modelConfig;
    private final ModelInformationRepository modelInfoRepository;

    public ThresholdCalibrationService(MSECalculator mseCalculator,
                                       BanknoteDatasetLoader datasetLoader,
                                       ModelPersistenceService persistenceService,
                                       ModelConfig modelConfig,
                                       ModelInformationRepository modelInfoRepository) {
        this.mseCalculator = mseCalculator;
        this.datasetLoader = datasetLoader;
        this.persistenceService = persistenceService;
        this.modelConfig = modelConfig;
        this.modelInfoRepository = modelInfoRepository;
    }

    public static class CalibrationResult {
        private final double threshold;
        private final double meanMse;
        private final double stdDevMse;
        private final double minMse;
        private final double maxMse;
        private final int sampleCount;

        public CalibrationResult(double threshold, double meanMse, double stdDevMse,
                                 double minMse, double maxMse, int sampleCount) {
            this.threshold = threshold;
            this.meanMse = meanMse;
            this.stdDevMse = stdDevMse;
            this.minMse = minMse;
            this.maxMse = maxMse;
            this.sampleCount = sampleCount;
        }

        public double getThreshold() {
            return threshold;
        }

        public double getMeanMse() {
            return meanMse;
        }

        public double getStdDevMse() {
            return stdDevMse;
        }

        public double getMinMse() {
            return minMse;
        }

        public double getMaxMse() {
            return maxMse;
        }

        public int getSampleCount() {
            return sampleCount;
        }
    }

    /**
     * Calibrate the anomaly detection threshold against genuine validation images.
     * Computes Mean (mu) and Standard Deviation (sigma).
     * Threshold tau = mu + (k * sigma), where k is the standard statistical multiplier (e.g. 2.5 for ~99% coverage).
     *
     * @param model trained CNN Autoencoder
     * @param validationDir directory containing genuine validation images
     * @param kMultiplier standard deviation multiplier (default 2.5)
     * @return CalibrationResult
     */
    public CalibrationResult calibrateFromValidationSet(MultiLayerNetwork model, File validationDir, double kMultiplier) {
        logger.info("Initiating threshold calibration on genuine validation images at: {}", validationDir.getAbsolutePath());

        List<File> validationFiles = datasetLoader.collectImageFiles(validationDir);
        if (validationFiles.isEmpty()) {
            logger.warn("No validation images found in {}. Using configured fallback threshold: {}",
                    validationDir.getAbsolutePath(), modelConfig.getModel().getDefaultThreshold());
            return new CalibrationResult(modelConfig.getModel().getDefaultThreshold(), 0.012, 0.002, 0.008, 0.016, 0);
        }

        List<Double> mseScores = new ArrayList<>();
        double sum = 0.0;
        double min = Double.MAX_VALUE;
        double max = 0.0;

        for (File f : validationFiles) {
            try {
                org.nd4j.linalg.dataset.DataSet ds = datasetLoader.loadAsDataSet(Collections.singletonList(f), 1);
                INDArray input = ds.getFeatures();
                INDArray reconstructed = model.output(input, false);
                double mse = mseCalculator.calculateMse(input, reconstructed);
                mseScores.add(mse);
                sum += mse;
                min = Math.min(min, mse);
                max = Math.max(max, mse);
            } catch (Exception e) {
                logger.warn("Skipping validation file {}: {}", f.getName(), e.getMessage());
            }
        }

        if (mseScores.isEmpty()) {
            return new CalibrationResult(modelConfig.getModel().getDefaultThreshold(), 0.012, 0.002, 0.008, 0.016, 0);
        }

        int n = mseScores.size();
        double mean = sum / n;

        double sumVariance = 0.0;
        for (double val : mseScores) {
            sumVariance += Math.pow(val - mean, 2);
        }
        double stdDev = Math.sqrt(sumVariance / n);

        // Calibrate threshold using Gaussian 99% coverage upper bound: Mean + (k * StdDev)
        double calibratedThreshold = mean + (kMultiplier * stdDev);

        logger.info("Calibration Complete: Genuine Samples: {}, Mean MSE: {}, StdDev: {}, Calibrated Threshold: {}",
                n, String.format("%.6f", mean), String.format("%.6f", stdDev), String.format("%.6f", calibratedThreshold));

        // Persist threshold metadata to disk and update database
        persistenceService.saveThresholdMetadata(
                modelConfig.getModel().getThresholdPath(),
                calibratedThreshold, mean, stdDev, n
        );

        // Update active database model information if present
        modelInfoRepository.findFirstByStatusOrderByTrainingDateDesc("ACTIVE").ifPresent(info -> {
            info.setThreshold(calibratedThreshold);
            info.setGenuineMseMean(mean);
            info.setGenuineMseStdDev(stdDev);
            modelInfoRepository.save(info);
        });

        return new CalibrationResult(calibratedThreshold, mean, stdDev, min, max, n);
    }
}

