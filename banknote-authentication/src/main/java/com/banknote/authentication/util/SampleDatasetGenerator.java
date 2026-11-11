package com.banknote.authentication.util;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Utility to generate calibrated sample genuine Indian banknote images
 * for ₹10, ₹20, ₹50, ₹100, ₹200, ₹500, ₹2000 denominations.
 * Synthesizes currency background hues, Guilloche micro-patterns, security threads,
 * and RBI denomination engravings using OpenCV, with zero-dependency fallback.
 */
public class SampleDatasetGenerator {

    private static final Logger logger = LoggerFactory.getLogger(SampleDatasetGenerator.class);

    // Official dominant colors (BGR) for Indian currency series
    public static final Scalar COLOR_10 = new Scalar(50, 75, 120);     // Chocolate Brown
    public static final Scalar COLOR_20 = new Scalar(60, 150, 160);    // Greenish Yellow
    public static final Scalar COLOR_50 = new Scalar(180, 180, 80);    // Fluorescent Blue
    public static final Scalar COLOR_100 = new Scalar(160, 110, 130);  // Lavender
    public static final Scalar COLOR_200 = new Scalar(50, 140, 230);   // Bright Yellow / Orange
    public static final Scalar COLOR_500 = new Scalar(140, 145, 130);  // Stone Grey
    public static final Scalar COLOR_2000 = new Scalar(130, 70, 170);  // Magenta

    private static volatile boolean openCvInitialized = false;

    public static boolean ensureOpenCVLoaded() {
        if (!openCvInitialized) {
            synchronized (SampleDatasetGenerator.class) {
                if (!openCvInitialized) {
                    try {
                        OpenCV.loadLocally();
                        openCvInitialized = true;
                    } catch (Throwable e) {
                        try {
                            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                            openCvInitialized = true;
                        } catch (Throwable fallbackErr) {
                            logger.warn("OpenCV native library not available: {}", fallbackErr.getMessage());
                            return false;
                        }
                    }
                }
            }
        }
        return openCvInitialized;
    }

    public static void main(String[] args) {
        generateStandardDataset("dataset");
    }

    public static void generateStandardDataset(String baseDirPath) {
        if (!ensureOpenCVLoaded()) {
            logger.info("OpenCV not loaded. Generating dataset via Java AWT fallback...");
            BanknoteDatasetInitializer.generateInitialDatasets(baseDirPath);
            return;
        }

        try {
            logger.info("Generating sample genuine banknote dataset into {}...", baseDirPath);
            String[] denominations = {"10", "20", "50", "100", "200", "500", "2000"};

            String trainBase = baseDirPath + "/train/genuine/";
            String valBase = baseDirPath + "/validation/genuine/";
            String testBase = baseDirPath + "/test/genuine/";

            for (String denom : denominations) {
                new File(trainBase + denom).mkdirs();
                new File(valBase + denom).mkdirs();
                new File(testBase + denom).mkdirs();

                Scalar baseColor = getColorForDenomination(denom);

                // Generate 12 genuine training samples per denomination
                for (int i = 1; i <= 12; i++) {
                    Mat img = createSampleBanknote(denom, baseColor, i, false);
                    Imgcodecs.imwrite(trainBase + denom + "/note_" + denom + "_train_" + i + ".png", img);
                }

                // Generate 4 genuine validation samples per denomination
                for (int i = 1; i <= 4; i++) {
                    Mat img = createSampleBanknote(denom, baseColor, 100 + i, false);
                    Imgcodecs.imwrite(valBase + denom + "/note_" + denom + "_val_" + i + ".png", img);
                }

                // Generate 4 test samples per denomination (3 genuine, 1 anomalous test sample)
                for (int i = 1; i <= 3; i++) {
                    Mat img = createSampleBanknote(denom, baseColor, 200 + i, false);
                    Imgcodecs.imwrite(testBase + denom + "/note_" + denom + "_test_genuine_" + i + ".png", img);
                }
                // 1 anomalous test sample with altered patterns for anomaly detection testing
                Mat anomalyImg = createSampleBanknote(denom, baseColor, 999, true);
                Imgcodecs.imwrite(testBase + denom + "/note_" + denom + "_test_anomaly.png", anomalyImg);
            }

            logger.info("Sample genuine banknote dataset successfully created across all 7 denominations!");
        } catch (Throwable t) {
            logger.warn("OpenCV dataset generation encountered: {}. Falling back to AWT generator...", t.getMessage());
            BanknoteDatasetInitializer.generateInitialDatasets(baseDirPath);
        }
    }

    public static Scalar getColorForDenomination(String denom) {
        if (denom == null) {
            return new Scalar(120, 120, 120);
        }
        switch (denom) {
            case "10":
                return COLOR_10;
            case "20":
                return COLOR_20;
            case "50":
                return COLOR_50;
            case "100":
                return COLOR_100;
            case "200":
                return COLOR_200;
            case "500":
                return COLOR_500;
            case "2000":
                return COLOR_2000;
            default:
                return new Scalar(120, 120, 120);
        }
    }

    /**
     * Synthesize a banknote image with geometric currency security elements.
     */
    public static Mat createSampleBanknote(String denom, Scalar baseColor, int seed, boolean isAnomaly) {
        int w = 440;
        int h = 224;
        Mat mat = new Mat(h, w, CvType.CV_8UC3, baseColor);

        // 1. Banknote Border
        Imgproc.rectangle(mat, new Point(8, 8), new Point(w - 8, h - 8), new Scalar(30, 30, 30), 2);
        Imgproc.rectangle(mat, new Point(14, 14), new Point(w - 14, h - 14), new Scalar(60, 60, 60), 1);

        // 2. Guilloche / Geometric lathe security curves
        Scalar curveColor = new Scalar(
                Math.min(255.0, baseColor.val[0] + 40.0),
                Math.min(255.0, baseColor.val[1] + 40.0),
                Math.min(255.0, baseColor.val[2] + 40.0)
        );
        for (int x = 20; x < w - 20; x += 15) {
            int yOffset = (int) (Math.sin((x + seed) * 0.08) * 35);
            Imgproc.line(mat, new Point(x, 40 + yOffset), new Point(x + 10, h - 40 - yOffset), curveColor, 1);
        }

        // 3. Security Thread (metallic green / dashed line)
        int threadX = (int) (w * 0.42) + (seed % 6);
        Scalar threadColor = isAnomaly ? new Scalar(0, 0, 255) : new Scalar(40, 180, 50);
        for (int y = 16; y < h - 16; y += 14) {
            Imgproc.line(mat, new Point(threadX, y), new Point(threadX, y + 8), threadColor, 3);
        }

        // 4. Watermark Oval Window
        int ovalCenterX = (int) (w * 0.18);
        int ovalCenterY = h / 2;
        Scalar watermarkBg = new Scalar(
                Math.max(0.0, baseColor.val[0] - 25.0),
                Math.max(0.0, baseColor.val[1] - 25.0),
                Math.max(0.0, baseColor.val[2] - 25.0)
        );
        Imgproc.ellipse(mat, new Point(ovalCenterX, ovalCenterY), new Size(35, 55), 0.0, 0.0, 360.0, watermarkBg, -1);
        Imgproc.ellipse(mat, new Point(ovalCenterX, ovalCenterY), new Size(35, 55), 0.0, 0.0, 360.0, new Scalar(180, 180, 180), 1);

        // 5. Reserve Bank of India Header & Denomination Text
        Imgproc.putText(mat, "RESERVE BANK OF INDIA", new Point(w * 0.30, 36),
                Imgproc.FONT_HERSHEY_SIMPLEX, 0.55, new Scalar(20, 20, 20), 2);

        Imgproc.putText(mat, "Rs." + denom, new Point(w * 0.70, h * 0.55),
                Imgproc.FONT_HERSHEY_DUPLEX, 1.2, new Scalar(20, 20, 20), 3);

        // Bleed lines / Identification marks for visually impaired
        int bleedY = 40;
        for (int b = 0; b < 4; b++) {
            Imgproc.line(mat, new Point(10, bleedY + (b * 10)), new Point(22, bleedY + (b * 10)), new Scalar(20, 20, 20), 2);
            Imgproc.line(mat, new Point(w - 22, bleedY + (b * 10)), new Point(w - 10, bleedY + (b * 10)), new Scalar(20, 20, 20), 2);
        }

        // Introduce simulated anomaly if requested
        if (isAnomaly) {
            Imgproc.rectangle(mat, new Point(w * 0.5, h * 0.4), new Point(w * 0.65, h * 0.65), new Scalar(0, 0, 255), -1);
            Imgproc.putText(mat, "COUNTERFEIT", new Point(w * 0.35, h * 0.8),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 0, 255), 2);
        }

        return mat;
    }
}
