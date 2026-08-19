package com.salarymanagement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEmployeeRequest {

    @Size(max = 100, message = "First name must be under 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must be under 100 characters")
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    private String designation;
    private Long departmentId;
    private String country;

    @Size(min = 3, max = 3, message = "Currency must be 3 character ISO code")
    private String currency;

    private String status;
}
