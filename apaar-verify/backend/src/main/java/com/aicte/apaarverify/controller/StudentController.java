package com.aicte.apaarverify.controller;

import com.aicte.apaarverify.dto.StudentCreateRequest;
import com.aicte.apaarverify.dto.StudentResponseDto;
import com.aicte.apaarverify.service.AuditLogService;
import com.aicte.apaarverify.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;
    private final AuditLogService auditLogService;

    public StudentController(StudentService studentService, AuditLogService auditLogService) {
        this.studentService = studentService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<StudentResponseDto>> getStudents(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) String academicYear) {
        List<StudentResponseDto> students = studentService.getStudents(institutionId, academicYear);
        return ResponseEntity.ok(students);
    }

    @PostMapping
    public ResponseEntity<StudentResponseDto> createStudent(
            @RequestBody StudentCreateRequest request,
            HttpServletRequest httpRequest) {
        StudentResponseDto created = studentService.createStudent(request);
        auditLogService.recordLog(
                null, "OPERATOR", request.getInstitutionId(),
                "CREATE_STUDENT", "STUDENT", created.getStudentId().toString(),
                httpRequest.getRemoteAddr(), "Created student: " + created.getStudentName()
        );
        return ResponseEntity.ok(created);
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> bulkUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("institutionId") UUID institutionId,
            HttpServletRequest httpRequest) {
        try {
            List<StudentResponseDto> ingested = studentService.bulkUploadCsv(file, institutionId);
            auditLogService.recordLog(
                    null, "OPERATOR", institutionId,
                    "BULK_CSV_INGESTION", "STUDENTS", null,
                    httpRequest.getRemoteAddr(), "Ingested " + ingested.size() + " student records from CSV"
            );
            return ResponseEntity.ok(ingested);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error parsing CSV: " + e.getMessage());
        }
    }
}
