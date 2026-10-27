package com.aicte.apaarverify.service;

import com.aicte.apaarverify.model.*;
import com.aicte.apaarverify.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ValidationService {

    private final StudentRepository studentRepository;
    private final EnrolmentRepository enrolmentRepository;
    private final ApprovedIntakeRepository approvedIntakeRepository;
    private final ValidationResultRepository validationResultRepository;
    private final VerhoeffCheckDigit verhoeffCheckDigit;

    public ValidationService(
            StudentRepository studentRepository,
            EnrolmentRepository enrolmentRepository,
            ApprovedIntakeRepository approvedIntakeRepository,
            ValidationResultRepository validationResultRepository,
            VerhoeffCheckDigit verhoeffCheckDigit) {
        this.studentRepository = studentRepository;
        this.enrolmentRepository = enrolmentRepository;
        this.approvedIntakeRepository = approvedIntakeRepository;
        this.validationResultRepository = validationResultRepository;
        this.verhoeffCheckDigit = verhoeffCheckDigit;
    }

    @Transactional
    public List<ValidationResult> runAutomatedValidationEngine() {
        List<ValidationResult> generatedResults = new ArrayList<>();
        List<Enrolment> activeEnrolments = enrolmentRepository.findAll();

        // Map for cross-institution duplicate tracking: APAAR -> list of active enrolments
        Map<String, List<Enrolment>> apaarEnrolmentMap = new HashMap<>();

        for (Enrolment e : activeEnrolments) {
            Student s = e.getStudent();
            String apaar = s.getApaarId();

            // 1. Missing APAAR ID check
            if (apaar == null || apaar.trim().isEmpty()) {
                ValidationResult vr = createOrUpdateResult(
                        e.getInstitution(), s, e,
                        ValidationResult.Category.MISSING_APAAR,
                        ValidationResult.Severity.WARNING,
                        "Mandatory APAAR ID is missing for student " + s.getStudentName() + ". Provisional grace period active.",
                        "{\"field\": \"apaarId\"}"
                );
                generatedResults.add(vr);
            } else {
                // 2. Format & Verhoeff check
                boolean isValid12 = apaar.matches("\\d{12}");
                boolean isValidVerhoeff = isValid12 && verhoeffCheckDigit.validate(apaar);

                if (!isValid12 || !isValidVerhoeff) {
                    ValidationResult vr = createOrUpdateResult(
                            e.getInstitution(), s, e,
                            ValidationResult.Category.INVALID_APAAR_FORMAT,
                            ValidationResult.Severity.ERROR,
                            "APAAR ID '" + apaar + "' failed 12-digit standard or Verhoeff check digit verification.",
                            "{\"apaarId\": \"" + apaar + "\", \"syntax12\": " + isValid12 + ", \"verhoeff\": " + isValidVerhoeff + "}"
                    );
                    generatedResults.add(vr);
                } else {
                    // Collect for cross-institution check
                    apaarEnrolmentMap.computeIfAbsent(apaar, k -> new ArrayList<>()).add(e);
                }
            }
        }

        // 3. Duplicate checks (Intra and Inter-Institution)
        for (Map.Entry<String, List<Enrolment>> entry : apaarEnrolmentMap.entrySet()) {
            List<Enrolment> list = entry.getValue();
            if (list.size() > 1) {
                Set<UUID> institutionIds = new HashSet<>();
                for (Enrolment e : list) {
                    institutionIds.add(e.getInstitution().getId());
                }

                // If enrolled in multiple DIFFERENT institutions simultaneously
                if (institutionIds.size() > 1) {
                    for (Enrolment e : list) {
                        ValidationResult vr = createOrUpdateResult(
                                e.getInstitution(), e.getStudent(), e,
                                ValidationResult.Category.INTER_INSTITUTION_DUPLICATE,
                                ValidationResult.Severity.CRITICAL,
                                "Potential active dual enrolment detected across institutions for APAAR " + entry.getKey() + ". Review required: verify bona fide transfer or migration.",
                                "{\"matchedInstitutionsCount\": " + institutionIds.size() + ", \"apaarId\": \"" + entry.getKey() + "\"}"
                        );
                        generatedResults.add(vr);
                    }
                } else {
                    // Enrolled twice within the SAME institution
                    for (Enrolment e : list) {
                        ValidationResult vr = createOrUpdateResult(
                                e.getInstitution(), e.getStudent(), e,
                                ValidationResult.Category.INTRA_INSTITUTION_DUPLICATE,
                                ValidationResult.Severity.ERROR,
                                "Duplicate active enrolment detected within same institution for APAAR " + entry.getKey(),
                                "{\"apaarId\": \"" + entry.getKey() + "\"}"
                        );
                        generatedResults.add(vr);
                    }
                }
            }
        }

        // 4. Sanctioned Intake vs Actual Enrolment Check
        List<ApprovedIntake> allIntake = approvedIntakeRepository.findAll();
        for (ApprovedIntake intake : allIntake) {
            long currentEnrolled = enrolmentRepository.countActiveEnrolments(
                    intake.getInstitution().getId(),
                    intake.getCourseName(),
                    intake.getAcademicYear()
            );

            if (currentEnrolled > intake.getApprovedSeats()) {
                ValidationResult vr = createOrUpdateResult(
                        intake.getInstitution(), null, null,
                        ValidationResult.Category.EXCESS_ADMISSION_INTAKE,
                        ValidationResult.Severity.CRITICAL,
                        "Admitted students (" + currentEnrolled + ") exceed AICTE sanctioned intake (" + intake.getApprovedSeats() + ") for course: " + intake.getCourseName(),
                        "{\"approved\": " + intake.getApprovedSeats() + ", \"actual\": " + currentEnrolled + "}"
                );
                generatedResults.add(vr);
            }
        }

        return generatedResults;
    }

    @Transactional
    public ValidationResult resolveAnomaly(UUID anomalyId, String resolutionNotes, User resolver) {
        ValidationResult vr = validationResultRepository.findById(anomalyId)
                .orElseThrow(() -> new IllegalArgumentException("Anomaly not found with ID: " + anomalyId));
        vr.setResolved(true);
        vr.setResolutionNotes(resolutionNotes);
        vr.setResolvedBy(resolver);
        return validationResultRepository.save(vr);
    }

    private ValidationResult createOrUpdateResult(
            Institution inst, Student s, Enrolment e,
            ValidationResult.Category category, ValidationResult.Severity severity,
            String message, String details) {

        ValidationResult vr = new ValidationResult();
        vr.setInstitution(inst);
        vr.setStudent(s);
        vr.setEnrolment(e);
        vr.setRuleCategory(category);
        vr.setSeverity(severity);
        vr.setMessage(message);
        vr.setDetails(details);
        vr.setResolved(false);
        return validationResultRepository.save(vr);
    }
}
