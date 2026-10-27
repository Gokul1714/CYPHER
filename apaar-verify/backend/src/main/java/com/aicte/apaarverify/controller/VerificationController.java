package com.aicte.apaarverify.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/verify")
@CrossOrigin(origins = "*")
public class VerificationController {

    @GetMapping("/{token}")
    public ResponseEntity<?> verifyStudentCredential(@PathVariable String token) {
        // Mock verification matching the signed token format
        if (token.contains("982345012344") || token.contains("verified")) {
            return ResponseEntity.ok(Map.of(
                "isValid", true,
                "verificationStatus", "OFFICIAL_MOCK_VERIFIED",
                "studentName", "Aarav Patel",
                "maskedApaar", "9823-XXXX-2344",
                "institutionName", "Delhi Institute of Engineering & Technology",
                "institutionCode", "1-AICTE-DELHI-001",
                "course", "B.Tech Computer Science & Engineering",
                "academicYear", "2026-2027",
                "enrolmentStatus", "ACTIVE",
                "verifiedAt", Instant.now().toString(),
                "auditReference", "AUD-VERIFY-" + System.currentTimeMillis()
            ));
        }

        if (token.contains("dual_enrolled_flag")) {
            return ResponseEntity.ok(Map.of(
                "isValid", true,
                "verificationStatus", "FLAGGED_FOR_AICTE_REVIEW",
                "studentName", "Karan Johar Sharma",
                "maskedApaar", "7734-XXXX-5621",
                "institutionName", "Delhi Institute of Engineering & Technology / MNIT Pune",
                "course", "B.Tech Computer Science & Engineering",
                "academicYear", "2026-2027",
                "enrolmentStatus", "FLAGGED",
                "verifiedAt", Instant.now().toString(),
                "warning", "Active dual enrolment detected across institutions. Adjudication pending."
            ));
        }

        return ResponseEntity.status(404).body(Map.of(
            "isValid", false,
            "message", "Verification token is invalid, expired, or revoked in AICTE ledger."
        ));
    }
}
