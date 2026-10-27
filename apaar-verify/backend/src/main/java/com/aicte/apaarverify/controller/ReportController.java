package com.aicte.apaarverify.controller;

import com.aicte.apaarverify.service.AuditLogService;
import com.aicte.apaarverify.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;
    private final AuditLogService auditLogService;

    public ReportController(ReportService reportService, AuditLogService auditLogService) {
        this.reportService = reportService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/enrolment/csv")
    public ResponseEntity<byte[]> downloadEnrolmentCsv(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) String academicYear,
            HttpServletRequest httpRequest) throws Exception {

        byte[] csvData = reportService.generateStudentEnrolmentCsv(institutionId, academicYear);
        auditLogService.recordLog(null, "OPERATOR", institutionId, "EXPORT_REPORT_CSV", "REPORT", "ENROLMENT_CSV", httpRequest.getRemoteAddr(), "Downloaded Student Enrolment CSV");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"aicte_student_enrolments.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    @GetMapping("/enrolment/pdf")
    public ResponseEntity<byte[]> downloadEnrolmentPdf(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) String academicYear,
            HttpServletRequest httpRequest) throws Exception {

        byte[] pdfData = reportService.generateStudentEnrolmentPdf(institutionId, academicYear);
        auditLogService.recordLog(null, "OPERATOR", institutionId, "EXPORT_REPORT_PDF", "REPORT", "ENROLMENT_PDF", httpRequest.getRemoteAddr(), "Downloaded Student Enrolment PDF");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"aicte_student_enrolments.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }

    @GetMapping("/intake/csv")
    public ResponseEntity<byte[]> downloadIntakeCsv(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) String academicYear,
            HttpServletRequest httpRequest) throws Exception {

        byte[] csvData = reportService.generateIntakeComplianceCsv(institutionId, academicYear);
        auditLogService.recordLog(null, "OPERATOR", institutionId, "EXPORT_REPORT_CSV", "REPORT", "INTAKE_CSV", httpRequest.getRemoteAddr(), "Downloaded Intake Compliance CSV");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"aicte_intake_compliance.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    @GetMapping("/intake/pdf")
    public ResponseEntity<byte[]> downloadIntakePdf(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) String academicYear,
            HttpServletRequest httpRequest) throws Exception {

        byte[] pdfData = reportService.generateIntakeCompliancePdf(institutionId, academicYear);
        auditLogService.recordLog(null, "OPERATOR", institutionId, "EXPORT_REPORT_PDF", "REPORT", "INTAKE_PDF", httpRequest.getRemoteAddr(), "Downloaded Intake Compliance PDF");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"aicte_intake_compliance.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }

    @GetMapping("/anomalies/csv")
    public ResponseEntity<byte[]> downloadAnomaliesCsv(
            @RequestParam(required = false) UUID institutionId,
            HttpServletRequest httpRequest) throws Exception {

        byte[] csvData = reportService.generateAnomaliesCsv(institutionId);
        auditLogService.recordLog(null, "OPERATOR", institutionId, "EXPORT_REPORT_CSV", "REPORT", "ANOMALIES_CSV", httpRequest.getRemoteAddr(), "Downloaded Validation Anomalies CSV");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"aicte_validation_anomalies.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
