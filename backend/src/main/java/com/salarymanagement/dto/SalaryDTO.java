package com.salarymanagement.dto;

import com.salarymanagement.entity.Salary;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryDTO {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private BigDecimal baseSalary;
    private BigDecimal bonus;
    private BigDecimal deductions;
    private BigDecimal netSalary;
    private String currency;
    private LocalDate effectiveDate;
    private LocalDate endDate;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;

    public static SalaryDTO fromEntity(Salary salary) {
        return SalaryDTO.builder()
                .id(salary.getId())
                .employeeId(salary.getEmployee().getId())
                .employeeName(salary.getEmployee().getFirstName() + " " + salary.getEmployee().getLastName())
                .baseSalary(salary.getBaseSalary())
                .bonus(salary.getBonus())
                .deductions(salary.getDeductions())
                .netSalary(salary.getNetSalary())
                .currency(salary.getCurrency())
                .effectiveDate(salary.getEffectiveDate())
                .endDate(salary.getEndDate())
                .notes(salary.getNotes())
                .createdBy(salary.getCreatedBy())
                .createdAt(salary.getCreatedAt())
                .build();
    }
}
