package com.aicte.apaarverify.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignUpRequest {

    @NotBlank(message = "Full Name is required")
    @Size(max = 150, message = "Full Name cannot exceed 150 characters")
    private String fullName;

    @NotBlank(message = "Staff Email ID is required")
    @Email(message = "Please enter a valid staff email address")
    private String staffEmail;

    @NotBlank(message = "Staff ID is required")
    @Size(max = 100, message = "Staff ID cannot exceed 100 characters")
    private String staffId;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    public SignUpRequest() {}

    public SignUpRequest(String fullName, String staffEmail, String staffId, String department, String password) {
        this.fullName = fullName;
        this.staffEmail = staffEmail;
        this.staffId = staffId;
        this.department = department;
        this.password = password;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getStaffEmail() { return staffEmail; }
    public void setStaffEmail(String staffEmail) { this.staffEmail = staffEmail; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
