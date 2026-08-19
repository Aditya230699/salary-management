package com.salarymanagement.service;

import com.salarymanagement.dto.CreateEmployeeRequest;
import com.salarymanagement.dto.EmployeeDTO;
import com.salarymanagement.dto.UpdateEmployeeRequest;
import com.salarymanagement.entity.Department;
import com.salarymanagement.entity.Employee;
import com.salarymanagement.entity.Salary;
import com.salarymanagement.exception.DuplicateResourceException;
import com.salarymanagement.exception.ResourceNotFoundException;
import com.salarymanagement.repository.DepartmentRepository;
import com.salarymanagement.repository.EmployeeRepository;
import com.salarymanagement.repository.SalaryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final SalaryRepository salaryRepository;
    private final AuditService auditService;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           SalaryRepository salaryRepository,
                           AuditService auditService) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.salaryRepository = salaryRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getEmployees(String search, String department, String country,
                                           String status, int page, int size, String sortBy, String sortDir) {
        Employee.EmployeeStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            statusEnum = Employee.EmployeeStatus.valueOf(status.toUpperCase());
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Employee> employees = employeeRepository.findWithFilters(
                search, department, country, statusEnum, pageable);

        return employees.map(emp -> {
            EmployeeDTO dto = EmployeeDTO.fromEntity(emp);
            salaryRepository.findCurrentSalaryByEmployeeId(emp.getId())
                    .ifPresent(salary -> dto.setCurrentSalary(salary.getBaseSalary()));
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        EmployeeDTO dto = EmployeeDTO.fromEntity(employee);
        salaryRepository.findCurrentSalaryByEmployeeId(id)
                .ifPresent(salary -> dto.setCurrentSalary(salary.getBaseSalary()));
        return dto;
    }

    public EmployeeDTO createEmployee(CreateEmployeeRequest request, String performedBy) {
        if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Employee with email already exists: " + request.getEmail());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        Employee employee = Employee.builder()
                .employeeId(generateEmployeeId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .designation(request.getDesignation())
                .department(department)
                .country(request.getCountry())
                .currency(request.getCurrency())
                .joinDate(request.getJoinDate())
                .status(Employee.EmployeeStatus.ACTIVE)
                .build();

        employee = employeeRepository.save(employee);

        // Create initial salary record
        Salary salary = Salary.builder()
                .employee(employee)
                .baseSalary(request.getBaseSalary())
                .bonus(request.getBonus() != null ? request.getBonus() : BigDecimal.ZERO)
                .deductions(request.getDeductions() != null ? request.getDeductions() : BigDecimal.ZERO)
                .currency(request.getCurrency())
                .effectiveDate(request.getJoinDate())
                .createdBy(performedBy)
                .build();
        salaryRepository.save(salary);

        auditService.log(employee.getId(), "EMPLOYEE_CREATED",
                "Employee created: " + employee.getFirstName() + " " + employee.getLastName(), performedBy);

        EmployeeDTO dto = EmployeeDTO.fromEntity(employee);
        dto.setCurrentSalary(request.getBaseSalary());
        return dto;
    }

    public EmployeeDTO updateEmployee(Long id, UpdateEmployeeRequest request, String performedBy) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        StringBuilder changes = new StringBuilder();

        if (request.getFirstName() != null) {
            changes.append("firstName: ").append(employee.getFirstName()).append(" -> ").append(request.getFirstName()).append("; ");
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            changes.append("lastName: ").append(employee.getLastName()).append(" -> ").append(request.getLastName()).append("; ");
            employee.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            employeeRepository.findByEmail(request.getEmail())
                    .filter(e -> !e.getId().equals(id))
                    .ifPresent(e -> { throw new DuplicateResourceException("Email already in use: " + request.getEmail()); });
            changes.append("email: ").append(employee.getEmail()).append(" -> ").append(request.getEmail()).append("; ");
            employee.setEmail(request.getEmail());
        }
        if (request.getDesignation() != null) {
            changes.append("designation: ").append(employee.getDesignation()).append(" -> ").append(request.getDesignation()).append("; ");
            employee.setDesignation(request.getDesignation());
        }
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            changes.append("department: ").append(employee.getDepartment().getName()).append(" -> ").append(dept.getName()).append("; ");
            employee.setDepartment(dept);
        }
        if (request.getCountry() != null) {
            changes.append("country: ").append(employee.getCountry()).append(" -> ").append(request.getCountry()).append("; ");
            employee.setCountry(request.getCountry());
        }
        if (request.getCurrency() != null) {
            employee.setCurrency(request.getCurrency());
        }
        if (request.getStatus() != null) {
            changes.append("status: ").append(employee.getStatus()).append(" -> ").append(request.getStatus()).append("; ");
            employee.setStatus(Employee.EmployeeStatus.valueOf(request.getStatus().toUpperCase()));
        }

        employee = employeeRepository.save(employee);
        auditService.log(id, "EMPLOYEE_UPDATED", changes.toString(), performedBy);

        EmployeeDTO dto = EmployeeDTO.fromEntity(employee);
        salaryRepository.findCurrentSalaryByEmployeeId(id)
                .ifPresent(salary -> dto.setCurrentSalary(salary.getBaseSalary()));
        return dto;
    }

    private String generateEmployeeId() {
        return "EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
