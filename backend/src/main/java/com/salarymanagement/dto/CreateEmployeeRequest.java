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
public class CreateEmployeeRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be under 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be under 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotBlank(message = "Country is required")
    private String country;

    // The currency of record is deliberately not accepted here: it is derived server-side
    // from the country by CurrencyResolver, so a caller cannot store pay figures under a
    // currency unrelated to where the employee is paid.

    @NotNull(message = "Join date is required")
    private LocalDate joinDate;

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base salary must be positive")
    private BigDecimal baseSalary;

    private BigDecimal bonus;
    private BigDecimal deductions;
}
