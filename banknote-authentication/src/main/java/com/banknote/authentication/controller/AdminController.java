package com.banknote.authentication.controller;

import com.banknote.authentication.deepLearning.BanknoteModelTrainer;
import com.banknote.authentication.entity.ModelInformation;
import com.banknote.authentication.entity.ScreeningResult;
import com.banknote.authentication.repository.BanknoteRepository;
import com.banknote.authentication.repository.ModelInformationRepository;
import com.banknote.authentication.security.CustomUserDetails;
import com.banknote.authentication.service.DeepLearningService;
import com.banknote.authentication.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final BanknoteRepository banknoteRepository;
    private final ModelInformationRepository modelInfoRepository;
    private final DeepLearningService deepLearningService;
    private final BanknoteModelTrainer modelTrainer;

    public AdminController(UserService userService,
                           BanknoteRepository banknoteRepository,
                           ModelInformationRepository modelInfoRepository,
                           DeepLearningService deepLearningService,
                           BanknoteModelTrainer modelTrainer) {
        this.userService = userService;
        this.banknoteRepository = banknoteRepository;
        this.modelInfoRepository = modelInfoRepository;
        this.deepLearningService = deepLearningService;
        this.modelTrainer = modelTrainer;
    }

    @GetMapping
    public String adminDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        long totalUsers = userService.countUsers();
        long totalScans = banknoteRepository.count();
        long genuineCount = banknoteRepository.countByScreeningResult(ScreeningResult.GENUINE_LIKE);
        long anomalyCount = banknoteRepository.countByScreeningResult(ScreeningResult.ANOMALY_DETECTED);
        long inconclusiveCount = banknoteRepository.countByScreeningResult(ScreeningResult.INCONCLUSIVE);

        Optional<ModelInformation> activeModel = modelInfoRepository.findFirstByStatusOrderByTrainingDateDesc("ACTIVE");

        model.addAttribute("currentUser", userDetails.getUser());
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalScans", totalScans);
        model.addAttribute("genuineCount", genuineCount);
        model.addAttribute("anomalyCount", anomalyCount);
        model.addAttribute("inconclusiveCount", inconclusiveCount);
        model.addAttribute("activeModel", activeModel.orElse(null));
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("allModels", modelInfoRepository.findAll());
        model.addAttribute("activeThreshold", deepLearningService.getActiveThreshold());
        model.addAttribute("isModelReady", deepLearningService.isModelReady());

        return "admin";
    }

    @PostMapping("/threshold/update")
    public String updateThreshold(@RequestParam("threshold") double threshold,
                                  RedirectAttributes redirectAttributes) {
        if (threshold <= 0 || threshold > 1.0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Threshold must be between 0.001 and 1.0");
            return "redirect:/admin";
        }

        deepLearningService.setActiveThreshold(threshold);
        modelInfoRepository.findFirstByStatusOrderByTrainingDateDesc("ACTIVE").ifPresent(info -> {
            info.setThreshold(threshold);
            modelInfoRepository.save(info);
        });

        redirectAttributes.addFlashAttribute("successMessage", "Anomaly threshold updated successfully to " + threshold);
        return "redirect:/admin";
    }

    @PostMapping("/retrain")
    public String retrainModel(@RequestParam(value = "epochs", defaultValue = "5") int epochs,
                               @RequestParam(value = "learningRate", defaultValue = "0.001") double lr,
                               @RequestParam(value = "version", defaultValue = "v1.1.0") String version,
                               RedirectAttributes redirectAttributes) {
        File trainDir = new File("dataset/train/genuine/");
        File valDir = new File("dataset/validation/genuine/");

        BanknoteModelTrainer.TrainingSummary summary = modelTrainer.train(trainDir, valDir, epochs, lr, version);
        if (summary.isSuccess()) {
            deepLearningService.loadOrInitializeModel();
            redirectAttributes.addFlashAttribute("successMessage", summary.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", summary.getMessage());
        }
        return "redirect:/admin";
    }
}

