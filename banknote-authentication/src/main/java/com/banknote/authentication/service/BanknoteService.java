package com.banknote.authentication.service;

import com.banknote.authentication.anomalyDetection.AnomalyDetectionService;
import com.banknote.authentication.config.ModelConfig;
import com.banknote.authentication.dto.AnalysisResponse;
import com.banknote.authentication.entity.BanknoteRecord;
import com.banknote.authentication.entity.ScreeningResult;
import com.banknote.authentication.entity.User;
import com.banknote.authentication.imageProcessing.ImagePreprocessingService;
import com.banknote.authentication.repository.BanknoteRepository;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Service
public class BanknoteService {

    private static final Logger logger = LoggerFactory.getLogger(BanknoteService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ImagePreprocessingService preprocessingService;
    private final AnomalyDetectionService anomalyDetectionService;
    private final DeepLearningService deepLearningService;
    private final BanknoteRepository banknoteRepository;
    private final ModelConfig modelConfig;

    public BanknoteService(ImagePreprocessingService preprocessingService,
                           AnomalyDetectionService anomalyDetectionService,
                           DeepLearningService deepLearningService,
                           BanknoteRepository banknoteRepository,
                           ModelConfig modelConfig) {
        this.preprocessingService = preprocessingService;
        this.anomalyDetectionService = anomalyDetectionService;
        this.deepLearningService = deepLearningService;
        this.banknoteRepository = banknoteRepository;
        this.modelConfig = modelConfig;
    }

    /**
     * Process an uploaded banknote image file.
     */
    @Transactional
    public AnalysisResponse processBanknote(MultipartFile file, String denomination, User currentUser) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No banknote image file was uploaded");
        }
        byte[] bytes = file.getBytes();
        String originalFilename = file.getOriginalFilename();
        return executeScreeningPipeline(bytes, originalFilename, denomination, currentUser);
    }

    /**
     * Process a camera capture submitted as Base64 string.
     */
    @Transactional
    public AnalysisResponse processBase64Capture(String base64Image, String denomination, User currentUser) throws IOException {
        if (base64Image == null || base64Image.isBlank()) {
            throw new IllegalArgumentException("Empty camera capture data received");
        }
        String cleanBase64 = base64Image;
        if (cleanBase64.contains(",")) {
            cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
        }
        byte[] bytes = Base64.getDecoder().decode(cleanBase64);
        String filename = "camera_capture_" + System.currentTimeMillis() + ".jpg";
        return executeScreeningPipeline(bytes, filename, denomination, currentUser);
    }

    /**
     * Core execution pipeline:
     * 1. Save original image to uploads/
     * 2. OpenCV decode and validation
     * 3. OpenCV preprocessing & tensor conversion
     * 4. Anomaly detection via CNN Autoencoder & MSE calculation
     * 5. Generate reconstructed image & error heatmap
     * 6. Save records to MySQL
     */
    private AnalysisResponse executeScreeningPipeline(byte[] imageBytes, String originalFilename,
                                                      String denomination, User currentUser) throws IOException {
        long startTime = System.currentTimeMillis();
        String uniqueId = UUID.randomUUID().toString().substring(0, 12);

        // 1. Save original file
        String originalExt = (originalFilename != null && originalFilename.contains(".")) ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String originalRelPath = "uploads/orig_" + uniqueId + originalExt;
        File origFile = new File(originalRelPath);
        File parent = origFile.getParentFile();
        if (parent != null && !parent.exists()) origFile.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(origFile)) {
            fos.write(imageBytes);
        }

        // 2. Decode using OpenCV
        Mat rawBgr = preprocessingService.loadImageFromBytes(imageBytes);

        // 3. Preprocess
        ImagePreprocessingService.PreprocessedData preprocessedData = preprocessingService.preprocessImage(rawBgr);

        // 4. Run Anomaly Detection screening
        AnomalyDetectionService.DetectionOutcome outcome = anomalyDetectionService.performScreening(preprocessedData);

        String reconstructedRelPath = null;
        String heatmapRelPath = null;

        // 5. Generate Reconstructed Image & Difference Heatmap if reconstruction took place
        if (outcome.getReconstructedTensor() != null) {
            Mat reconstructedBgr = preprocessingService.nd4jTensorToBgrMat(outcome.getReconstructedTensor());
            reconstructedRelPath = modelConfig.getReconstructedDir() + "recon_" + uniqueId + ".png";
            preprocessingService.saveMatToFile(reconstructedBgr, reconstructedRelPath);

            Mat heatmap = preprocessingService.generateErrorHeatmap(outcome.getOriginalTensor(), outcome.getReconstructedTensor());
            heatmapRelPath = modelConfig.getHeatmapDir() + "diff_" + uniqueId + ".png";
            preprocessingService.saveMatToFile(heatmap, heatmapRelPath);
        }

        long processingTime = System.currentTimeMillis() - startTime;

        // 6. Persist to MySQL
        BanknoteRecord record = new BanknoteRecord();
        record.setUser(currentUser);
        record.setOriginalFilename(originalFilename);
        record.setImagePath(originalRelPath);
        record.setReconstructedImagePath(reconstructedRelPath);
        record.setHeatmapPath(heatmapRelPath);
        record.setDenomination(denomination != null && !denomination.isBlank() ? denomination : "UNKNOWN");
        record.setMseScore(outcome.getMseScore());
        record.setAnomalyThreshold(outcome.getThreshold());
        record.setScreeningResult(outcome.getResult());
        record.setProcessingTimeMs(processingTime);
        record.setBlurScore(outcome.getBlurScore());
        record.setModelVersion(deepLearningService.getActiveModelVersion());
        record.setNotes(outcome.getDiagnosticMessage());
        banknoteRepository.save(record);

        // 7. Assemble AnalysisResponse DTO
        AnalysisResponse response = new AnalysisResponse();
        response.setId(record.getId());
        response.setScreeningResult(outcome.getResult());
        response.setMseScore(outcome.getMseScore());
        response.setAnomalyThreshold(outcome.getThreshold());
        response.setDeviationPercentage(outcome.getDeviationPercentage());
        response.setProcessingTimeMs(processingTime);
        response.setBlurScore(outcome.getBlurScore());
        response.setQualityAcceptable(outcome.isQualityAcceptable());
        response.setDenomination(record.getDenomination());
        response.setOriginalImageUrl("/" + originalRelPath.replace("\\", "/"));
        if (reconstructedRelPath != null) {
            response.setReconstructedImageUrl("/" + reconstructedRelPath.replace("\\", "/"));
        }
        if (heatmapRelPath != null) {
            response.setHeatmapImageUrl("/" + heatmapRelPath.replace("\\", "/"));
        }
        response.setModelVersion(record.getModelVersion());
        response.setTimestamp(record.getCreatedAt().format(FORMATTER));

        return response;
    }

    public BanknoteRecord getRecordById(Long id) {
        return banknoteRepository.findById(id).orElse(null);
    }
}

