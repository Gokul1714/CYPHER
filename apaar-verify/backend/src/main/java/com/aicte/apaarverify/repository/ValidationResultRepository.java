package com.aicte.apaarverify.repository;

import com.aicte.apaarverify.model.ValidationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ValidationResultRepository extends JpaRepository<ValidationResult, UUID> {
    List<ValidationResult> findByInstitutionId(UUID institutionId);
    List<ValidationResult> findByInstitutionIdAndResolved(UUID institutionId, Boolean resolved);
    List<ValidationResult> findByResolved(Boolean resolved);
}
