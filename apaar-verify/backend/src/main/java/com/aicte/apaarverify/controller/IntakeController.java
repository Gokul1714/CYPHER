package com.aicte.apaarverify.controller;

import com.aicte.apaarverify.service.IntakeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/intake")
public class IntakeController {

    private final IntakeService intakeService;

    public IntakeController(IntakeService intakeService) {
        this.intakeService = intakeService;
    }

    @GetMapping
    public ResponseEntity<List<IntakeService.IntakeUtilizationDto>> getIntake(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(intakeService.getIntakeUtilization(institutionId, academicYear));
    }
}
