package com.aicte.apaarverify.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_students_apaar_id", columnList = "apaar_id")
})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "student_name", nullable = false, length = 200)
    private String studentName;

    @Column(nullable = false, length = 20)
    private String gender;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "apaar_id", length = 50)
    private String apaarId;

    @Column(name = "apaar_format_valid")
    private Boolean apaarFormatValid = false;

    @Column(name = "apaar_verification_status", length = 50)
    private String apaarVerificationStatus = "UNVERIFIED"; // UNVERIFIED, MOCK_VERIFIED, PENDING_OFFICIAL_GATEWAY, REJECTED

    @Column(name = "father_or_guardian_name", length = 200)
    private String fatherOrGuardianName;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public Student() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getApaarId() { return apaarId; }
    public void setApaarId(String apaarId) { this.apaarId = apaarId; }

    public Boolean getApaarFormatValid() { return apaarFormatValid; }
    public void setApaarFormatValid(Boolean apaarFormatValid) { this.apaarFormatValid = apaarFormatValid; }

    public String getApaarVerificationStatus() { return apaarVerificationStatus; }
    public void setApaarVerificationStatus(String apaarVerificationStatus) { this.apaarVerificationStatus = apaarVerificationStatus; }

    public String getFatherOrGuardianName() { return fatherOrGuardianName; }
    public void setFatherOrGuardianName(String fatherOrGuardianName) { this.fatherOrGuardianName = fatherOrGuardianName; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
