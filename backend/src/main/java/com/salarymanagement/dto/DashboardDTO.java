package com.salarymanagement.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {

    private long totalEmployees;
    private long activeEmployees;
    private BigDecimal averageSalary;
    private BigDecimal totalPayroll;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private Map<String, Long> employeesByDepartment;
    private Map<String, Long> employeesByCountry;
    private Map<String, BigDecimal> avgSalaryByDepartment;
    private Map<String, BigDecimal> avgSalaryByCountry;
    private Map<String, BigDecimal> payrollByDepartment;
    private Map<String, BigDecimal> payrollByCountry;
}
