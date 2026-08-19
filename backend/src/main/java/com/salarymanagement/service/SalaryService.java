package com.salarymanagement.service;

import com.salarymanagement.dto.SalaryDTO;
import com.salarymanagement.dto.UpdateSalaryRequest;
import com.salarymanagement.entity.Employee;
import com.salarymanagement.entity.Salary;
import com.salarymanagement.exception.ResourceNotFoundException;
import com.salarymanagement.repository.EmployeeRepository;
import com.salarymanagement.repository.SalaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SalaryService {

    private final SalaryRepository salaryRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    public SalaryService(SalaryRepository salaryRepository,
                         EmployeeRepository employeeRepository,
                         AuditService auditService) {
        this.salaryRepository = salaryRepository;
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public SalaryDTO getCurrentSalary(Long employeeId) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        Salary salary = salaryRepository.findCurrentSalaryByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("No current salary found for employee: " + employeeId));
        return SalaryDTO.fromEntity(salary);
    }

    @Transactional(readOnly = true)
    public List<SalaryDTO> getSalaryHistory(Long employeeId) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        return salaryRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId)
                .stream()
                .map(SalaryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public SalaryDTO updateSalary(Long employeeId, UpdateSalaryRequest request, String performedBy) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        // Close current salary record
        salaryRepository.findCurrentSalaryByEmployeeId(employeeId)
                .ifPresent(currentSalary -> {
                    currentSalary.setEndDate(request.getEffectiveDate().minusDays(1));
                    salaryRepository.save(currentSalary);
                });

        // Create new salary record
        Salary newSalary = Salary.builder()
                .employee(employee)
                .baseSalary(request.getBaseSalary())
                .bonus(request.getBonus())
                .deductions(request.getDeductions())
                .currency(employee.getCurrency())
                .effectiveDate(request.getEffectiveDate())
                .notes(request.getNotes())
                .createdBy(performedBy)
                .build();

        newSalary = salaryRepository.save(newSalary);

        auditService.log(employeeId, "SALARY_UPDATED",
                String.format("Salary updated: base=%s, bonus=%s, deductions=%s, effective=%s",
                        request.getBaseSalary(), request.getBonus(), request.getDeductions(), request.getEffectiveDate()),
                performedBy);

        return SalaryDTO.fromEntity(newSalary);
    }
}
