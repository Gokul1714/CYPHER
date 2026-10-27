package com.aicte.apaarverify.controller;

import com.aicte.apaarverify.model.AuditLog;
import com.aicte.apaarverify.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getLogs(@RequestParam(required = false) UUID institutionId) {
        return ResponseEntity.ok(auditLogService.getLogs(institutionId));
    }
}
