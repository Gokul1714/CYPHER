package com.aicte.apaarverify.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "approved_intake", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"institution_id", "course_name", "academic_year"})
})
public class ApprovedIntake {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "course_name", nullable = false, length = 150)
    private String courseName;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "approved_seats", nullable = false)
    private Integer approvedSeats;

    @Column(name = "nri_quota")
    private Integer nriQuota = 0;

    @Column(name = "supernumerary_quota")
    private Integer supernumeraryQuota = 0;

    @Column(name = "approval_ref_number", length = 100)
    private String approvalRefNumber;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public ApprovedIntake() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getApprovedSeats() { return approvedSeats; }
    public void setApprovedSeats(Integer approvedSeats) { this.approvedSeats = approvedSeats; }

    public Integer getNriQuota() { return nriQuota; }
    public void setNriQuota(Integer nriQuota) { this.nriQuota = nriQuota; }

    public Integer getSupernumeraryQuota() { return supernumeraryQuota; }
    public void setSupernumeraryQuota(Integer supernumeraryQuota) { this.supernumeraryQuota = supernumeraryQuota; }

    public String getApprovalRefNumber() { return approvalRefNumber; }
    public void setApprovalRefNumber(String approvalRefNumber) { this.approvalRefNumber = approvalRefNumber; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
