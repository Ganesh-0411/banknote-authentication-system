package com.banknote.authentication.repository;

import com.banknote.authentication.entity.ModelInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModelInformationRepository extends JpaRepository<ModelInformation, Long> {
    Optional<ModelInformation> findByModelVersion(String modelVersion);
    Optional<ModelInformation> findFirstByStatusOrderByTrainingDateDesc(String status);
}

