package com.banknote.authentication.anomalyDetection;

import com.banknote.authentication.config.ModelConfig;
import com.banknote.authentication.entity.ScreeningResult;
import com.banknote.authentication.imageProcessing.ImagePreprocessingService;
import com.banknote.authentication.service.DeepLearningService;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * One-Class Anomaly Detection Engine using CNN Autoencoder Reconstruction Error.
 * Evaluates visual consistency against learned genuine banknote features.
 */
@Service
public class AnomalyDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(AnomalyDetectionService.class);

    private final DeepLearningService deepLearningService;
    private final MSECalculator mseCalculator;
    private final ModelConfig modelConfig;

    public AnomalyDetectionService(DeepLearningService deepLearningService,
                                   MSECalculator mseCalculator,
                                   ModelConfig modelConfig) {
        this.deepLearningService = deepLearningService;
        this.mseCalculator = mseCalculator;
        this.modelConfig = modelConfig;
    }

    public static class DetectionOutcome {
        private final ScreeningResult result;
        private final double mseScore;
        private final double threshold;
        private final double deviationPercentage;
        private final boolean qualityAcceptable;
        private final double blurScore;
        private final INDArray originalTensor;
        private final INDArray reconstructedTensor;
        private final MSECalculator.MseMetrics detailedMetrics;
        private final String diagnosticMessage;

        public DetectionOutcome(ScreeningResult result, double mseScore, double threshold,
                                double deviationPercentage, boolean qualityAcceptable, double blurScore,
                                INDArray originalTensor, INDArray reconstructedTensor,
                                MSECalculator.MseMetrics detailedMetrics, String diagnosticMessage) {
            this.result = result;
            this.mseScore = mseScore;
            this.threshold = threshold;
            this.deviationPercentage = deviationPercentage;
            this.qualityAcceptable = qualityAcceptable;
            this.blurScore = blurScore;
            this.originalTensor = originalTensor;
            this.reconstructedTensor = reconstructedTensor;
            this.detailedMetrics = detailedMetrics;
            this.diagnosticMessage = diagnosticMessage;
        }

        public ScreeningResult getResult() {
            return result;
        }

        public double getMseScore() {
            return mseScore;
        }

        public double getThreshold() {
            return threshold;
        }

        public double getDeviationPercentage() {
            return deviationPercentage;
        }

        public boolean isQualityAcceptable() {
            return qualityAcceptable;
        }

        public double getBlurScore() {
            return blurScore;
        }

        public INDArray getOriginalTensor() {
            return originalTensor;
        }

        public INDArray getReconstructedTensor() {
            return reconstructedTensor;
        }

        public MSECalculator.MseMetrics getDetailedMetrics() {
            return detailedMetrics;
        }

        public String getDiagnosticMessage() {
            return diagnosticMessage;
        }
    }

    /**
     * Perform one-class anomaly screening on preprocessed banknote image.
     */
    public DetectionOutcome performScreening(ImagePreprocessingService.PreprocessedData preprocessedData) {
        double threshold = deepLearningService.getActiveThreshold();
        double blurScore = preprocessedData.getBlurScore();
        INDArray inputTensor = preprocessedData.getTensor();

        // 1. Image Quality Validation Guard
        if (!preprocessedData.isQualityAcceptable()) {
            logger.warn("Image quality failed screening standards: {}", preprocessedData.getQualityNotes());
            return new DetectionOutcome(
                    ScreeningResult.INCONCLUSIVE,
                    0.0,
                    threshold,
                    0.0,
                    false,
                    blurScore,
                    inputTensor,
                    null,
                    null,
                    "Image screening is inconclusive: " + preprocessedData.getQualityNotes()
            );
        }

        // 2. CNN Autoencoder Reconstruction
        INDArray reconstructedTensor = deepLearningService.reconstruct(inputTensor);

        // 3. Reconstruction Error Calculation (MSE)
        MSECalculator.MseMetrics metrics = mseCalculator.calculateDetailedMse(inputTensor, reconstructedTensor);
        double mse = metrics.getTotalMse();

        // 4. Deviation Percentage relative to calibrated threshold
        double deviation = ((mse - threshold) / threshold) * 100.0;

        // 5. Threshold-based Anomaly Decision
        ScreeningResult decision;
        String diagnostic;

        if (mse <= threshold) {
            decision = ScreeningResult.GENUINE_LIKE;
            diagnostic = String.format("Reconstruction error (%.5f) is within learned genuine bounds (Threshold: %.5f). Visual features are consistent with genuine banknotes.",
                    mse, threshold);
        } else {
            decision = ScreeningResult.ANOMALY_DETECTED;
            diagnostic = String.format("Reconstruction error (%.5f) exceeds calibrated genuine threshold (Threshold: %.5f by +%.1f%%). Unusual visual patterns or anomalies detected.",
                    mse, threshold, deviation);
        }

        logger.info("Screening Result: {} | MSE: {} | Threshold: {} | Deviation: {}%",
                decision, String.format("%.5f", mse), String.format("%.5f", threshold), String.format("%.1f", deviation));

        return new DetectionOutcome(
                decision,
                mse,
                threshold,
                deviation,
                true,
                blurScore,
                inputTensor,
                reconstructedTensor,
                metrics,
                diagnostic
        );
    }
}

