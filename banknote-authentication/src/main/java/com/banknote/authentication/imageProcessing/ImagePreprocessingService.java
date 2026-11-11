package com.banknote.authentication.imageProcessing;

import com.banknote.authentication.config.ModelConfig;
import com.banknote.authentication.config.OpenCVConfig;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable OpenCV Java Preprocessing Service.
 * Handles image validation, quality evaluation (Laplacian blur variance),
 * denoising, contrast normalization, 224x224 resizing, ND4J INDArray tensor conversion,
 * reconstructed tensor to Mat conversion, and difference heatmap visualization.
 */
@Service
public class ImagePreprocessingService {

    private static final Logger logger = LoggerFactory.getLogger(ImagePreprocessingService.class);

    private final ModelConfig modelConfig;
    private final OpenCVConfig openCVConfig;

    public ImagePreprocessingService(ModelConfig modelConfig, OpenCVConfig openCVConfig) {
        this.modelConfig = modelConfig;
        this.openCVConfig = openCVConfig;
    }

    /**
     * Data structure holding preprocessing outputs and quality metrics.
     */
    public static class PreprocessedData {
        private final INDArray tensor;       // [1, 3, 224, 224] normalized tensor (NCHW)
        private final Mat processedMat;      // OpenCV 224x224 RGB Mat
        private final double blurScore;      // Laplacian variance
        private final double brightnessMean; // Average luminance
        private final boolean qualityAcceptable;
        private final String qualityNotes;

        public PreprocessedData(INDArray tensor, Mat processedMat, double blurScore,
                                double brightnessMean, boolean qualityAcceptable, String qualityNotes) {
            this.tensor = tensor;
            this.processedMat = processedMat;
            this.blurScore = blurScore;
            this.brightnessMean = brightnessMean;
            this.qualityAcceptable = qualityAcceptable;
            this.qualityNotes = qualityNotes;
        }

        public INDArray getTensor() {
            return tensor;
        }

        public Mat getProcessedMat() {
            return processedMat;
        }

        public double getBlurScore() {
            return blurScore;
        }

        public double getBrightnessMean() {
            return brightnessMean;
        }

        public boolean isQualityAcceptable() {
            return qualityAcceptable;
        }

        public String getQualityNotes() {
            return qualityNotes;
        }
    }

    /**
     * Load image from file path using OpenCV.
     */
    public Mat loadImage(String filePath) {
        File f = new File(filePath);
        if (!f.exists() || !f.canRead()) {
            throw new IllegalArgumentException("Image file does not exist or cannot be read: " + filePath);
        }
        Mat bgr = Imgcodecs.imread(f.getAbsolutePath());
        if (bgr.empty()) {
            throw new IllegalArgumentException("OpenCV failed to decode image: " + filePath);
        }
        return bgr;
    }

    /**
     * Load image from raw byte array (from web upload or camera stream).
     */
    public Mat loadImageFromBytes(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("Empty or null image byte array supplied");
        }
        MatOfByte matOfByte = new MatOfByte(imageBytes);
        Mat bgr = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_COLOR);
        if (bgr.empty()) {
            throw new IllegalArgumentException("OpenCV failed to decode byte array into an image");
        }
        return bgr;
    }

    /**
     * Complete preprocessing pipeline for training and inference:
     * 1. Quality validation (Laplacian blur test, brightness bounds)
     * 2. Resize to 224x224
     * 3. Color conversion BGR -> RGB
     * 4. Subtle Gaussian noise filtering
     * 5. Conversion into ND4J INDArray [1, 3, 224, 224] with [0.0, 1.0] pixel normalization.
     */
    public PreprocessedData preprocessImage(Mat rawBgr) {
        if (rawBgr == null || rawBgr.empty()) {
            throw new IllegalArgumentException("Cannot preprocess null or empty Mat");
        }

        // 1. Calculate image quality metrics
        double blurVariance = calculateLaplacianVariance(rawBgr);
        double brightness = calculateAverageBrightness(rawBgr);

        double blurThreshold = modelConfig.getModel().getBlurVarianceThreshold();
        boolean qualityAcceptable = true;
        StringBuilder notes = new StringBuilder();

        if (blurVariance < blurThreshold) {
            qualityAcceptable = false;
            notes.append(String.format("High blur detected (Variance: %.1f < %.1f). ", blurVariance, blurThreshold));
        }
        if (brightness < 25.0) {
            qualityAcceptable = false;
            notes.append(String.format("Severely underexposed image (Luminance: %.1f). ", brightness));
        } else if (brightness > 240.0) {
            qualityAcceptable = false;
            notes.append(String.format("Severely overexposed image (Luminance: %.1f). ", brightness));
        }

        if (qualityAcceptable) {
            notes.append("Image visual quality passed validation checks.");
        }

        // 2. Resize to target dimensions (224x224)
        int targetW = modelConfig.getModel().getImageWidth();
        int targetH = modelConfig.getModel().getImageHeight();
        Mat resized = new Mat();
        Imgproc.resize(rawBgr, resized, new Size(targetW, targetH), 0, 0,
                rawBgr.width() > targetW ? Imgproc.INTER_AREA : Imgproc.INTER_CUBIC);

        // 3. Convert BGR to RGB
        Mat rgb = new Mat();
        Imgproc.cvtColor(resized, rgb, Imgproc.COLOR_BGR2RGB);

        // 4. Subtle Gaussian filter for noise reduction while preserving banknote micro-features
        Mat denoised = new Mat();
        Imgproc.GaussianBlur(rgb, denoised, new Size(3, 3), 0.5);

        // 5. Convert Mat to ND4J INDArray [1, 3, 224, 224] normalized to [0.0, 1.0]
        INDArray tensor = matToNd4jTensor(denoised);

        return new PreprocessedData(tensor, denoised, blurVariance, brightness, qualityAcceptable, notes.toString());
    }

    /**
     * Convert OpenCV 3-channel RGB Mat into ND4J INDArray of shape [1, 3, height, width]
     * in NCHW format with pixel values normalized in [0.0, 1.0].
     */
    public INDArray matToNd4jTensor(Mat rgbMat) {
        int height = rgbMat.rows();
        int width = rgbMat.cols();
        int channels = rgbMat.channels(); // 3 (R, G, B)

        byte[] byteData = new byte[height * width * channels];
        rgbMat.get(0, 0, byteData);

        // Allocate ND4J array with shape [1, channels, height, width]
        INDArray tensor = Nd4j.create(new int[]{1, channels, height, width}, 'c');

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                int pixelIdx = (h * width + w) * channels;
                float r = (byteData[pixelIdx] & 0xFF) / 255.0f;
                float g = (byteData[pixelIdx + 1] & 0xFF) / 255.0f;
                float b = (byteData[pixelIdx + 2] & 0xFF) / 255.0f;

                tensor.putScalar(new int[]{0, 0, h, w}, r);
                tensor.putScalar(new int[]{0, 1, h, w}, g);
                tensor.putScalar(new int[]{0, 2, h, w}, b);
            }
        }

        return tensor;
    }

    /**
     * Convert ND4J INDArray tensor of shape [1, 3, height, width] back into OpenCV BGR Mat (0-255).
     */
    public Mat nd4jTensorToBgrMat(INDArray tensor) {
        int channels = (int) tensor.size(1);
        int height = (int) tensor.size(2);
        int width = (int) tensor.size(3);

        Mat bgrMat = new Mat(height, width, CvType.CV_8UC3);
        byte[] bgrBytes = new byte[height * width * 3];

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                float r = tensor.getFloat(0, 0, h, w);
                float g = tensor.getFloat(0, 1, h, w);
                float b = tensor.getFloat(0, 2, h, w);

                // Clamp values between 0.0 and 1.0
                r = Math.max(0.0f, Math.min(1.0f, r));
                g = Math.max(0.0f, Math.min(1.0f, g));
                b = Math.max(0.0f, Math.min(1.0f, b));

                int bByte = Math.round(b * 255.0f);
                int gByte = Math.round(g * 255.0f);
                int rByte = Math.round(r * 255.0f);

                int idx = (h * width + w) * 3;
                bgrBytes[idx] = (byte) bByte;
                bgrBytes[idx + 1] = (byte) gByte;
                bgrBytes[idx + 2] = (byte) rByte;
            }
        }

        bgrMat.put(0, 0, bgrBytes);
        return bgrMat;
    }

    /**
     * Generate an absolute difference error heatmap between original preprocessed image
     * and autoencoder reconstructed image.
     * Uses OpenCV COLORMAP_JET to visually highlight regions of high reconstruction error.
     */
    public Mat generateErrorHeatmap(INDArray originalTensor, INDArray reconstructedTensor) {
        int height = (int) originalTensor.size(2);
        int width = (int) originalTensor.size(3);

        Mat diffGray = new Mat(height, width, CvType.CV_8UC1);
        byte[] grayBytes = new byte[height * width];

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                float diffR = Math.abs(originalTensor.getFloat(0, 0, h, w) - reconstructedTensor.getFloat(0, 0, h, w));
                float diffG = Math.abs(originalTensor.getFloat(0, 1, h, w) - reconstructedTensor.getFloat(0, 1, h, w));
                float diffB = Math.abs(originalTensor.getFloat(0, 2, h, w) - reconstructedTensor.getFloat(0, 2, h, w));

                float meanDiff = (diffR + diffG + diffB) / 3.0f;
                // Amplify difference for visual clarity in false-color heatmap
                int intensity = Math.min(255, Math.round(meanDiff * 4.0f * 255.0f));
                grayBytes[h * width + w] = (byte) intensity;
            }
        }
        diffGray.put(0, 0, grayBytes);

        Mat heatmap = new Mat();
        Imgproc.applyColorMap(diffGray, heatmap, Imgproc.COLORMAP_JET);
        return heatmap;
    }

    /**
     * Compute image blurriness metric using the variance of the Laplacian filter.
     */
    public double calculateLaplacianVariance(Mat bgrMat) {
        Mat gray = new Mat();
        Imgproc.cvtColor(bgrMat, gray, Imgproc.COLOR_BGR2GRAY);

        Mat laplacian = new Mat();
        Imgproc.Laplacian(gray, laplacian, CvType.CV_64F);

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(laplacian, mean, stddev);

        double std = stddev.get(0, 0)[0];
        return std * std;
    }

    /**
     * Compute average luminance of the image.
     */
    public double calculateAverageBrightness(Mat bgrMat) {
        Mat gray = new Mat();
        Imgproc.cvtColor(bgrMat, gray, Imgproc.COLOR_BGR2GRAY);
        Scalar meanScalar = Core.mean(gray);
        return meanScalar.val[0];
    }

    /**
     * Save an OpenCV Mat to disk as PNG/JPG.
     */
    public boolean saveMatToFile(Mat mat, String destinationPath) {
        File file = new File(destinationPath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        return Imgcodecs.imwrite(file.getAbsolutePath(), mat);
    }
}

