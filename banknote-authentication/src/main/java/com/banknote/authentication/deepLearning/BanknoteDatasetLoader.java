package com.banknote.authentication.deepLearning;

import com.banknote.authentication.imageProcessing.ImagePreprocessingService;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Loads genuine Indian banknote images for training and evaluation.
 * Creates ND4J DataSet objects where features == labels (reconstruction task).
 */
@Component
public class BanknoteDatasetLoader {

    private static final Logger logger = LoggerFactory.getLogger(BanknoteDatasetLoader.class);
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".bmp");

    private final ImagePreprocessingService preprocessingService;

    public BanknoteDatasetLoader(ImagePreprocessingService preprocessingService) {
        this.preprocessingService = preprocessingService;
    }

    /**
     * Recursively collect all genuine banknote image files from directory.
     */
    public List<File> collectImageFiles(File baseDir) {
        List<File> imageFiles = new ArrayList<>();
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            logger.warn("Dataset directory does not exist: {}", baseDir.getAbsolutePath());
            return imageFiles;
        }

        File[] files = baseDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    imageFiles.addAll(collectImageFiles(file));
                } else {
                    String name = file.getName().toLowerCase();
                    for (String ext : SUPPORTED_EXTENSIONS) {
                        if (name.endsWith(ext)) {
                            imageFiles.add(file);
                            break;
                        }
                    }
                }
            }
        }
        return imageFiles;
    }

    /**
     * Load image files and convert into a batched ND4J DataSet.
     * Note: In an Autoencoder, the target labels match the input features.
     */
    public DataSet loadAsDataSet(List<File> imageFiles, int maxSamples) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new IllegalArgumentException("No image files provided to load");
        }

        int count = Math.min(imageFiles.size(), maxSamples > 0 ? maxSamples : imageFiles.size());
        logger.info("Loading and preprocessing {} genuine banknote images...", count);

        List<INDArray> tensors = new ArrayList<>();
        int successCount = 0;

        for (int i = 0; i < count; i++) {
            File f = imageFiles.get(i);
            try {
                Mat raw = preprocessingService.loadImage(f.getAbsolutePath());
                ImagePreprocessingService.PreprocessedData data = preprocessingService.preprocessImage(raw);
                tensors.add(data.getTensor());
                successCount++;
            } catch (Exception e) {
                logger.warn("Skipping unreadable or corrupted image: {} (Reason: {})", f.getName(), e.getMessage());
            }
        }

        if (tensors.isEmpty()) {
            throw(new IllegalStateException("Failed to load any valid banknote images from the provided list"));
        }

        // Stack individual [1, 3, 224, 224] tensors into a single [N, 3, 224, 224] tensor
        INDArray features = Nd4j.concat(0, tensors.toArray(new INDArray[0]));
        INDArray labels = features.dup(); // Autoencoder target is input reconstruction

        logger.info("Successfully created ND4J DataSet with shape: {} (Samples: {})", features.shapeInfoToString(), successCount);
        return new DataSet(features, labels);
    }

    /**
     * Convenience method to load from directory path.
     */
    public DataSet loadFromDirectory(String directoryPath, int maxSamples) {
        File dir = new File(directoryPath);
        List<File> files = collectImageFiles(dir);
        Collections.shuffle(files);
        return loadAsDataSet(files, maxSamples);
    }
}

