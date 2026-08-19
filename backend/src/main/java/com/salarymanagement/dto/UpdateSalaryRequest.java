package com.salarymanagement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSalaryRequest {

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base salary must be positive")
    private BigDecimal baseSalary;

    @DecimalMin(value = "0.0", message = "Bonus cannot be negative")
    private BigDecimal bonus;

    @DecimalMin(value = "0.0", message = "Deductions cannot be negative")
    private BigDecimal deductions;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    @Size(max = 500, message = "Notes must be under 500 characters")
    private String notes;
}
