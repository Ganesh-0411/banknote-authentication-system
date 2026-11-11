package com.banknote.authentication.controller;

import com.banknote.authentication.dto.*;
import com.banknote.authentication.entity.BanknoteRecord;
import com.banknote.authentication.entity.ModelInformation;
import com.banknote.authentication.entity.User;
import com.banknote.authentication.repository.ModelInformationRepository;
import com.banknote.authentication.security.CustomUserDetails;
import com.banknote.authentication.service.BanknoteService;
import com.banknote.authentication.service.DeepLearningService;
import com.banknote.authentication.service.HistoryService;
import com.banknote.authentication.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BanknoteRestController {

    private final UserService userService;
    private final BanknoteService banknoteService;
    private final HistoryService historyService;
    private final DeepLearningService deepLearningService;
    private final ModelInformationRepository modelInfoRepository;
    private final AuthenticationManager authenticationManager;

    public BanknoteRestController(UserService userService,
                                  BanknoteService banknoteService,
                                  HistoryService historyService,
                                  DeepLearningService deepLearningService,
                                  ModelInformationRepository modelInfoRepository,
                                  AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.banknoteService = banknoteService;
        this.historyService = historyService;
        this.deepLearningService = deepLearningService;
        this.modelInfoRepository = modelInfoRepository;
        this.authenticationManager = authenticationManager;
    }

    // 1. POST /api/auth/register
    @PostMapping("/auth/register")
    public ResponseEntity<?> registerApi(@Valid @RequestBody RegisterRequest request) {
        try {
            User registered = userService.registerUser(request);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "User registered successfully");
            resp.put("userId", registered.getId());
            resp.put("email", registered.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
    }

    // 2. POST /api/auth/login
    @PostMapping("/auth/login")
    public ResponseEntity<?> loginApi(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Authentication successful");
            resp.put("email", userDetails.getUsername());
            resp.put("fullName", userDetails.getFullName());
            resp.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }
    }

    // 3. POST /api/banknote/upload
    @PostMapping("/banknote/upload")
    public ResponseEntity<UploadResponse> uploadBanknote(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(UploadResponse.failure("No file provided"));
        }
        try {
            String tempPath = "uploads/temp_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            java.io.File dest = new java.io.File(tempPath);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);

            return ResponseEntity.ok(UploadResponse.success(
                    "Image uploaded successfully",
                    tempPath,
                    file.getOriginalFilename(),
                    file.getSize()
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(UploadResponse.failure("Failed to save image: " + e.getMessage()));
        }
    }

    // 4. POST /api/banknote/analyze
    @PostMapping("/banknote/analyze")
    public ResponseEntity<?> analyzeBanknote(@RequestParam("file") MultipartFile file,
                                            @RequestParam(value = "denomination", defaultValue = "UNKNOWN") String denomination,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails != null ? userDetails.getUser() : null;
            AnalysisResponse response = banknoteService.processBanknote(file, denomination, user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
    }

    // 5. GET /api/banknote/history
    @GetMapping("/banknote/history")
    public ResponseEntity<?> getHistory(@RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "size", defaultValue = "10") int size,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Page<BanknoteRecord> records;
        if (userDetails != null && userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            records = historyService.getAllHistory(PageRequest.of(page, size));
        } else if (userDetails != null) {
            records = historyService.getUserHistory(userDetails.getUser(), PageRequest.of(page, size));
        } else {
            records = historyService.getAllHistory(PageRequest.of(page, size));
        }
        return ResponseEntity.ok(records);
    }

    // 6. GET /api/banknote/history/{id}
    @GetMapping("/banknote/history/{id}")
    public ResponseEntity<?> getHistoryRecord(@PathVariable("id") Long id) {
        BanknoteRecord record = banknoteService.getRecordById(id);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    // 7. DELETE /api/banknote/history/{id}
    @DeleteMapping("/api/banknote/history/{id}")
    public ResponseEntity<?> deleteHistoryRecord(@PathVariable("id") Long id,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isAdmin = userDetails != null && userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        User user = userDetails != null ? userDetails.getUser() : null;
        boolean deleted = historyService.deleteRecord(id, user, isAdmin);
        if (deleted) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Record deleted"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "error", "Record not found"));
        }
    }

    // 8. GET /api/dashboard/statistics
    @GetMapping("/dashboard/statistics")
    public ResponseEntity<DashboardResponse> getDashboardStatistics(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails != null ? userDetails.getUser() : null;
        DashboardResponse stats = historyService.getDashboardMetrics(user);
        return ResponseEntity.ok(stats);
    }

    // 9. GET /api/admin/users
    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getAdminUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    // 10. GET /api/admin/model
    @GetMapping("/admin/model")
    public ResponseEntity<?> getAdminModelInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("activeThreshold", deepLearningService.getActiveThreshold());
        info.put("activeVersion", deepLearningService.getActiveModelVersion());
        info.put("isModelReady", deepLearningService.isModelReady());
        modelInfoRepository.findFirstByStatusOrderByTrainingDateDesc("ACTIVE").ifPresent(m -> {
            info.put("modelDetails", m);
        });
        return ResponseEntity.ok(info);
    }
}

