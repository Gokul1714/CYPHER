package com.aicte.apaarverify.service;

import com.aicte.apaarverify.model.*;
import com.aicte.apaarverify.repository.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportService {

    private final StudentRepository studentRepository;
    private final EnrolmentRepository enrolmentRepository;
    private final ApprovedIntakeRepository intakeRepository;
    private final ValidationResultRepository validationResultRepository;
    private final InstitutionRepository institutionRepository;

    public ReportService(
            StudentRepository studentRepository,
            EnrolmentRepository enrolmentRepository,
            ApprovedIntakeRepository intakeRepository,
            ValidationResultRepository validationResultRepository,
            InstitutionRepository institutionRepository) {
        this.studentRepository = studentRepository;
        this.enrolmentRepository = enrolmentRepository;
        this.intakeRepository = intakeRepository;
        this.validationResultRepository = validationResultRepository;
        this.institutionRepository = institutionRepository;
    }

    // =========================================================================
    // 1. STUDENT ENROLMENT ROSTER REPORT
    // =========================================================================

    @Transactional(readOnly = true)
    public byte[] generateStudentEnrolmentCsv(UUID institutionId, String academicYear) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.withHeader("Student ID", "Full Legal Name", "Gender", "Date of Birth",
                        "APAAR ID (Masked)", "Institution Code", "Institution Name", "Course",
                        "Academic Year", "Roll Number", "Category", "Enrolment Status", "Verification Status"))) {

            List<Enrolment> enrolments = getFilteredEnrolments(institutionId, academicYear);
            for (Enrolment e : enrolments) {
                Student s = e.getStudent();
                Institution inst = e.getInstitution();
                String maskedApaar = maskApaar(s.getApaarId());

                printer.printRecord(
                        s.getId(),
                        s.getStudentName(),
                        s.getGender(),
                        s.getDateOfBirth(),
                        maskedApaar,
                        inst.getAicteCode(),
                        inst.getName(),
                        e.getCourseName(),
                        e.getAcademicYear(),
                        e.getRollNumber(),
                        e.getAdmissionCategory(),
                        e.getEnrolmentStatus(),
                        s.getApaarVerificationStatus()
                );
            }
        }
        return out.toByteArray();
    }

    @Transactional(readOnly = true)
    public byte[] generateStudentEnrolmentPdf(UUID institutionId, String academicYear) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Header Styling
        addAicteReportHeader(document, "STUDENT ENROLMENT MASTER ROSTER",
                "Official Statutory Report under AICTE APAAR Framework (AY: " + (academicYear != null ? academicYear : "2026-2027") + ")");

        Table table = new Table(UnitValue.createPercentArray(new float[]{25f, 20f, 25f, 15f, 15f})).useAllAvailableWidth();
        table.addHeaderCell(createHeaderCell("Student Name"));
        table.addHeaderCell(createHeaderCell("APAAR ID"));
        table.addHeaderCell(createHeaderCell("Course & Dept"));
        table.addHeaderCell(createHeaderCell("Category"));
        table.addHeaderCell(createHeaderCell("Status"));

        List<Enrolment> enrolments = getFilteredEnrolments(institutionId, academicYear);
        for (Enrolment e : enrolments) {
            Student s = e.getStudent();
            table.addCell(new Cell().add(new Paragraph(s.getStudentName()).setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(maskApaar(s.getApaarId())).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(e.getCourseName()).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(e.getAdmissionCategory().name()).setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(e.getEnrolmentStatus().name()).setFontSize(8)));
        }

        document.add(table);
        addReportFooter(document, enrolments.size() + " Total Student Enrolments Reported.");
        document.close();

        return out.toByteArray();
    }

    // =========================================================================
    // 2. INTAKE VS ACTUAL COMPLIANCE REPORT
    // =========================================================================

    @Transactional(readOnly = true)
    public byte[] generateIntakeComplianceCsv(UUID institutionId, String academicYear) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.withHeader("Institution Code", "Institution Name", "Course", "Department",
                        "Academic Year", "Sanctioned Seats", "Actual Admitted", "NRI Quota",
                        "Supernumerary Quota", "Utilization %", "Compliance Status"))) {

            List<ApprovedIntake> intakes = getFilteredIntakes(institutionId, academicYear);
            for (ApprovedIntake intake : intakes) {
                Institution inst = intake.getInstitution();
                long count = enrolmentRepository.countActiveEnrolments(inst.getId(), intake.getCourseName(), intake.getAcademicYear());
                double pct = intake.getApprovedSeats() > 0 ? (count * 100.0) / intake.getApprovedSeats() : 0.0;
                String status = count > intake.getApprovedSeats() ? "EXCESS_ADMISSION_BREACH" : "COMPLIANT";

                printer.printRecord(
                        inst.getAicteCode(),
                        inst.getName(),
                        intake.getCourseName(),
                        intake.getDepartment(),
                        intake.getAcademicYear(),
                        intake.getApprovedSeats(),
                        count,
                        intake.getNriQuota(),
                        intake.getSupernumeraryQuota(),
                        String.format("%.1f%%", pct),
                        status
                );
            }
        }
        return out.toByteArray();
    }

    @Transactional(readOnly = true)
    public byte[] generateIntakeCompliancePdf(UUID institutionId, String academicYear) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        addAicteReportHeader(document, "SANCTIONED INTAKE VS ACTUAL ADMISSIONS AUDIT",
                "Statutory Inspection Dossier for AICTE Approved Quotas (AY: " + (academicYear != null ? academicYear : "2026-2027") + ")");

        Table table = new Table(UnitValue.createPercentArray(new float[]{30f, 15f, 15f, 20f, 20f})).useAllAvailableWidth();
        table.addHeaderCell(createHeaderCell("Course & Dept"));
        table.addHeaderCell(createHeaderCell("Approved Seats"));
        table.addHeaderCell(createHeaderCell("Actual Admitted"));
        table.addHeaderCell(createHeaderCell("Quotas (NRI/Super)"));
        table.addHeaderCell(createHeaderCell("Compliance Status"));

        List<ApprovedIntake> intakes = getFilteredIntakes(institutionId, academicYear);
        for (ApprovedIntake intake : intakes) {
            Institution inst = intake.getInstitution();
            long count = enrolmentRepository.countActiveEnrolments(inst.getId(), intake.getCourseName(), intake.getAcademicYear());
            boolean isExcess = count > intake.getApprovedSeats();

            table.addCell(new Cell().add(new Paragraph(intake.getCourseName()).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(intake.getApprovedSeats())).setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(count)).setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(intake.getNriQuota() + " NRI / " + intake.getSupernumeraryQuota() + " Super").setFontSize(8)));

            Cell statusCell = new Cell().add(new Paragraph(isExcess ? "EXCESS BREACH" : "COMPLIANT").setFontSize(8));
            if (isExcess) {
                statusCell.setBackgroundColor(new DeviceRgb(254, 226, 226));
            } else {
                statusCell.setBackgroundColor(new DeviceRgb(220, 252, 231));
            }
            table.addCell(statusCell);
        }

        document.add(table);
        addReportFooter(document, "Generated by CYPHER Regulatory Engine.");
        document.close();

        return out.toByteArray();
    }

    // =========================================================================
    // 3. ANOMALIES & POTENTIAL DUPLICATE ENROLMENTS REPORT
    // =========================================================================

    @Transactional(readOnly = true)
    public byte[] generateAnomaliesCsv(UUID institutionId) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.withHeader("Finding ID", "Severity", "Category", "Student Name",
                        "APAAR ID (Masked)", "Institution", "Description", "Resolved Status"))) {

            List<ValidationResult> results = (institutionId != null) ?
                    validationResultRepository.findByInstitutionId(institutionId) :
                    validationResultRepository.findAll();

            for (ValidationResult vr : results) {
                String name = vr.getStudent() != null ? vr.getStudent().getStudentName() : "Institution Level";
                String apaar = vr.getStudent() != null ? maskApaar(vr.getStudent().getApaarId()) : "N/A";
                String instName = vr.getInstitution() != null ? vr.getInstitution().getName() : "AICTE Central";

                printer.printRecord(
                        vr.getId(),
                        vr.getSeverity(),
                        vr.getRuleCategory(),
                        name,
                        apaar,
                        instName,
                        vr.getMessage(),
                        vr.getResolved() ? "RESOLVED" : "PENDING_ACTION"
                );
            }
        }
        return out.toByteArray();
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private List<Enrolment> getFilteredEnrolments(UUID institutionId, String academicYear) {
        if (institutionId != null && academicYear != null) {
            return enrolmentRepository.findByInstitutionIdAndAcademicYear(institutionId, academicYear);
        } else if (institutionId != null) {
            return enrolmentRepository.findByInstitutionId(institutionId);
        }
        return enrolmentRepository.findAll();
    }

    private List<ApprovedIntake> getFilteredIntakes(UUID institutionId, String academicYear) {
        if (institutionId != null && academicYear != null) {
            return intakeRepository.findByInstitutionIdAndAcademicYear(institutionId, academicYear);
        } else if (institutionId != null) {
            return intakeRepository.findByInstitutionId(institutionId);
        }
        return intakeRepository.findAll();
    }

    private String maskApaar(String apaarId) {
        if (apaarId == null || apaarId.trim().isEmpty()) {
            return "NOT_PROVIDED";
        }
        if (apaarId.length() >= 8) {
            return apaarId.substring(0, 4) + "-XXXX-" + apaarId.substring(apaarId.length() - 4);
        }
        return apaarId;
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontSize(9).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(new DeviceRgb(15, 43, 92)) // Navy blue #0f2b5c
                .setTextAlignment(TextAlignment.LEFT);
    }

    private void addAicteReportHeader(Document doc, String title, String subtitle) {
        doc.add(new Paragraph("ALL INDIA COUNCIL FOR TECHNICAL EDUCATION")
                .setBold()
                .setFontSize(14)
                .setFontColor(new DeviceRgb(15, 43, 92))
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph("Ministry of Education, Government of India • AICTE Regulatory Officer Portal")
                .setFontSize(9)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph(title)
                .setBold()
                .setFontSize(12)
                .setFontColor(new DeviceRgb(234, 88, 12)) // Saffron accent
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph(subtitle)
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(12));
    }

    private void addReportFooter(Document doc, String notes) {
        doc.add(new Paragraph(notes)
                .setFontSize(8)
                .setItalic()
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginTop(10));

        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        doc.add(new Paragraph("Generated at: " + timestamp + " | Confidential Government Statutory Record")
                .setFontSize(7)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.RIGHT));
    }
}
