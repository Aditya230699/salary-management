package com.salarymanagement.controller;

import com.salarymanagement.dto.CreateEmployeeRequest;
import com.salarymanagement.dto.EmployeeDTO;
import com.salarymanagement.dto.UpdateEmployeeRequest;
import com.salarymanagement.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> getEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<EmployeeDTO> employees = employeeService.getEmployees(
                search, department, country, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request,
            Authentication authentication) {
        EmployeeDTO employee = employeeService.createEmployee(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request,
            Authentication authentication) {
        EmployeeDTO employee = employeeService.updateEmployee(id, request, authentication.getName());
        return ResponseEntity.ok(employee);
    }
}
