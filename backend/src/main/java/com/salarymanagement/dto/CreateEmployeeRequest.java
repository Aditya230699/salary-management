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

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 character ISO code")
    private String currency;

    @NotNull(message = "Join date is required")
    private LocalDate joinDate;

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base salary must be positive")
    private BigDecimal baseSalary;

    private BigDecimal bonus;
    private BigDecimal deductions;
}
