package com.banknote.authentication.config;

import jakarta.annotation.PostConstruct;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenCVConfig {

    private static final Logger logger = LoggerFactory.getLogger(OpenCVConfig.class);
    private static volatile boolean loaded = false;

    @PostConstruct
    public void initOpenCV() {
        if (!loaded) {
            synchronized (OpenCVConfig.class) {
                if (!loaded) {
                    try {
                        logger.info("Initializing OpenCV Java native libraries via OpenPnP loader...");
                        OpenCV.loadLocally();
                        loaded = true;
                        logger.info("OpenCV Java initialized successfully! Native OpenCV Version: {}", Core.VERSION);
                    } catch (Throwable e) {
                        logger.warn("OpenCV.loadLocally() encountered note: {}. Attempting fallback System.loadLibrary...", e.getMessage());
                        try {
                            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                            loaded = true;
                            logger.info("OpenCV native library loaded successfully via System.loadLibrary: {}", Core.VERSION);
                        } catch (Throwable fallbackErr) {
                            logger.error("Failed to initialize OpenCV native libraries: {}", fallbackErr.getMessage());
                        }
                    }
                }
            }
        }
    }

    public static boolean isOpenCVLoaded() {
        return loaded;
    }
}

