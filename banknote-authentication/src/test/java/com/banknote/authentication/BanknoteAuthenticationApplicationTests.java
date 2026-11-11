package com.banknote.authentication;

import com.banknote.authentication.anomalyDetection.MSECalculator;
import com.banknote.authentication.entity.ScreeningResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BanknoteAuthenticationApplicationTests {

    @Test
    @DisplayName("Verify Spring Boot application context loads successfully")
    void contextLoads() {
        // Confirms all beans, repositories, services, and configurations initialize
    }

    @Test
    @DisplayName("Verify ND4J MSE Calculator computes exact reconstruction loss")
    void testMSECalculation() {
        MSECalculator calculator = new MSECalculator();

        INDArray original = Nd4j.create(new float[]{1.0f, 0.5f, 0.0f, 0.25f}, new int[]{1, 4});
        INDArray identical = Nd4j.create(new float[]{1.0f, 0.5f, 0.0f, 0.25f}, new int[]{1, 4});

        double mseIdentical = calculator.calculateMse(original, identical);
        assertEquals(0.0, mseIdentical, 1e-6, "MSE between identical matrices must be 0");

        INDArray deviated = Nd4j.create(new float[]{0.0f, 0.5f, 0.0f, 0.25f}, new int[]{1, 4});
        double mseDeviated = calculator.calculateMse(original, deviated);
        assertEquals(0.25, mseDeviated, 1e-6, "MSE calculation must match mathematical expectation");
    }

    @Test
    @DisplayName("Verify ScreeningResult enum states")
    void testScreeningResultEnum() {
        assertEquals("Genuine-Like", ScreeningResult.GENUINE_LIKE.getDisplayName());
        assertEquals("Anomaly Detected", ScreeningResult.ANOMALY_DETECTED.getDisplayName());
        assertEquals("Inconclusive", ScreeningResult.INCONCLUSIVE.getDisplayName());
    }
}
