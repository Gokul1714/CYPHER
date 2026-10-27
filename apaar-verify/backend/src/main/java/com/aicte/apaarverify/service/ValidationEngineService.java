package com.aicte.apaarverify.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ValidationEngineService {

    private final VerhoeffCheckDigit verhoeffCheckDigit;

    public ValidationEngineService(VerhoeffCheckDigit verhoeffCheckDigit) {
        this.verhoeffCheckDigit = verhoeffCheckDigit;
    }

    public static class ValidationFinding {
        private String ruleCategory;
        private String severity;
        private String studentId;
        private String studentName;
        private String message;
        private Map<String, Object> details;

        public ValidationFinding(String ruleCategory, String severity, String studentId, String studentName, String message, Map<String, Object> details) {
            this.ruleCategory = ruleCategory;
            this.severity = severity;
            this.studentId = studentId;
            this.studentName = studentName;
            this.message = message;
            this.details = details;
        }

        public String getRuleCategory() { return ruleCategory; }
        public String getSeverity() { return severity; }
        public String getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public String getMessage() { return message; }
        public Map<String, Object> getDetails() { return details; }
    }

    public List<ValidationFinding> validateStudentRecord(String studentId, String studentName, String apaarId, String courseName, int currentEnrolled, int approvedSeats) {
        List<ValidationFinding> findings = new ArrayList<>();

        // 1. Missing APAAR Reference
        if (apaarId == null || apaarId.trim().isEmpty()) {
            findings.add(new ValidationFinding(
                "MISSING_APAAR",
                "WARNING",
                studentId,
                studentName,
                "Student enrolled without mandatory APAAR ID reference. Provisional admission requires APAAR generation.",
                Map.of("field", "apaarId")
            ));
            return findings;
        }

        // 2. Syntactic 12-Digit Format Check
        if (!apaarId.matches("\\d{12}")) {
            findings.add(new ValidationFinding(
                "INVALID_APAAR_FORMAT",
                "ERROR",
                studentId,
                studentName,
                "APAAR ID must contain exactly 12 numerical digits. Current length: " + apaarId.length(),
                Map.of("apaarId", apaarId, "syntaxValid", false)
            ));
        }

        // 3. Approved Intake Monitoring Check
        if (currentEnrolled > approvedSeats) {
            findings.add(new ValidationFinding(
                "EXCESS_ADMISSION_INTAKE",
                "CRITICAL",
                studentId,
                studentName,
                "Enrolment exceeds sanctioned AICTE approved intake for " + courseName + " (" + currentEnrolled + " / " + approvedSeats + ").",
                Map.of("course", courseName, "approved", approvedSeats, "enrolled", currentEnrolled)
            ));
        }

        return findings;
    }
}
