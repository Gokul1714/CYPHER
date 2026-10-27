package com.aicte.apaarverify.controller;

import com.aicte.apaarverify.model.ValidationResult;
import com.aicte.apaarverify.repository.ValidationResultRepository;
import com.aicte.apaarverify.service.AuditLogService;
import com.aicte.apaarverify.service.ValidationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/validation")
public class ValidationController {

    private final ValidationService validationService;
    private final ValidationResultRepository resultRepository;
    private final AuditLogService auditLogService;

    public ValidationController(
            ValidationService validationService,
            ValidationResultRepository resultRepository,
            AuditLogService auditLogService) {
        this.validationService = validationService;
        this.resultRepository = resultRepository;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/run")
    public ResponseEntity<List<ValidationResult>> runValidation(HttpServletRequest httpRequest) {
        List<ValidationResult> results = validationService.runAutomatedValidationEngine();
        auditLogService.recordLog(
                null, "OPERATOR", null,
                "EXECUTE_VALIDATION_ENGINE", "SYSTEM", null,
                httpRequest.getRemoteAddr(), "Executed automated validation engine. Flags generated: " + results.size()
        );
        return ResponseEntity.ok(results);
    }

    @GetMapping("/results")
    public ResponseEntity<List<ValidationResult>> getResults(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) Boolean resolved) {
        if (institutionId != null && resolved != null) {
            return ResponseEntity.ok(resultRepository.findByInstitutionIdAndResolved(institutionId, resolved));
        } else if (institutionId != null) {
            return ResponseEntity.ok(resultRepository.findByInstitutionId(institutionId));
        } else if (resolved != null) {
            return ResponseEntity.ok(resultRepository.findByResolved(resolved));
        }
        return ResponseEntity.ok(resultRepository.findAll());
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<?> resolveAnomaly(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {
        String notes = body.getOrDefault("notes", "Resolved by administrator");
        ValidationResult resolved = validationService.resolveAnomaly(id, notes, null);
        auditLogService.recordLog(
                null, "OPERATOR", resolved.getInstitution() != null ? resolved.getInstitution().getId() : null,
                "RESOLVE_ANOMALY", "VALIDATION_RESULT", id.toString(),
                httpRequest.getRemoteAddr(), "Resolved anomaly: " + notes
        );
        return ResponseEntity.ok(resolved);
    }
}
