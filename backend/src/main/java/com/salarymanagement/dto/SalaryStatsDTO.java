package com.salarymanagement.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Distribution of pay for one comparable group. Every figure is in a single
 * {@code currency}: mixing currencies into one average produces a number that means
 * nothing, so statistics are only ever reported within a currency.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryStatsDTO {

    private String group;
    private String currency;
    private long employeeCount;
    private BigDecimal minSalary;
    private BigDecimal percentile25;
    private BigDecimal medianSalary;
    private BigDecimal percentile75;
    private BigDecimal maxSalary;
    private BigDecimal averageSalary;
    private BigDecimal totalPayroll;
}
