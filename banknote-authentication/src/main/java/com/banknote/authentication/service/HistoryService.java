package com.banknote.authentication.service;

import com.banknote.authentication.dto.DashboardResponse;
import com.banknote.authentication.entity.BanknoteRecord;
import com.banknote.authentication.entity.ScreeningResult;
import com.banknote.authentication.entity.User;
import com.banknote.authentication.repository.BanknoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HistoryService {

    private final BanknoteRepository banknoteRepository;

    public HistoryService(BanknoteRepository banknoteRepository) {
        this.banknoteRepository = banknoteRepository;
    }

    public Page<BanknoteRecord> getUserHistory(User user, Pageable pageable) {
        return banknoteRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public Page<BanknoteRecord> getAllHistory(Pageable pageable) {
        return banknoteRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Page<BanknoteRecord> filterRecords(User user, String denomination, ScreeningResult result,
                                             LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return banknoteRepository.filterRecords(user, denomination, result, startDate, endDate, pageable);
    }

    @Transactional
    public boolean deleteRecord(Long recordId, User currentUser, boolean isAdmin) {
        return banknoteRepository.findById(recordId).map(record -> {
            if (!isAdmin && (record.getUser() == null || !record.getUser().getId().equals(currentUser.getId()))) {
                throw new SecurityException("Unauthorized to delete this record");
            }
            // Delete generated image files if present
            if (record.getImagePath() != null) new File(record.getImagePath()).delete();
            if (record.getReconstructedImagePath() != null) new File(record.getReconstructedImagePath()).delete();
            if (record.getHeatmapPath() != null) new File(record.getHeatmapPath()).delete();

            banknoteRepository.delete(record);
            return true;
        }).orElse(false);
    }

    public DashboardResponse getDashboardMetrics(User user) {
        DashboardResponse dto = new DashboardResponse();

        long total = banknoteRepository.count();
        long genuine = banknoteRepository.countByScreeningResult(ScreeningResult.GENUINE_LIKE);
        long anomaly = banknoteRepository.countByScreeningResult(ScreeningResult.ANOMALY_DETECTED);
        long inconclusive = banknoteRepository.countByScreeningResult(ScreeningResult.INCONCLUSIVE);

        dto.setTotalScans(total);
        dto.setGenuineLikeScans(genuine);
        dto.setAnomalyScans(anomaly);
        dto.setInconclusiveScans(inconclusive);
        dto.setGenuineRate(total > 0 ? ((double) genuine / total) * 100.0 : 0.0);

        // Denominations breakdown
        Map<String, Long> denomMap = new HashMap<>();
        List<Object[]> denomCounts = banknoteRepository.countByDenominations();
        for (Object[] row : denomCounts) {
            String denom = (String) row[0];
            Long count = (Long) row[1];
            denomMap.put(denom != null ? denom : "Unknown", count);
        }
        dto.setDenominationBreakdown(denomMap);

        // Results breakdown
        Map<String, Long> resMap = new HashMap<>();
        resMap.put("GENUINE_LIKE", genuine);
        resMap.put("ANOMALY_DETECTED", anomaly);
        resMap.put("INCONCLUSIVE", inconclusive);
        dto.setResultBreakdown(resMap);

        return dto;
    }
}

