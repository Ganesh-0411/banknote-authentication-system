package com.banknote.authentication.service;

import com.banknote.authentication.config.ModelConfig;
import com.banknote.authentication.deepLearning.BanknoteAutoencoder;
import com.banknote.authentication.deepLearning.ModelPersistenceService;
import com.banknote.authentication.entity.ModelInformation;
import com.banknote.authentication.repository.ModelInformationRepository;
import jakarta.annotation.PostConstruct;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Singleton Deep Learning Inference Service.
 * Ensures the DL4J CNN Autoencoder is loaded once into memory
 * and reused efficiently for thread-safe banknote screening inference.
 */
@Service
public class DeepLearningService {

    private static final Logger logger = LoggerFactory.getLogger(DeepLearningService.class);

    private final BanknoteAutoencoder autoencoderArchitecture;
    private final ModelPersistenceService persistenceService;
    private final ModelConfig modelConfig;
    private final ModelInformationRepository modelInfoRepository;

    private MultiLayerNetwork loadedModel;
    private double activeThreshold;
    private String activeModelVersion = "v1.0.0";
    private boolean modelReady = false;

    public DeepLearningService(BanknoteAutoencoder autoencoderArchitecture,
                               ModelPersistenceService persistenceService,
                               ModelConfig modelConfig,
                               ModelInformationRepository modelInfoRepository) {
        this.autoencoderArchitecture = autoencoderArchitecture;
        this.persistenceService = persistenceService;
        this.modelConfig = modelConfig;
        this.modelInfoRepository = modelInfoRepository;
    }

    @PostConstruct
    public void init() {
        modelConfig.initDirectories();
        loadOrInitializeModel();
    }

    /**
     * Load trained model from disk or initialize fresh architecture if not yet trained.
     */
    public synchronized void loadOrInitializeModel() {
        String modelPath = modelConfig.getModel().getPath();
        activeThreshold = modelConfig.getModel().getDefaultThreshold();

        // Check database for active threshold
        try {
            Optional<ModelInformation> activeInfo = modelInfoRepository.findFirstByStatusOrderByTrainingDateDesc("ACTIVE");
            if (activeInfo.isPresent()) {
                activeThreshold = activeInfo.get().getThreshold();
                activeModelVersion = activeInfo.get().getModelVersion();
                logger.info("Retrieved active model threshold from database: {} (Version: {})",
                        activeThreshold, activeModelVersion);
            }
        } catch (Exception e) {
            logger.warn("Could not query model_information table at boot: {}", e.getMessage());
        }

        File modelFile = new File(modelPath);
        if (modelFile.exists() && modelFile.length() > 0) {
            try {
                logger.info("Loading persisted DL4J CNN Autoencoder from: {}", modelFile.getAbsolutePath());
                this.loadedModel = persistenceService.loadModel(modelPath);
                this.modelReady = true;
                logger.info("Trained CNN Autoencoder loaded successfully into memory.");
                return;
            } catch (IOException e) {
                logger.error("Failed to load model file at {}: {}. Initializing baseline model.", modelPath, e.getMessage());
            }
        }

        // Initialize baseline network so application is immediately functional
        logger.info("No pre-trained model file found at {}. Initializing baseline CNN Autoencoder architecture...", modelPath);
        this.loadedModel = autoencoderArchitecture.buildNetwork(0.001, 42L);
        this.modelReady = true;
        try {
            persistenceService.saveModel(this.loadedModel, modelPath, false);
            logger.info("Saved initial baseline CNN Autoencoder model to {}", modelPath);
        } catch (Exception e) {
            logger.warn("Could not persist initial baseline model: {}", e.getMessage());
        }
    }

    /**
     * Thread-safe forward pass through the CNN Autoencoder to reconstruct an input banknote INDArray.
     *
     * @param inputTensor shape [1, 3, 224, 224]
     * @return reconstructed INDArray shape [1, 3, 224, 224]
     */
    public synchronized INDArray reconstruct(INDArray inputTensor) {
        if (!modelReady || loadedModel == null) {
            throw new IllegalStateException("DeepLearning CNN Autoencoder model is not loaded or initialized");
        }
        // Output from CNN Autoencoder (inference mode, training=false)
        return loadedModel.output(inputTensor, false);
    }

    public double getActiveThreshold() {
        return activeThreshold;
    }

    public void setActiveThreshold(double activeThreshold) {
        this.activeThreshold = activeThreshold;
    }

    public String getActiveModelVersion() {
        return activeModelVersion;
    }

    public boolean isModelReady() {
        return modelReady;
    }

    public MultiLayerNetwork getLoadedModel() {
        return loadedModel;
    }
}

