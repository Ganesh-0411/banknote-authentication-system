package com.banknote.authentication;

import com.banknote.authentication.anomalyDetection.MSECalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import static org.junit.jupiter.api.Assertions.*;

public class MSECalculatorTest {

    private MSECalculator mseCalculator;

    @BeforeEach
    public void setup() {
        mseCalculator = new MSECalculator();
    }

    @Test
    public void testZeroMseOnIdenticalTensors() {
        INDArray tensorA = Nd4j.ones(1, 3, 224, 224);
        INDArray tensorB = Nd4j.ones(1, 3, 224, 224);

        double mse = mseCalculator.calculateMse(tensorA, tensorB);
        assertEquals(0.0, mse, 0.000001, "Identical tensors should produce exactly 0.0 MSE");
    }

    @Test
    public void testMseCalculationAccuracy() {
        // Create tensor of 1.0s and tensor of 0.5s -> difference is 0.5 -> squared difference is 0.25
        INDArray tensorA = Nd4j.ones(1, 3, 224, 224);
        INDArray tensorB = Nd4j.valueArrayOf(new int[]{1, 3, 224, 224}, 0.5f);

        double mse = mseCalculator.calculateMse(tensorA, tensorB);
        assertEquals(0.25, mse, 0.0001, "MSE should match expected theoretical value of 0.25");
    }

    @Test
    public void testDetailedMseMetrics() {
        INDArray tensorA = Nd4j.ones(1, 3, 224, 224);
        INDArray tensorB = Nd4j.valueArrayOf(new int[]{1, 3, 224, 224}, 0.8f);

        MSECalculator.MseMetrics metrics = mseCalculator.calculateDetailedMse(tensorA, tensorB);
        assertNotNull(metrics);
        assertTrue(metrics.getTotalMse() > 0);
        assertEquals(0.2, metrics.getMaxPixelError(), 0.001);
    }
}

