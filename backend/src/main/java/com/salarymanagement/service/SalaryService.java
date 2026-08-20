package com.salarymanagement.service;

import com.salarymanagement.dto.SalaryDTO;
import com.salarymanagement.dto.UpdateSalaryRequest;
import com.salarymanagement.entity.Employee;
import com.salarymanagement.entity.Salary;
import com.salarymanagement.exception.ResourceNotFoundException;
import com.salarymanagement.exception.ValidationException;
import com.salarymanagement.repository.EmployeeRepository;
import com.salarymanagement.repository.SalaryRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public SalaryDTO updateSalary(Long employeeId, UpdateSalaryRequest request, String performedBy) {
        // Lock the parent employee before reading the current salary. This makes the
        // close-and-insert sequence atomic for one employee and prevents two requests
        // from both creating an open-ended ("current") salary row.
        Employee employee = employeeRepository.findByIdForSalaryUpdate(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        BigDecimal bonus = nullToZero(request.getBonus());
        BigDecimal deductions = nullToZero(request.getDeductions());

        if (deductions.compareTo(request.getBaseSalary().add(bonus)) > 0) {
            throw new ValidationException("Deductions cannot exceed base salary plus bonus");
        }

        // Close the current record the day before the new one takes effect. The new
        // effective date must fall strictly after the record it supersedes, otherwise the
        // superseded row would end before it began and the history becomes unreadable.
        salaryRepository.findCurrentSalaryByEmployeeId(employeeId)
                .ifPresent(currentSalary -> {
                    if (!request.getEffectiveDate().isAfter(currentSalary.getEffectiveDate())) {
                        throw new ValidationException(String.format(
                                "Effective date %s must be after the current salary's effective date %s",
                                request.getEffectiveDate(), currentSalary.getEffectiveDate()));
                    }
                    currentSalary.setEndDate(request.getEffectiveDate().minusDays(1));
                    salaryRepository.save(currentSalary);
                });

        // Create new salary record
        Salary newSalary = Salary.builder()
                .employee(employee)
                .baseSalary(request.getBaseSalary())
                .bonus(bonus)
                .deductions(deductions)
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

    private static BigDecimal nullToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
