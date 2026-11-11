package com.banknote.authentication.deepLearning;

import com.banknote.authentication.anomalyDetection.MSECalculator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Evaluates CNN Autoencoder reconstruction fidelity and genuine acceptance metrics.
 */
@Service
public class BanknoteModelEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(BanknoteModelEvaluator.class);

    private final BanknoteDatasetLoader datasetLoader;
    private final MSECalculator mseCalculator;

    public BanknoteModelEvaluator(BanknoteDatasetLoader datasetLoader, MSECalculator mseCalculator) {
        this.datasetLoader = datasetLoader;
        this.mseCalculator = mseCalculator;
    }

    public static class EvaluationReport {
        private final int totalImages;
        private final int genuineAccepted;
        private final int falseAnomalies;
        private final double genuineAcceptanceRate;
        private final double meanMse;
        private final double minMse;
        private final double maxMse;
        private final double threshold;

        public EvaluationReport(int totalImages, int genuineAccepted, int falseAnomalies,
                                double genuineAcceptanceRate, double meanMse, double minMse, double maxMse, double threshold) {
            this.totalImages = totalImages;
            this.genuineAccepted = genuineAccepted;
            this.falseAnomalies = falseAnomalies;
            this.genuineAcceptanceRate = genuineAcceptanceRate;
            this.meanMse = meanMse;
            this.minMse = minMse;
            this.maxMse = maxMse;
            this.threshold = threshold;
        }

        public int getTotalImages() {
            return totalImages;
        }

        public int getGenuineAccepted() {
            return genuineAccepted;
        }

        public int getFalseAnomalies() {
            return falseAnomalies;
        }

        public double getGenuineAcceptanceRate() {
            return genuineAcceptanceRate;
        }

        public double getMeanMse() {
            return meanMse;
        }

        public double getMinMse() {
            return minMse;
        }

        public double getMaxMse() {
            return maxMse;
        }

        public double getThreshold() {
            return threshold;
        }
    }

    /**
     * Evaluate model against an independent set of genuine banknote test images.
     */
    public EvaluationReport evaluate(MultiLayerNetwork model, File testDir, double threshold) {
        List<File> testFiles = datasetLoader.collectImageFiles(testDir);
        if (testFiles.isEmpty()) {
            return new EvaluationReport(0, 0, 0, 0.0, 0.0, 0.0, 0.0, threshold);
        }

        int accepted = 0;
        int flaggedAnomaly = 0;
        double sumMse = 0.0;
        double minMse = Double.MAX_VALUE;
        double maxMse = 0.0;

        for (File f : testFiles) {
            try {
                DataSet ds = datasetLoader.loadAsDataSet(Collections.singletonList(f), 1);
                INDArray input = ds.getFeatures();
                INDArray reconstructed = model.output(input, false);
                double mse = mseCalculator.calculateMse(input, reconstructed);

                sumMse += mse;
                minMse = Math.min(minMse, mse);
                maxMse = Math.max(maxMse, mse);

                if (mse <= threshold) {
                    accepted++;
                } else {
                    flaggedAnomaly++;
                }
            } catch (Exception e) {
                logger.warn("Evaluation skip on {}: {}", f.getName(), e.getMessage());
            }
        }

        int total = accepted + flaggedAnomaly;
        double meanMse = total > 0 ? sumMse / total : 0.0;
        double acceptanceRate = total > 0 ? ((double) accepted / total) * 100.0 : 0.0;

        logger.info("Evaluation completed. Total: {}, Accepted Genuine: {}, Flagged Anomaly: {}, Rate: {}%",
                total, accepted, flaggedAnomaly, String.format("%.2f", acceptanceRate));

        return new EvaluationReport(total, accepted, flaggedAnomaly, acceptanceRate, meanMse, minMse, maxMse, threshold);
    }
}

