package com.banknote.authentication.anomalyDetection;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.springframework.stereotype.Component;

/**
 * High-performance numerical calculator for Reconstruction Mean Squared Error (MSE)
 * and spatial deviation statistics between original and autoencoder-reconstructed INDArrays.
 */
@Component
public class MSECalculator {

    public static class MseMetrics {
        private final double totalMse;
        private final double redMse;
        private final double greenMse;
        private final double blueMse;
        private final double maxPixelError;

        public MseMetrics(double totalMse, double redMse, double greenMse, double blueMse, double maxPixelError) {
            this.totalMse = totalMse;
            this.redMse = redMse;
            this.greenMse = greenMse;
            this.blueMse = blueMse;
            this.maxPixelError = maxPixelError;
        }

        public double getTotalMse() {
            return totalMse;
        }

        public double getRedMse() {
            return redMse;
        }

        public double getGreenMse() {
            return greenMse;
        }

        public double getBlueMse() {
            return blueMse;
        }

        public double getMaxPixelError() {
            return maxPixelError;
        }
    }

    /**
     * Compute Mean Squared Error between original and reconstructed tensors.
     * MSE = (1 / N) * SUM((original - reconstructed)^2)
     */
    public double calculateMse(INDArray original, INDArray reconstructed) {
        if (original == null || reconstructed == null) {
            throw new IllegalArgumentException("Cannot calculate MSE on null INDArray tensors");
        }

        // Element-wise difference: (original - reconstructed)
        INDArray diff = original.sub(reconstructed);
        // Element-wise square: diff^2
        INDArray squaredDiff = diff.mul(diff);

        // Global mean across all elements
        Number mean = squaredDiff.meanNumber();
        return mean.doubleValue();
    }

    public double calculateMSE(INDArray original, INDArray reconstructed) {
        return calculateMse(original, reconstructed);
    }

    /**
     * Compute detailed channel-wise MSE and peak error metrics.
     */
    public MseMetrics calculateDetailedMse(INDArray original, INDArray reconstructed) {
        int height = (int) original.size(2);
        int width = (int) original.size(3);
        int pixelsPerChannel = height * width;

        double sumR = 0.0, sumG = 0.0, sumB = 0.0;
        double maxError = 0.0;

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                float rOrig = original.getFloat(0, 0, h, w);
                float rRecon = reconstructed.getFloat(0, 0, h, w);
                float diffR = rOrig - rRecon;
                sumR += (diffR * diffR);

                float gOrig = original.getFloat(0, 1, h, w);
                float gRecon = reconstructed.getFloat(0, 1, h, w);
                float diffG = gOrig - gRecon;
                sumG += (diffG * diffG);

                float bOrig = original.getFloat(0, 2, h, w);
                float bRecon = reconstructed.getFloat(0, 2, h, w);
                float diffB = bOrig - bRecon;
                sumB += (diffB * diffB);

                float absDiff = Math.max(Math.abs(diffR), Math.max(Math.abs(diffG), Math.abs(diffB)));
                if (absDiff > maxError) {
                    maxError = absDiff;
                }
            }
        }

        double mseR = sumR / pixelsPerChannel;
        double mseG = sumG / pixelsPerChannel;
        double mseB = sumB / pixelsPerChannel;
        double totalMse = (sumR + sumG + sumB) / (3.0 * pixelsPerChannel);

        return new MseMetrics(totalMse, mseR, mseG, mseB, maxError);
    }
}

