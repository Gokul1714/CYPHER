package com.aicte.apaarverify.dto;

import java.time.LocalDate;
import java.util.UUID;

public class StudentCreateRequest {
    private String studentName;
    private String gender;
    private LocalDate dateOfBirth;
    private String apaarId;
    private String fatherOrGuardianName;
    private UUID institutionId;
    private String courseName;
    private String department;
    private String academicYear;
    private String rollNumber;
    private String admissionCategory; // GENERAL, OBC_NCL, SC, ST, EWS
    private Boolean isNri = false;
    private Boolean isOci = false;
    private Boolean isForeignNational = false;
    private Boolean isSupernumerary = false;

    public StudentCreateRequest() {}

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getApaarId() { return apaarId; }
    public void setApaarId(String apaarId) { this.apaarId = apaarId; }

    public String getFatherOrGuardianName() { return fatherOrGuardianName; }
    public void setFatherOrGuardianName(String fatherOrGuardianName) { this.fatherOrGuardianName = fatherOrGuardianName; }

    public UUID getInstitutionId() { return institutionId; }
    public void setInstitutionId(UUID institutionId) { this.institutionId = institutionId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getAdmissionCategory() { return admissionCategory; }
    public void setAdmissionCategory(String admissionCategory) { this.admissionCategory = admissionCategory; }

    public Boolean getIsNri() { return isNri; }
    public void setIsNri(Boolean isNri) { this.isNri = isNri; }

    public Boolean getIsOci() { return isOci; }
    public void setIsOci(Boolean isOci) { this.isOci = isOci; }

    public Boolean getIsForeignNational() { return isForeignNational; }
    public void setIsForeignNational(Boolean isForeignNational) { this.isForeignNational = isForeignNational; }

    public Boolean getIsSupernumerary() { return isSupernumerary; }
    public void setIsSupernumerary(Boolean isSupernumerary) { this.isSupernumerary = isSupernumerary; }
}
