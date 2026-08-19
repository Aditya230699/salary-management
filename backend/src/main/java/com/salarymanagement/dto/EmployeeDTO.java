package com.salarymanagement.dto;

import com.salarymanagement.entity.Employee;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {

    private Long id;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String designation;
    private String departmentName;
    private String country;
    private String currency;
    private LocalDate joinDate;
    private String status;
    private BigDecimal currentSalary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EmployeeDTO fromEntity(Employee employee) {
        return EmployeeDTO.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .designation(employee.getDesignation())
                .departmentName(employee.getDepartment().getName())
                .country(employee.getCountry())
                .currency(employee.getCurrency())
                .joinDate(employee.getJoinDate())
                .status(employee.getStatus().name())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }
}
