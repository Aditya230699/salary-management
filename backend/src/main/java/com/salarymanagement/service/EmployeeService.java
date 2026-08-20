package com.salarymanagement.service;

import com.salarymanagement.dto.CreateEmployeeRequest;
import com.salarymanagement.dto.EmployeeDTO;
import com.salarymanagement.dto.UpdateEmployeeRequest;
import com.salarymanagement.entity.Department;
import com.salarymanagement.entity.Employee;
import com.salarymanagement.entity.Salary;
import com.salarymanagement.exception.DuplicateResourceException;
import com.salarymanagement.exception.ResourceNotFoundException;
import com.salarymanagement.exception.ValidationException;
import com.salarymanagement.repository.DepartmentRepository;
import com.salarymanagement.repository.EmployeeRepository;
import com.salarymanagement.repository.SalaryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class EmployeeService {

    /**
     * Whitelist of sortable fields, mapped from the API-facing name to the JPA property
     * path. Passing an unchecked {@code sortBy} straight into {@link Sort} lets a caller
     * probe the entity graph and turns typos into HTTP 500s, so unknown values are
     * rejected instead.
     */
    private static final Map<String, String> SORTABLE_FIELDS = Map.of(
            "id", "id",
            "employeeId", "employeeId",
            "firstName", "firstName",
            "lastName", "lastName",
            "email", "email",
            "designation", "designation",
            "country", "country",
            "status", "status",
            "joinDate", "joinDate",
            "department", "department.name"
    );

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final SalaryRepository salaryRepository;
    private final AuditService auditService;
    private final CurrencyResolver currencyResolver;

    @Value("${app.pagination.max-page-size:100}")
    private int maxPageSize;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           SalaryRepository salaryRepository,
                           AuditService auditService,
                           CurrencyResolver currencyResolver) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.salaryRepository = salaryRepository;
        this.auditService = auditService;
        this.currencyResolver = currencyResolver;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getEmployees(String search, String department, String country,
                                           String status, int page, int size, String sortBy, String sortDir) {
        Employee.EmployeeStatus statusEnum = parseStatus(status);

        Sort sort = Sort.by(parseDirection(sortDir), resolveSortField(sortBy));
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampPageSize(size), sort);

        Page<Employee> employees = employeeRepository.findWithFilters(
                emptyToNull(search), emptyToNull(department), emptyToNull(country), statusEnum, pageable);

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

    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public EmployeeDTO createEmployee(CreateEmployeeRequest request, String performedBy) {
        if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Employee with email already exists: " + request.getEmail());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        // The currency of record is derived from the country so that salary figures are
        // always stored in a unit that matches where the employee is paid.
        String currency = currencyResolver.currencyFor(request.getCountry());

        Employee employee = Employee.builder()
                .employeeId(generateEmployeeId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .designation(request.getDesignation())
                .department(department)
                .country(request.getCountry())
                .currency(currency)
                .joinDate(request.getJoinDate())
                .status(Employee.EmployeeStatus.ACTIVE)
                .build();

        employee = employeeRepository.save(employee);

        Salary salary = Salary.builder()
                .employee(employee)
                .baseSalary(request.getBaseSalary())
                .bonus(nullToZero(request.getBonus()))
                .deductions(nullToZero(request.getDeductions()))
                .currency(currency)
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

    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
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
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));
            changes.append("department: ").append(employee.getDepartment().getName()).append(" -> ").append(dept.getName()).append("; ");
            employee.setDepartment(dept);
        }
        if (request.getCountry() != null && !request.getCountry().equals(employee.getCountry())) {
            // A country transfer changes the currency in which compensation is recorded.
            // Relabelling the existing amount (for example INR as USD) would corrupt pay
            // data; converting it needs an approved FX rate and effective date. Keep that
            // as an explicit transfer workflow rather than silently creating bad data.
            throw new ValidationException(
                    "Country transfers require an approved compensation transfer and cannot be made through employee update");
        }
        if (request.getStatus() != null) {
            Employee.EmployeeStatus newStatus = parseStatus(request.getStatus());
            changes.append("status: ").append(employee.getStatus()).append(" -> ").append(newStatus).append("; ");
            employee.setStatus(newStatus);
        }

        employee = employeeRepository.save(employee);
        auditService.log(id, "EMPLOYEE_UPDATED", changes.toString(), performedBy);

        EmployeeDTO dto = EmployeeDTO.fromEntity(employee);
        salaryRepository.findCurrentSalaryByEmployeeId(id)
                .ifPresent(salary -> dto.setCurrentSalary(salary.getBaseSalary()));
        return dto;
    }

    private int clampPageSize(int requested) {
        if (requested < 1) {
            return 1;
        }
        return Math.min(requested, maxPageSize);
    }

    private String resolveSortField(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "id";
        }
        String mapped = SORTABLE_FIELDS.get(sortBy);
        if (mapped == null) {
            throw new ValidationException("Cannot sort by '" + sortBy + "'. Allowed: " + SORTABLE_FIELDS.keySet());
        }
        return mapped;
    }

    private Sort.Direction parseDirection(String sortDir) {
        return "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private Employee.EmployeeStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return Employee.EmployeeStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Unknown status '" + status + "'. Allowed: ACTIVE, INACTIVE, ON_LEAVE");
        }
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private static BigDecimal nullToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String generateEmployeeId() {
        return "EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
