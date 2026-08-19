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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private EmployeeService employeeService;

    private Department testDepartment;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testDepartment = Department.builder()
                .id(1L)
                .name("Engineering")
                .description("Engineering Department")
                .build();

        testEmployee = Employee.builder()
                .id(1L)
                .employeeId("EMP-00001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@acme.com")
                .designation("Software Engineer")
                .department(testDepartment)
                .country("USA")
                .currency("USD")
                .joinDate(LocalDate.of(2022, 1, 15))
                .status(Employee.EmployeeStatus.ACTIVE)
                .build();
        testEmployee.setCreatedAt(LocalDateTime.now());
        testEmployee.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return paginated employees with filters")
    void getEmployees_WithFilters_ReturnsPaginatedResults() {
        Page<Employee> page = new PageImpl<>(List.of(testEmployee));
        when(employeeRepository.findWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(salaryRepository.findCurrentSalaryByEmployeeId(1L)).thenReturn(Optional.empty());

        Page<EmployeeDTO> result = employeeService.getEmployees(
                null, null, null, null, 0, 20, "id", "asc");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("John");
        assertThat(result.getContent().get(0).getLastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("Should get employee by ID successfully")
    void getEmployeeById_ExistingEmployee_ReturnsDTO() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(salaryRepository.findCurrentSalaryByEmployeeId(1L)).thenReturn(Optional.empty());

        EmployeeDTO result = employeeService.getEmployeeById(1L);

        assertThat(result.getEmployeeId()).isEqualTo("EMP-00001");
        assertThat(result.getEmail()).isEqualTo("john.doe@acme.com");
        assertThat(result.getDepartmentName()).isEqualTo("Engineering");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent employee")
    void getEmployeeById_NonExistent_ThrowsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: 999");
    }

    @Test
    @DisplayName("Should create employee successfully")
    void createEmployee_ValidRequest_ReturnsCreatedEmployee() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@acme.com")
                .designation("Senior Engineer")
                .departmentId(1L)
                .country("USA")
                .currency("USD")
                .joinDate(LocalDate.of(2024, 3, 1))
                .baseSalary(new BigDecimal("120000"))
                .build();

        when(employeeRepository.findByEmail("jane.smith@acme.com")).thenReturn(Optional.empty());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            emp.setId(2L);
            emp.setCreatedAt(LocalDateTime.now());
            emp.setUpdatedAt(LocalDateTime.now());
            return emp;
        });
        when(salaryRepository.save(any(Salary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeDTO result = employeeService.createEmployee(request, "hr_manager");

        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getCurrentSalary()).isEqualByComparingTo(new BigDecimal("120000"));
        verify(auditService).log(eq(2L), eq("EMPLOYEE_CREATED"), anyString(), eq("hr_manager"));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException for existing email")
    void createEmployee_DuplicateEmail_ThrowsException() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .email("john.doe@acme.com")
                .build();

        when(employeeRepository.findByEmail("john.doe@acme.com")).thenReturn(Optional.of(testEmployee));

        assertThatThrownBy(() -> employeeService.createEmployee(request, "hr_manager"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Employee with email already exists");
    }

    @Test
    @DisplayName("Should update employee successfully")
    void updateEmployee_ValidRequest_ReturnsUpdatedEmployee() {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .firstName("Jonathan")
                .designation("Senior Software Engineer")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(salaryRepository.findCurrentSalaryByEmployeeId(1L)).thenReturn(Optional.empty());

        EmployeeDTO result = employeeService.updateEmployee(1L, request, "hr_manager");

        assertThat(result).isNotNull();
        verify(auditService).log(eq(1L), eq("EMPLOYEE_UPDATED"), anyString(), eq("hr_manager"));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent employee")
    void updateEmployee_NonExistent_ThrowsException() {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder().firstName("Test").build();
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(999L, request, "hr_manager"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
