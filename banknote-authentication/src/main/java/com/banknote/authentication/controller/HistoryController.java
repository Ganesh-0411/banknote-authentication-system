package com.banknote.authentication.controller;

import com.banknote.authentication.entity.BanknoteRecord;
import com.banknote.authentication.entity.ScreeningResult;
import com.banknote.authentication.security.CustomUserDetails;
import com.banknote.authentication.service.BanknoteService;
import com.banknote.authentication.service.HistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/history")
public class HistoryController {

    private final HistoryService historyService;
    private final BanknoteService banknoteService;

    public HistoryController(HistoryService historyService, BanknoteService banknoteService) {
        this.historyService = historyService;
        this.banknoteService = banknoteService;
    }

    @GetMapping
    public String historyList(@RequestParam(value = "denomination", required = false) String denomination,
                              @RequestParam(value = "result", required = false) ScreeningResult result,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size);
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Page<BanknoteRecord> recordPage;
        if (denomination != null || result != null) {
            recordPage = historyService.filterRecords(
                    isAdmin ? null : userDetails.getUser(),
                    (denomination != null && !denomination.isBlank()) ? denomination : null,
                    result, null, null, pageable
            );
        } else {
            recordPage = isAdmin ? historyService.getAllHistory(pageable) :
                    historyService.getUserHistory(userDetails.getUser(), pageable);
        }

        model.addAttribute("page", recordPage);
        model.addAttribute("selectedDenomination", denomination);
        model.addAttribute("selectedResult", result);
        model.addAttribute("currentUser", userDetails.getUser());

        return "history";
    }

    @GetMapping("/{id}")
    public String viewDetail(@PathVariable("id") Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        BanknoteRecord record = banknoteService.getRecordById(id);
        if (record == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Record not found");
            return "redirect:/history";
        }

        model.addAttribute("record", record);
        model.addAttribute("currentUser", userDetails.getUser());
        model.addAttribute("originalImageUrl", "/" + record.getImagePath().replace("\\", "/"));
        if (record.getReconstructedImagePath() != null) {
            model.addAttribute("reconstructedImageUrl", "/" + record.getReconstructedImagePath().replace("\\", "/"));
        }
        if (record.getHeatmapPath() != null) {
            model.addAttribute("heatmapImageUrl", "/" + record.getHeatmapPath().replace("\\", "/"));
        }

        return "detail";
    }

    @PostMapping("/{id}/delete")
    public String deleteRecord(@PathVariable("id") Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        try {
            historyService.deleteRecord(id, userDetails.getUser(), isAdmin);
            redirectAttributes.addFlashAttribute("successMessage", "Record deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete record: " + e.getMessage());
        }
        return "redirect:/history";
    }
}

