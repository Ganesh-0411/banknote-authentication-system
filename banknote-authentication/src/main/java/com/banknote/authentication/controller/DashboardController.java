package com.banknote.authentication.controller;

import com.banknote.authentication.dto.DashboardResponse;
import com.banknote.authentication.entity.BanknoteRecord;
import com.banknote.authentication.repository.BanknoteRepository;
import com.banknote.authentication.security.CustomUserDetails;
import com.banknote.authentication.service.HistoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final HistoryService historyService;
    private final BanknoteRepository banknoteRepository;

    public DashboardController(HistoryService historyService, BanknoteRepository banknoteRepository) {
        this.historyService = historyService;
        this.banknoteRepository = banknoteRepository;
    }

    @GetMapping("/")
    public String rootPage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/dashboard";
        }
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        DashboardResponse stats = historyService.getDashboardMetrics(userDetails.getUser());
        List<BanknoteRecord> recentScans = banknoteRepository.findTop10ByOrderByCreatedAtDesc();

        model.addAttribute("currentUser", userDetails.getUser());
        model.addAttribute("stats", stats);
        model.addAttribute("recentScans", recentScans);

        return "dashboard";
    }
}

