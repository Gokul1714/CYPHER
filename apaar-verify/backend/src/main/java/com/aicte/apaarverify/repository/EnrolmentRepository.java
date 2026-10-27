package com.aicte.apaarverify.repository;

import com.aicte.apaarverify.model.Enrolment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrolmentRepository extends JpaRepository<Enrolment, UUID> {

    List<Enrolment> findByInstitutionId(UUID institutionId);

    List<Enrolment> findByInstitutionIdAndAcademicYear(UUID institutionId, String academicYear);

    List<Enrolment> findByStudentId(UUID studentId);

    Optional<Enrolment> findByStudentIdAndInstitutionIdAndAcademicYear(UUID studentId, UUID institutionId, String academicYear);

    @Query("SELECT COUNT(e) FROM Enrolment e WHERE e.institution.id = :institutionId AND e.courseName = :courseName AND e.academicYear = :academicYear AND e.enrolmentStatus = 'ACTIVE'")
    long countActiveEnrolments(@Param("institutionId") UUID institutionId, @Param("courseName") String courseName, @Param("academicYear") String academicYear);

    @Query("SELECT e FROM Enrolment e JOIN e.student s WHERE s.apaarId = :apaarId AND e.enrolmentStatus = 'ACTIVE'")
    List<Enrolment> findActiveEnrolmentsByApaarId(@Param("apaarId") String apaarId);
}
