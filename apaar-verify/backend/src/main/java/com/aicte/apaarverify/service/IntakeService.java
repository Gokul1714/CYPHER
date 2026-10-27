package com.aicte.apaarverify.service;

import com.aicte.apaarverify.model.ApprovedIntake;
import com.aicte.apaarverify.model.Institution;
import com.aicte.apaarverify.repository.ApprovedIntakeRepository;
import com.aicte.apaarverify.repository.EnrolmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class IntakeService {

    private final ApprovedIntakeRepository intakeRepository;
    private final EnrolmentRepository enrolmentRepository;

    public IntakeService(ApprovedIntakeRepository intakeRepository, EnrolmentRepository enrolmentRepository) {
        this.intakeRepository = intakeRepository;
        this.enrolmentRepository = enrolmentRepository;
    }

    public static class IntakeUtilizationDto {
        private UUID intakeId;
        private UUID institutionId;
        private String institutionName;
        private String institutionCode;
        private String courseName;
        private String department;
        private String academicYear;
        private int approvedSeats;
        private int nriQuota;
        private int supernumeraryQuota;
        private long actualEnrolled;
        private double utilizationPercentage;
        private boolean isExcess;

        public IntakeUtilizationDto() {}

        public UUID getIntakeId() { return intakeId; }
        public void setIntakeId(UUID intakeId) { this.intakeId = intakeId; }
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
        public int getApprovedSeats() { return approvedSeats; }
        public void setApprovedSeats(int approvedSeats) { this.approvedSeats = approvedSeats; }
        public int getNriQuota() { return nriQuota; }
        public void setNriQuota(int nriQuota) { this.nriQuota = nriQuota; }
        public int getSupernumeraryQuota() { return supernumeraryQuota; }
        public void setSupernumeraryQuota(int supernumeraryQuota) { this.supernumeraryQuota = supernumeraryQuota; }
        public long getActualEnrolled() { return actualEnrolled; }
        public void setActualEnrolled(long actualEnrolled) { this.actualEnrolled = actualEnrolled; }
        public double getUtilizationPercentage() { return utilizationPercentage; }
        public void setUtilizationPercentage(double utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; }
        public boolean isExcess() { return isExcess; }
        public void setExcess(boolean excess) { isExcess = excess; }
    }

    @Transactional(readOnly = true)
    public List<IntakeUtilizationDto> getIntakeUtilization(UUID institutionId, String academicYear) {
        List<ApprovedIntake> list;
        if (institutionId != null && academicYear != null) {
            list = intakeRepository.findByInstitutionIdAndAcademicYear(institutionId, academicYear);
        } else if (institutionId != null) {
            list = intakeRepository.findByInstitutionId(institutionId);
        } else {
            list = intakeRepository.findAll();
        }

        List<IntakeUtilizationDto> dtos = new ArrayList<>();
        for (ApprovedIntake item : list) {
            Institution inst = item.getInstitution();
            long count = enrolmentRepository.countActiveEnrolments(inst.getId(), item.getCourseName(), item.getAcademicYear());
            double pct = item.getApprovedSeats() > 0 ? (count * 100.0) / item.getApprovedSeats() : 0.0;

            IntakeUtilizationDto dto = new IntakeUtilizationDto();
            dto.setIntakeId(item.getId());
            dto.setInstitutionId(inst.getId());
            dto.setInstitutionName(inst.getName());
            dto.setInstitutionCode(inst.getAicteCode());
            dto.setCourseName(item.getCourseName());
            dto.setDepartment(item.getDepartment());
            dto.setAcademicYear(item.getAcademicYear());
            dto.setApprovedSeats(item.getApprovedSeats());
            dto.setNriQuota(item.getNriQuota() != null ? item.getNriQuota() : 0);
            dto.setSupernumeraryQuota(item.getSupernumeraryQuota() != null ? item.getSupernumeraryQuota() : 0);
            dto.setActualEnrolled(count);
            dto.setUtilizationPercentage(Math.round(pct * 100.0) / 100.0);
            dto.setExcess(count > item.getApprovedSeats());

            dtos.add(dto);
        }
        return dtos;
    }
}
