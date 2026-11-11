package com.banknote.authentication.training;

import com.banknote.authentication.anomalyDetection.MSECalculator;
import com.banknote.authentication.anomalyDetection.ThresholdCalibrationService;
import com.banknote.authentication.config.ModelConfig;
import com.banknote.authentication.config.OpenCVConfig;
import com.banknote.authentication.deepLearning.*;
import com.banknote.authentication.imageProcessing.ImagePreprocessingService;
import com.banknote.authentication.util.SampleDatasetGenerator;
import nu.pattern.OpenCV;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.dataset.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Standalone Training Runner CLI.
 * Can be run from command line or IDE to train the DL4J CNN Autoencoder
 * on genuine Indian currency images, evaluate validation loss, calibrate threshold,
 * and save models/banknote-autoencoder.zip + models/threshold.json.
 */
public class TrainAutoencoderRunner {

    private static final Logger logger = LoggerFactory.getLogger(TrainAutoencoderRunner.class);

    public static void main(String[] args) {
        logger.info("=================================================================");
        logger.info("  STARTING BANKNOTE CNN AUTOENCODER TRAINING & CALIBRATION CLI  ");
        logger.info("=================================================================");

        // 1. Initialize native OpenCV
        OpenCV.loadLocally();

        File trainDir = new File("dataset/train/genuine/");
        File valDir = new File("dataset/validation/genuine/");
        File testDir = new File("dataset/test/genuine/");

        // Auto-generate sample dataset if not present
        if (!trainDir.exists() || trainDir.listFiles() == null || trainDir.listFiles().length == 0) {
            logger.info("Dataset directories empty. Generating calibrated genuine banknote sample dataset...");
            SampleDatasetGenerator.generateStandardDataset("dataset");
        }

        // 2. Setup modules
        ModelConfig modelConfig = new ModelConfig();
        modelConfig.initDirectories();
        OpenCVConfig openCVConfig = new OpenCVConfig();

        ImagePreprocessingService preprocessingService = new ImagePreprocessingService(modelConfig, openCVConfig);
        BanknoteDatasetLoader datasetLoader = new BanknoteDatasetLoader(preprocessingService);
        MSECalculator mseCalculator = new MSECalculator();
        ModelPersistenceService persistenceService = new ModelPersistenceService();
        BanknoteAutoencoder autoencoderArchitecture = new BanknoteAutoencoder();

        try {
            // 3. Load genuine training images
            List<File> trainFiles = datasetLoader.collectImageFiles(trainDir);
            logger.info("Found {} genuine training banknote images.", trainFiles.size());
            DataSet trainData = datasetLoader.loadAsDataSet(trainFiles, -1);

            // 4. Build CNN Autoencoder
            MultiLayerNetwork network = autoencoderArchitecture.buildNetwork(0.001, 12345L);
            network.setListeners(new ScoreIterationListener(5));

            // 5. Train Autoencoder
            int epochs = (args.length > 0) ? Integer.parseInt(args[0]) : 10;
            logger.info("Training for {} epochs on genuine Indian banknote dataset...", epochs);

            for (int ep = 1; ep <= epochs; ep++) {
                network.fit(trainData);
                logger.info("Epoch {}/{} completed. Reconstruction MSE: {}", ep, epochs, String.format("%.6f", network.score()));
            }

            // 6. Save model to models/banknote-autoencoder.zip
            String modelPath = "models/banknote-autoencoder.zip";
            persistenceService.saveModel(network, modelPath, true);
            logger.info("Saved trained model to: {}", modelPath);

            // 7. Calibrate Threshold on genuine validation images
            List<File> valFiles = datasetLoader.collectImageFiles(valDir);
            logger.info("Found {} genuine validation banknote images for threshold calibration.", valFiles.size());

            double sumValMse = 0.0;
            int countVal = 0;
            double[] valMses = new double[valFiles.size()];

            for (File vf : valFiles) {
                DataSet vds = datasetLoader.loadAsDataSet(List.of(vf), 1);
                var input = vds.getFeatures();
                var recon = network.output(input, false);
                double mse = mseCalculator.calculateMse(input, recon);
                valMses[countVal] = mse;
                sumValMse += mse;
                countVal++;
            }

            double meanMse = (countVal > 0) ? sumValMse / countVal : 0.012;
            double sumVar = 0.0;
            for (int i = 0; i < countVal; i++) {
                sumVar += Math.pow(valMses[i] - meanMse, 2);
            }
            double stdDevMse = (countVal > 0) ? Math.sqrt(sumVar / countVal) : 0.002;

            // Statistically calibrated threshold: Mean + (2.5 * StdDev)
            double threshold = meanMse + (2.5 * stdDevMse);

            logger.info("-----------------------------------------------------------------");
            logger.info("THRESHOLD CALIBRATION SUMMARY:");
            logger.info("Genuine Samples Evaluated: {}", countVal);
            logger.info("Mean Reconstruction MSE:   {}", String.format("%.6f", meanMse));
            logger.info("MSE Standard Deviation:    {}", String.format("%.6f", stdDevMse));
            logger.info("CALIBRATED THRESHOLD:      {}", String.format("%.6f", threshold));
            logger.info("-----------------------------------------------------------------");

            persistenceService.saveThresholdMetadata("models/threshold.json", threshold, meanMse, stdDevMse, countVal);

            // 8. Evaluate on test set
            BanknoteModelEvaluator evaluator = new BanknoteModelEvaluator(datasetLoader, mseCalculator);
            BanknoteModelEvaluator.EvaluationReport report = evaluator.evaluate(network, testDir, threshold);

            logger.info("-----------------------------------------------------------------");
            logger.info("TEST SET EVALUATION REPORT:");
            logger.info("Test Images Evaluated:     {}", report.getTotalImages());
            logger.info("Genuine Samples Accepted:  {}", report.getGenuineAccepted());
            logger.info("False Anomalies Flagged:   {}", report.getFalseAnomalies());
            logger.info("Genuine Acceptance Rate:   {}%", String.format("%.2f", report.getGenuineAcceptanceRate()));
            logger.info("=================================================================");
            logger.info("TRAINING PIPELINE COMPLETE! Model is ready for web deployment.");

        } catch (Exception e) {
            logger.error("Training execution encountered error: {}", e.getMessage(), e);
        }
    }
}

