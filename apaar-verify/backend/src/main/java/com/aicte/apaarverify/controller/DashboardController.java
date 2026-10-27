package com.aicte.apaarverify.controller;

import com.aicte.apaarverify.repository.EnrolmentRepository;
import com.aicte.apaarverify.repository.InstitutionRepository;
import com.aicte.apaarverify.repository.StudentRepository;
import com.aicte.apaarverify.repository.ValidationResultRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final InstitutionRepository institutionRepository;
    private final StudentRepository studentRepository;
    private final EnrolmentRepository enrolmentRepository;
    private final ValidationResultRepository resultRepository;

    public DashboardController(
            InstitutionRepository institutionRepository,
            StudentRepository studentRepository,
            EnrolmentRepository enrolmentRepository,
            ValidationResultRepository resultRepository) {
        this.institutionRepository = institutionRepository;
        this.studentRepository = studentRepository;
        this.enrolmentRepository = enrolmentRepository;
        this.resultRepository = resultRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(@RequestParam(required = false) UUID institutionId) {
        Map<String, Object> stats = new HashMap<>();

        long totalInstitutions = institutionRepository.count();
        long totalStudents = studentRepository.count();
        long totalEnrolments = institutionId != null ?
                enrolmentRepository.findByInstitutionId(institutionId).size() :
                enrolmentRepository.count();

        long unresolvedAnomalies = institutionId != null ?
                resultRepository.findByInstitutionIdAndResolved(institutionId, false).size() :
                resultRepository.findByResolved(false).size();

        stats.put("totalInstitutions", totalInstitutions);
        stats.put("totalStudents", totalStudents);
        stats.put("totalEnrolments", totalEnrolments);
        stats.put("unresolvedAnomalies", unresolvedAnomalies);
        stats.put("academicYear", "2026-2027");

        return ResponseEntity.ok(stats);
    }
}
