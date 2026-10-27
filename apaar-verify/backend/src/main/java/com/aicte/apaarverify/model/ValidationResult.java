package com.aicte.apaarverify.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "validation_results", indexes = {
    @Index(name = "idx_validation_inst", columnList = "institution_id, resolved")
})
public class ValidationResult {

    public enum Severity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }

    public enum Category {
        MISSING_APAAR,
        INVALID_APAAR_FORMAT,
        INTRA_INSTITUTION_DUPLICATE,
        INTER_INSTITUTION_DUPLICATE,
        EXCESS_ADMISSION_INTAKE,
        MISSING_MANDATORY_FIELD,
        CATEGORY_MISMATCH
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrolment_id")
    private Enrolment enrolment;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_category", nullable = false, length = 50)
    private Category ruleCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private Boolean resolved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public ValidationResult() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Enrolment getEnrolment() { return enrolment; }
    public void setEnrolment(Enrolment enrolment) { this.enrolment = enrolment; }

    public Category getRuleCategory() { return ruleCategory; }
    public void setRuleCategory(Category ruleCategory) { this.ruleCategory = ruleCategory; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Boolean getResolved() { return resolved; }
    public void setResolved(Boolean resolved) { this.resolved = resolved; }

    public User getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(User resolvedBy) { this.resolvedBy = resolvedBy; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
