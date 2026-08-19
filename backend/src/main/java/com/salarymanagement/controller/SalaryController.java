package com.salarymanagement.controller;

import com.salarymanagement.dto.SalaryDTO;
import com.salarymanagement.dto.UpdateSalaryRequest;
import com.salarymanagement.service.SalaryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees/{employeeId}/salary")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @GetMapping("/current")
    public ResponseEntity<SalaryDTO> getCurrentSalary(@PathVariable Long employeeId) {
        return ResponseEntity.ok(salaryService.getCurrentSalary(employeeId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<SalaryDTO>> getSalaryHistory(@PathVariable Long employeeId) {
        return ResponseEntity.ok(salaryService.getSalaryHistory(employeeId));
    }

    @PutMapping
    public ResponseEntity<SalaryDTO> updateSalary(
            @PathVariable Long employeeId,
            @Valid @RequestBody UpdateSalaryRequest request,
            Authentication authentication) {
        SalaryDTO salary = salaryService.updateSalary(employeeId, request, authentication.getName());
        return ResponseEntity.ok(salary);
    }
}
