package com.aicte.apaarverify.dto;

import java.util.UUID;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private UUID userId;
    private String username;
    private String fullName;
    private String role;
    private UUID institutionId;
    private String institutionName;
    private String institutionCode;

    public AuthResponse() {}

    public AuthResponse(String token, UUID userId, String username, String fullName, String role, UUID institutionId, String institutionName, String institutionCode) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.institutionId = institutionId;
        this.institutionName = institutionName;
        this.institutionCode = institutionCode;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public UUID getInstitutionId() { return institutionId; }
    public void setInstitutionId(UUID institutionId) { this.institutionId = institutionId; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public String getInstitutionCode() { return institutionCode; }
    public void setInstitutionCode(String institutionCode) { this.institutionCode = institutionCode; }
}
