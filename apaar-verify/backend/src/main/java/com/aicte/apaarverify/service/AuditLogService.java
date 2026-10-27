package com.aicte.apaarverify.service;

import com.aicte.apaarverify.model.AuditLog;
import com.aicte.apaarverify.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public AuditLog recordLog(UUID userId, String username, UUID institutionId, String action, String entityType, String entityId, String ipAddress, String details) {
        AuditLog log = new AuditLog(userId, username, institutionId, action, entityType, entityId, ipAddress, details);
        return auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogs(UUID institutionId) {
        if (institutionId != null) {
            return auditLogRepository.findByInstitutionIdOrderByCreatedAtDesc(institutionId);
        }
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }
}
