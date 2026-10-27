package com.aicte.apaarverify.repository;

import com.aicte.apaarverify.model.ApprovedIntake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApprovedIntakeRepository extends JpaRepository<ApprovedIntake, UUID> {
    List<ApprovedIntake> findByInstitutionId(UUID institutionId);
    List<ApprovedIntake> findByInstitutionIdAndAcademicYear(UUID institutionId, String academicYear);
    Optional<ApprovedIntake> findByInstitutionIdAndCourseNameAndAcademicYear(UUID institutionId, String courseName, String academicYear);
}
