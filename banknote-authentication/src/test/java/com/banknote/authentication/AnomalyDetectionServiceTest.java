package com.banknote.authentication;

import com.banknote.authentication.anomalyDetection.AnomalyDetectionService;
import com.banknote.authentication.anomalyDetection.MSECalculator;
import com.banknote.authentication.config.ModelConfig;
import com.banknote.authentication.entity.ScreeningResult;
import com.banknote.authentication.imageProcessing.ImagePreprocessingService;
import com.banknote.authentication.service.DeepLearningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.opencv.core.Mat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AnomalyDetectionServiceTest {

    private DeepLearningService deepLearningService;
    private MSECalculator mseCalculator;
    private ModelConfig modelConfig;
    private AnomalyDetectionService anomalyDetectionService;

    @BeforeEach
    public void setup() {
        deepLearningService = Mockito.mock(DeepLearningService.class);
        mseCalculator = new MSECalculator();
        modelConfig = new ModelConfig();
        anomalyDetectionService = new AnomalyDetectionService(deepLearningService, mseCalculator, modelConfig);
    }

    @Test
    public void testInconclusiveWhenQualityNotAcceptable() {
        INDArray tensor = Nd4j.zeros(1, 3, 224, 224);
        ImagePreprocessingService.PreprocessedData badQuality =
                new ImagePreprocessingService.PreprocessedData(tensor, new Mat(), 10.0, 50.0, false, "Severe blur");

        AnomalyDetectionService.DetectionOutcome outcome = anomalyDetectionService.performScreening(badQuality);
        assertEquals(ScreeningResult.INCONCLUSIVE, outcome.getResult());
        assertFalse(outcome.isQualityAcceptable());
    }

    @Test
    public void testGenuineLikeWhenMseBelowThreshold() {
        when(deepLearningService.getActiveThreshold()).thenReturn(0.020);

        INDArray inputTensor = Nd4j.valueArrayOf(new int[]{1, 3, 224, 224}, 0.5f);
        // Reconstructed tensor very close (diff = 0.05 -> MSE = 0.0025 <= 0.020)
        INDArray reconstructed = Nd4j.valueArrayOf(new int[]{1, 3, 224, 224}, 0.55f);
        when(deepLearningService.reconstruct(any())).thenReturn(reconstructed);

        ImagePreprocessingService.PreprocessedData goodQuality =
                new ImagePreprocessingService.PreprocessedData(inputTensor, new Mat(), 150.0, 120.0, true, "OK");

        AnomalyDetectionService.DetectionOutcome outcome = anomalyDetectionService.performScreening(goodQuality);
        assertEquals(ScreeningResult.GENUINE_LIKE, outcome.getResult());
        assertTrue(outcome.getMseScore() <= outcome.getThreshold());
    }

    @Test
    public void testAnomalyDetectedWhenMseAboveThreshold() {
        when(deepLearningService.getActiveThreshold()).thenReturn(0.015);

        INDArray inputTensor = Nd4j.valueArrayOf(new int[]{1, 3, 224, 224}, 0.1f);
        // Large reconstruction error (diff = 0.4 -> MSE = 0.16 > 0.015)
        INDArray reconstructed = Nd4j.valueArrayOf(new int[]{1, 3, 224, 224}, 0.5f);
        when(deepLearningService.reconstruct(any())).thenReturn(reconstructed);

        ImagePreprocessingService.PreprocessedData goodQuality =
                new ImagePreprocessingService.PreprocessedData(inputTensor, new Mat(), 150.0, 120.0, true, "OK");

        AnomalyDetectionService.DetectionOutcome outcome = anomalyDetectionService.performScreening(goodQuality);
        assertEquals(ScreeningResult.ANOMALY_DETECTED, outcome.getResult());
        assertTrue(outcome.getMseScore() > outcome.getThreshold());
    }
}

