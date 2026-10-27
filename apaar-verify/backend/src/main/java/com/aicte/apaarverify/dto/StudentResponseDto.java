package com.aicte.apaarverify.dto;

import java.time.LocalDate;
import java.util.UUID;

public class StudentResponseDto {
    private UUID studentId;
    private UUID enrolmentId;
    private String studentName;
    private String gender;
    private LocalDate dateOfBirth;
    private String apaarId;
    private Boolean apaarFormatValid;
    private String apaarVerificationStatus;
    private UUID institutionId;
    private String institutionName;
    private String institutionCode;
    private String courseName;
    private String department;
    private String academicYear;
    private String rollNumber;
    private String enrolmentStatus;
    private String admissionCategory;
    private Boolean isNri;
    private Boolean isOci;
    private Boolean isForeignNational;
    private Boolean isSupernumerary;
    private String verificationToken;

    public StudentResponseDto() {}

    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }

    public UUID getEnrolmentId() { return enrolmentId; }
    public void setEnrolmentId(UUID enrolmentId) { this.enrolmentId = enrolmentId; }

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

    public UUID getInstitutionId() { return institutionId; }
    public void setInstitutionId(UUID institutionId) { this.institutionId = institutionId; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public String getInstitutionCode() { return institutionCode; }
    public void setInstitutionCode(String institutionCode) { this.institutionCode = institutionCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getEnrolmentStatus() { return enrolmentStatus; }
    public void setEnrolmentStatus(String enrolmentStatus) { this.enrolmentStatus = enrolmentStatus; }

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

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }
}
