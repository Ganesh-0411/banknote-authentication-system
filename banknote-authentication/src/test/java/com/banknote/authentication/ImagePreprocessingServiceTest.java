package com.banknote.authentication;

import com.banknote.authentication.config.ModelConfig;
import com.banknote.authentication.config.OpenCVConfig;
import com.banknote.authentication.imageProcessing.ImagePreprocessingService;
import nu.pattern.OpenCV;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import static org.junit.jupiter.api.Assertions.*;

public class ImagePreprocessingServiceTest {

    private static ImagePreprocessingService preprocessingService;

    @BeforeAll
    public static void init() {
        OpenCV.loadLocally();
        ModelConfig modelConfig = new ModelConfig();
        OpenCVConfig openCVConfig = new OpenCVConfig();
        preprocessingService = new ImagePreprocessingService(modelConfig, openCVConfig);
    }

    @Test
    public void testPreprocessingOutputsCorrectTensorShape() {
        // Create synthetic 400x200 BGR OpenCV Mat
        Mat raw = new Mat(200, 400, CvType.CV_8UC3, new Scalar(100, 150, 200));

        ImagePreprocessingService.PreprocessedData data = preprocessingService.preprocessImage(raw);
        assertNotNull(data);
        INDArray tensor = data.getTensor();

        // Target shape is [1, 3, 224, 224] (NCHW)
        long[] shape = tensor.shape();
        assertEquals(1, shape[0]);
        assertEquals(3, shape[1]);
        assertEquals(224, shape[2]);
        assertEquals(224, shape[3]);

        // Values should be normalized between 0.0 and 1.0
        float min = tensor.minNumber().floatValue();
        float max = tensor.maxNumber().floatValue();
        assertTrue(min >= 0.0f);
        assertTrue(max <= 1.0f);
    }

    @Test
    public void testTensorToMatConversionRoundtrip() {
        Mat raw = new Mat(224, 224, CvType.CV_8UC3, new Scalar(80, 120, 160));
        ImagePreprocessingService.PreprocessedData data = preprocessingService.preprocessImage(raw);

        Mat reconstructed = preprocessingService.nd4jTensorToBgrMat(data.getTensor());
        assertNotNull(reconstructed);
        assertEquals(224, reconstructed.rows());
        assertEquals(224, reconstructed.cols());
        assertEquals(3, reconstructed.channels());
    }

    @Test
    public void testLaplacianVarianceCalculation() {
        // Flat solid color image has virtually zero variance (blurry / featureless)
        Mat solid = new Mat(100, 100, CvType.CV_8UC3, new Scalar(128, 128, 128));
        double variance = preprocessingService.calculateLaplacianVariance(solid);
        assertEquals(0.0, variance, 0.01);
    }
}

