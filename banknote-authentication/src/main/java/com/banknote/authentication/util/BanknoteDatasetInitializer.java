package com.banknote.authentication.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Automatically initializes genuine Indian currency sample datasets on application startup
 * if the dataset directory is empty. Uses standard Java AWT/ImageIO (zero native dependency)
 * so it executes reliably across all operating systems.
 */
@Component
public class BanknoteDatasetInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(BanknoteDatasetInitializer.class);

    private static final String[] DENOMINATIONS = {"10", "20", "50", "100", "200", "500", "2000"};

    @Override
    public void run(ApplicationArguments args) {
        File trainDir = new File("dataset/train/genuine/");
        File valDir = new File("dataset/validation/genuine/");
        File testDir = new File("dataset/test/genuine/");

        trainDir.mkdirs();
        valDir.mkdirs();
        testDir.mkdirs();

        // Check if training data already exists
        File[] files = trainDir.listFiles();
        boolean hasImages = false;
        if (files != null && files.length > 0) {
            for (File f : files) {
                if (f.isDirectory() && f.listFiles() != null && f.listFiles().length > 0) {
                    hasImages = true;
                    break;
                }
            }
        }

        if (!hasImages) {
            logger.info("Dataset directory is empty. Synthesizing genuine Indian banknote dataset across all 7 denominations...");
            generateInitialDatasets("dataset");
            logger.info("Genuine Indian currency dataset initialized successfully!");
        }
    }

    public static void generateInitialDatasets(String basePath) {
        for (String denom : DENOMINATIONS) {
            String trainPath = basePath + "/train/genuine/" + denom;
            String valPath = basePath + "/validation/genuine/" + denom;
            String testPath = basePath + "/test/genuine/" + denom;

            new File(trainPath).mkdirs();
            new File(valPath).mkdirs();
            new File(testPath).mkdirs();

            Color baseColor = getColorForDenomination(denom);

            // Generate 10 genuine training banknotes per denomination
            for (int i = 1; i <= 10; i++) {
                BufferedImage img = drawBanknote(denom, baseColor, i, false);
                saveImage(img, new File(trainPath, "note_" + denom + "_train_" + i + ".png"));
            }

            // Generate 4 genuine validation banknotes per denomination
            for (int i = 1; i <= 4; i++) {
                BufferedImage img = drawBanknote(denom, baseColor, 100 + i, false);
                saveImage(img, new File(valPath, "note_" + denom + "_val_" + i + ".png"));
            }

            // Generate 3 genuine test banknotes and 1 anomalous test sample per denomination
            for (int i = 1; i <= 3; i++) {
                BufferedImage img = drawBanknote(denom, baseColor, 200 + i, false);
                saveImage(img, new File(testPath, "note_" + denom + "_test_genuine_" + i + ".png"));
            }
            // 1 anomalous test note with irregular features for testing anomaly detection
            BufferedImage anomalyImg = drawBanknote(denom, baseColor, 999, true);
            saveImage(anomalyImg, new File(testPath, "note_" + denom + "_test_anomaly.png"));
        }
    }

    private static Color getColorForDenomination(String denom) {
        if (denom == null) {
            return new Color(150, 150, 150);
        }
        switch (denom) {
            case "10":
                return new Color(139, 90, 43);    // Chocolate Brown
            case "20":
                return new Color(175, 175, 60);   // Greenish Yellow
            case "50":
                return new Color(70, 180, 210);   // Fluorescent Blue
            case "100":
                return new Color(150, 115, 165); // Lavender
            case "200":
                return new Color(235, 140, 50);  // Bright Orange
            case "500":
                return new Color(135, 145, 140); // Stone Grey
            case "2000":
                return new Color(185, 75, 135); // Magenta
            default:
                return new Color(150, 150, 150);
        }
    }

    private static BufferedImage drawBanknote(String denom, Color baseColor, int seed, boolean isAnomaly) {
        int w = 448;
        int h = 224;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Random rand = new Random(seed);

        // 1. Banknote Base Color
        g.setColor(baseColor);
        g.fillRect(0, 0, w, h);

        // 2. Currency Micro-patterns & Guilloche Sine waves
        g.setColor(new Color(Math.min(255, baseColor.getRed() + 30),
                             Math.min(255, baseColor.getGreen() + 30),
                             Math.min(255, baseColor.getBlue() + 30)));
        for (int x = 15; x < w - 15; x += 12) {
            int yOffset = (int) (Math.sin((x + seed) * 0.08) * 30);
            g.drawLine(x, 30 + yOffset, x + 8, h - 30 - yOffset);
        }

        // 3. Banknote Double Border
        g.setColor(new Color(40, 40, 40));
        g.setStroke(new BasicStroke(2.0f));
        g.draw(new RoundRectangle2D.Float(8, 8, w - 16, h - 16, 10, 10));
        g.setStroke(new BasicStroke(1.0f));
        g.draw(new RoundRectangle2D.Float(14, 14, w - 28, h - 28, 6, 6));

        // 4. Gandhi Watermark Oval Window
        int ovalX = 40;
        int ovalY = 35;
        int ovalW = 90;
        int ovalH = 150;
        g.setColor(new Color(Math.max(0, baseColor.getRed() - 35),
                             Math.max(0, baseColor.getGreen() - 35),
                             Math.max(0, baseColor.getBlue() - 35)));
        g.fill(new Ellipse2D.Float(ovalX, ovalY, ovalW, ovalH));
        g.setColor(new Color(220, 220, 220, 120));
        g.draw(new Ellipse2D.Float(ovalX, ovalY, ovalW, ovalH));

        // Watermark Profile Silhouette
        g.setColor(new Color(240, 240, 240, 90));
        g.fillOval(ovalX + 25, ovalY + 30, 40, 45);
        g.drawArc(ovalX + 20, ovalY + 70, 50, 60, 0, 180);

        // 5. Metallic Security Thread (Green/Gold dashes)
        int threadX = (int) (w * 0.44) + (seed % 6);
        g.setColor(isAnomaly ? new Color(220, 30, 30) : new Color(40, 160, 60));
        g.setStroke(new BasicStroke(3.5f));
        for (int y = 20; y < h - 20; y += 18) {
            g.drawLine(threadX, y, threadX, y + 10);
        }

        // 6. Header Typography: RESERVE BANK OF INDIA
        g.setColor(new Color(25, 25, 25));
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString("RESERVE BANK OF INDIA", (int) (w * 0.35), 42);

        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString("GUARANTEED BY THE CENTRAL GOVERNMENT", (int) (w * 0.35), 58);

        // 7. Large Denomination Numeral
        g.setFont(new Font("SansSerif", Font.BOLD, 38));
        g.drawString("\u20B9" + denom, (int) (w * 0.70), (int) (h * 0.58));

        // 8. Governor Promise and Ashoka Pillar emblem box
        g.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g.drawString("I PROMISE TO PAY THE BEARER", (int) (w * 0.36), (int) (h * 0.75));
        g.drawString("THE SUM OF " + denom + " RUPEES", (int) (w * 0.36), (int) (h * 0.83));

        // Ashoka Pillar emblem box right edge
        g.drawRect(w - 60, (int) (h * 0.35), 42, 65);
        g.drawString("EMBLEM", w - 56, (int) (h * 0.52));

        // Bleed marks for visually impaired
        g.setStroke(new BasicStroke(2.0f));
        for (int b = 0; b < 4; b++) {
            g.drawLine(10, 40 + (b * 12), 22, 40 + (b * 12));
            g.drawLine(w - 22, 40 + (b * 12), w - 10, 40 + (b * 12));
        }

        // Simulated Anomaly Defect (if testing anomaly detection)
        if (isAnomaly) {
            g.setColor(new Color(220, 30, 30));
            g.fillRect((int) (w * 0.50), (int) (h * 0.35), 80, 50);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("ANOMALY", (int) (w * 0.51), (int) (h * 0.55));
        }

        g.dispose();
        return img;
    }

    private static void saveImage(BufferedImage img, File targetFile) {
        try {
            ImageIO.write(img, "png", targetFile);
        } catch (IOException e) {
            logger.warn("Could not write sample image {}: {}", targetFile.getName(), e.getMessage());
        }
    }
}

