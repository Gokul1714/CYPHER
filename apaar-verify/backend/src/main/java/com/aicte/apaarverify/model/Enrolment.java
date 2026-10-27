package com.aicte.apaarverify.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrolments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "institution_id", "academic_year"})
}, indexes = {
    @Index(name = "idx_enrolments_institution", columnList = "institution_id, academic_year"),
    @Index(name = "idx_enrolments_student", columnList = "student_id")
})
public class Enrolment {

    public enum EnrolmentStatus {
        ACTIVE,
        COMPLETED,
        TRANSFERRED,
        WITHDRAWN,
        SUSPENDED
    }

    public enum AdmissionCategory {
        GENERAL,
        OBC_NCL,
        SC,
        ST,
        EWS,
        MANAGEMENT,
        NRI,
        FOREIGN_NATIONAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "course_name", nullable = false, length = 150)
    private String courseName;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "roll_number", length = 100)
    private String rollNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrolment_status", nullable = false, length = 50)
    private EnrolmentStatus enrolmentStatus = EnrolmentStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "admission_category", nullable = false, length = 50)
    private AdmissionCategory admissionCategory = AdmissionCategory.GENERAL;

    @Column(name = "is_nri")
    private Boolean isNri = false;

    @Column(name = "is_oci")
    private Boolean isOci = false;

    @Column(name = "is_foreign_national")
    private Boolean isForeignNational = false;

    @Column(name = "is_supernumerary")
    private Boolean isSupernumerary = false;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public Enrolment() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public EnrolmentStatus getEnrolmentStatus() { return enrolmentStatus; }
    public void setEnrolmentStatus(EnrolmentStatus enrolmentStatus) { this.enrolmentStatus = enrolmentStatus; }

    public AdmissionCategory getAdmissionCategory() { return admissionCategory; }
    public void setAdmissionCategory(AdmissionCategory admissionCategory) { this.admissionCategory = admissionCategory; }

    public Boolean getIsNri() { return isNri; }
    public void setIsNri(Boolean isNri) { this.isNri = isNri; }

    public Boolean getIsOci() { return isOci; }
    public void setIsOci(Boolean isOci) { this.isOci = isOci; }

    public Boolean getIsForeignNational() { return isForeignNational; }
    public void setIsForeignNational(Boolean isForeignNational) { this.isForeignNational = isForeignNational; }

    public Boolean getIsSupernumerary() { return isSupernumerary; }
    public void setIsSupernumerary(Boolean isSupernumerary) { this.isSupernumerary = isSupernumerary; }

    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
