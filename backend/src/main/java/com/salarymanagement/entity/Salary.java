package com.salarymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "salaries", indexes = {
    @Index(name = "idx_salary_employee", columnList = "employee_id"),
    @Index(name = "idx_salary_effective_date", columnList = "effectiveDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal baseSalary;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal bonus = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal deductions = BigDecimal.ZERO;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    private LocalDate endDate;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public BigDecimal getNetSalary() {
        return baseSalary.add(bonus != null ? bonus : BigDecimal.ZERO)
                         .subtract(deductions != null ? deductions : BigDecimal.ZERO);
    }
}
