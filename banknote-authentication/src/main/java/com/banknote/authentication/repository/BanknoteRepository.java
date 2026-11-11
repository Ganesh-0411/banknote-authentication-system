package com.banknote.authentication.repository;

import com.banknote.authentication.entity.BanknoteRecord;
import com.banknote.authentication.entity.ScreeningResult;
import com.banknote.authentication.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BanknoteRepository extends JpaRepository<BanknoteRecord, Long> {

    List<BanknoteRecord> findByUserOrderByCreatedAtDesc(User user);

    Page<BanknoteRecord> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<BanknoteRecord> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByScreeningResult(ScreeningResult screeningResult);

    @Query("SELECT r.denomination, COUNT(r) FROM BanknoteRecord r GROUP BY r.denomination")
    List<Object[]> countByDenominations();

    @Query("SELECT r.screeningResult, COUNT(r) FROM BanknoteRecord r GROUP BY r.screeningResult")
    List<Object[]> countByResults();

    @Query("SELECT r FROM BanknoteRecord r WHERE " +
           "(:user IS NULL OR r.user = :user) AND " +
           "(:denomination IS NULL OR r.denomination = :denomination) AND " +
           "(:result IS NULL OR r.screeningResult = :result) AND " +
           "(:startDate IS NULL OR r.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR r.createdAt <= :endDate) " +
           "ORDER BY r.createdAt DESC")
    Page<BanknoteRecord> filterRecords(
            @Param("user") User user,
            @Param("denomination") String denomination,
            @Param("result") ScreeningResult result,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    List<BanknoteRecord> findTop10ByOrderByCreatedAtDesc();
}

