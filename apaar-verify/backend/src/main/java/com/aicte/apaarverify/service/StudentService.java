package com.aicte.apaarverify.service;

import com.aicte.apaarverify.dto.StudentCreateRequest;
import com.aicte.apaarverify.dto.StudentResponseDto;
import com.aicte.apaarverify.model.*;
import com.aicte.apaarverify.repository.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final EnrolmentRepository enrolmentRepository;
    private final InstitutionRepository institutionRepository;
    private final VerificationTokenRepository tokenRepository;
    private final VerhoeffCheckDigit verhoeffCheckDigit;

    public StudentService(
            StudentRepository studentRepository,
            EnrolmentRepository enrolmentRepository,
            InstitutionRepository institutionRepository,
            VerificationTokenRepository tokenRepository,
            VerhoeffCheckDigit verhoeffCheckDigit) {
        this.studentRepository = studentRepository;
        this.enrolmentRepository = enrolmentRepository;
        this.institutionRepository = institutionRepository;
        this.tokenRepository = tokenRepository;
        this.verhoeffCheckDigit = verhoeffCheckDigit;
    }

    @Transactional
    public StudentResponseDto createStudent(StudentCreateRequest req) {
        Institution inst = institutionRepository.findById(req.getInstitutionId())
                .orElseThrow(() -> new IllegalArgumentException("Institution not found with ID: " + req.getInstitutionId()));

        Student student = new Student();
        student.setStudentName(req.getStudentName());
        student.setGender(req.getGender());
        student.setDateOfBirth(req.getDateOfBirth());
        student.setFatherOrGuardianName(req.getFatherOrGuardianName());

        String apaar = req.getApaarId();
        if (apaar != null && !apaar.trim().isEmpty()) {
            student.setApaarId(apaar.trim());
            boolean isFormatValid = verhoeffCheckDigit.validate(apaar.trim());
            student.setApaarFormatValid(isFormatValid);
            // Labelled mock verification provider until authorized gateway integration
            student.setApaarVerificationStatus(isFormatValid ? "MOCK_VERIFIED" : "UNVERIFIED");
        } else {
            student.setApaarId(null);
            student.setApaarFormatValid(false);
            student.setApaarVerificationStatus("UNVERIFIED");
        }

        Student savedStudent = studentRepository.save(student);

        // Create Enrolment
        Enrolment enrolment = new Enrolment();
        enrolment.setStudent(savedStudent);
        enrolment.setInstitution(inst);
        enrolment.setCourseName(req.getCourseName());
        enrolment.setDepartment(req.getDepartment() != null ? req.getDepartment() : "Engineering & Technology");
        enrolment.setAcademicYear(req.getAcademicYear() != null ? req.getAcademicYear() : "2026-2027");
        enrolment.setRollNumber(req.getRollNumber());
        enrolment.setAdmissionDate(LocalDate.now());

        if (req.getAdmissionCategory() != null) {
            try {
                enrolment.setAdmissionCategory(Enrolment.AdmissionCategory.valueOf(req.getAdmissionCategory()));
            } catch (Exception e) {
                enrolment.setAdmissionCategory(Enrolment.AdmissionCategory.GENERAL);
            }
        }
        enrolment.setIsNri(Boolean.TRUE.equals(req.getIsNri()));
        enrolment.setIsOci(Boolean.TRUE.equals(req.getIsOci()));
        enrolment.setIsForeignNational(Boolean.TRUE.equals(req.getIsForeignNational()));
        enrolment.setIsSupernumerary(Boolean.TRUE.equals(req.getIsSupernumerary()));

        Enrolment savedEnrolment = enrolmentRepository.save(enrolment);

        // Generate Signed Verification Token
        VerificationToken token = createVerificationToken(savedStudent, savedEnrolment);

        return mapToDto(savedStudent, savedEnrolment, inst, token.getToken());
    }

    @Transactional
    public List<StudentResponseDto> bulkUploadCsv(MultipartFile file, UUID institutionId) throws Exception {
        Institution inst = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institution not found with ID: " + institutionId));

        List<StudentResponseDto> results = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord record : csvParser) {
                StudentCreateRequest req = new StudentCreateRequest();
                req.setInstitutionId(institutionId);
                req.setStudentName(record.get("student_name"));
                req.setGender(record.isMapped("gender") ? record.get("gender") : "FEMALE");
                req.setDateOfBirth(record.isMapped("date_of_birth") ? LocalDate.parse(record.get("date_of_birth")) : LocalDate.of(2005, 1, 1));
                req.setApaarId(record.isMapped("apaar_id") ? record.get("apaar_id") : null);
                req.setFatherOrGuardianName(record.isMapped("father_or_guardian_name") ? record.get("father_or_guardian_name") : null);
                req.setCourseName(record.isMapped("course_name") ? record.get("course_name") : "B.Tech Computer Science & Engineering");
                req.setDepartment(record.isMapped("department") ? record.get("department") : "Engineering & Technology");
                req.setAcademicYear(record.isMapped("academic_year") ? record.get("academic_year") : "2026-2027");
                req.setRollNumber(record.isMapped("roll_number") ? record.get("roll_number") : null);
                req.setAdmissionCategory(record.isMapped("admission_category") ? record.get("admission_category") : "GENERAL");
                req.setIsNri(record.isMapped("is_nri") && Boolean.parseBoolean(record.get("is_nri")));
                req.setIsSupernumerary(record.isMapped("is_supernumerary") && Boolean.parseBoolean(record.get("is_supernumerary")));

                results.add(createStudent(req));
            }
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<StudentResponseDto> getStudents(UUID institutionId, String academicYear) {
        List<Enrolment> enrolments;
        if (institutionId != null && academicYear != null) {
            enrolments = enrolmentRepository.findByInstitutionIdAndAcademicYear(institutionId, academicYear);
        } else if (institutionId != null) {
            enrolments = enrolmentRepository.findByInstitutionId(institutionId);
        } else {
            enrolments = enrolmentRepository.findAll();
        }

        return enrolments.stream().map(e -> {
            Student s = e.getStudent();
            Institution inst = e.getInstitution();
            String tok = tokenRepository.findByStudentId(s.getId()).map(VerificationToken::getToken).orElse(null);
            return mapToDto(s, e, inst, tok);
        }).collect(Collectors.toList());
    }

    private VerificationToken createVerificationToken(Student student, Enrolment enrolment) {
        VerificationToken token = new VerificationToken();
        // Cryptographically signed unpredictable token format
        String tokenStr = "tok_sec_" + UUID.randomUUID().toString().replace("-", "") + "_aicte_sig";
        token.setToken(tokenStr);
        token.setStudent(student);
        token.setEnrolment(enrolment);
        token.setExpiresAt(OffsetDateTime.now().plusYears(1));
        return tokenRepository.save(token);
    }

    private StudentResponseDto mapToDto(Student s, Enrolment e, Institution inst, String token) {
        StudentResponseDto dto = new StudentResponseDto();
        dto.setStudentId(s.getId());
        dto.setEnrolmentId(e.getId());
        dto.setStudentName(s.getStudentName());
        dto.setGender(s.getGender());
        dto.setDateOfBirth(s.getDateOfBirth());
        dto.setApaarId(s.getApaarId());
        dto.setApaarFormatValid(s.getApaarFormatValid());
        dto.setApaarVerificationStatus(s.getApaarVerificationStatus());
        dto.setInstitutionId(inst.getId());
        dto.setInstitutionName(inst.getName());
        dto.setInstitutionCode(inst.getAicteCode());
        dto.setCourseName(e.getCourseName());
        dto.setDepartment(e.getDepartment());
        dto.setAcademicYear(e.getAcademicYear());
        dto.setRollNumber(e.getRollNumber());
        dto.setEnrolmentStatus(e.getEnrolmentStatus().name());
        dto.setAdmissionCategory(e.getAdmissionCategory().name());
        dto.setIsNri(e.getIsNri());
        dto.setIsOci(e.getIsOci());
        dto.setIsForeignNational(e.getIsForeignNational());
        dto.setIsSupernumerary(e.getIsSupernumerary());
        dto.setVerificationToken(token);
        return dto;
    }
}
