package com.banknote.authentication.controller;

import com.banknote.authentication.dto.AnalysisResponse;
import com.banknote.authentication.entity.BanknoteRecord;
import com.banknote.authentication.security.CustomUserDetails;
import com.banknote.authentication.service.BanknoteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class BanknoteController {

    private final BanknoteService banknoteService;

    public BanknoteController(BanknoteService banknoteService) {
        this.banknoteService = banknoteService;
    }

    @GetMapping("/upload")
    public String uploadPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("currentUser", userDetails != null ? userDetails.getUser() : null);
        return "upload";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "denomination", defaultValue = "UNKNOWN") String denomination,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select an image file to analyze.");
            return "redirect:/upload";
        }

        try {
            AnalysisResponse response = banknoteService.processBanknote(
                    file, denomination, userDetails != null ? userDetails.getUser() : null);
            return "redirect:/result/" + response.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Image processing failed: " + e.getMessage());
            return "redirect:/upload";
        }
    }

    @PostMapping("/capture")
    public String handleCameraCapture(@RequestParam("imageData") String imageData,
                                      @RequestParam(value = "denomination", defaultValue = "UNKNOWN") String denomination,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        try {
            AnalysisResponse response = banknoteService.processBase64Capture(
                    imageData, denomination, userDetails != null ? userDetails.getUser() : null);
            return "redirect:/result/" + response.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Camera capture analysis failed: " + e.getMessage());
            return "redirect:/upload";
        }
    }

    @GetMapping("/result/{id}")
    public String resultPage(@PathVariable("id") Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        BanknoteRecord record = banknoteService.getRecordById(id);
        if (record == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Screening record not found.");
            return "redirect:/dashboard";
        }

        model.addAttribute("record", record);
        model.addAttribute("currentUser", userDetails != null ? userDetails.getUser() : null);

        // Normalize image paths for web browser display
        model.addAttribute("originalImageUrl", "/" + record.getImagePath().replace("\\", "/"));
        if (record.getReconstructedImagePath() != null) {
            model.addAttribute("reconstructedImageUrl", "/" + record.getReconstructedImagePath().replace("\\", "/"));
        }
        if (record.getHeatmapPath() != null) {
            model.addAttribute("heatmapImageUrl", "/" + record.getHeatmapPath().replace("\\", "/"));
        }

        return "result";
    }
}

