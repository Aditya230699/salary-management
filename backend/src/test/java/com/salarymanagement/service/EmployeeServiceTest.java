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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

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

    private static final int MAX_PAGE_SIZE = 100;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private AuditService auditService;

    private EmployeeService employeeService;

    private Department testDepartment;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(
                employeeRepository, departmentRepository, salaryRepository, auditService, new CurrencyResolver());
        ReflectionTestUtils.setField(employeeService, "maxPageSize", MAX_PAGE_SIZE);

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
    }

    @Test
    @DisplayName("Caps page size so a caller cannot request the whole table in one page")
    void getEmployees_OversizedPage_IsClamped() {
        when(employeeRepository.findWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        employeeService.getEmployees(null, null, null, null, 0, 100_000, "id", "asc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(employeeRepository).findWithFilters(any(), any(), any(), any(), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(MAX_PAGE_SIZE);
    }

    @Test
    @DisplayName("Rejects sorting by a field that is not whitelisted")
    void getEmployees_UnknownSortField_Throws() {
        assertThatThrownBy(() -> employeeService.getEmployees(
                null, null, null, null, 0, 20, "password", "asc"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Cannot sort by 'password'");

        verifyNoInteractions(employeeRepository);
    }

    @Test
    @DisplayName("Maps the department sort key onto the related entity property")
    void getEmployees_SortByDepartment_UsesNestedProperty() {
        when(employeeRepository.findWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        employeeService.getEmployees(null, null, null, null, 0, 20, "department", "desc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(employeeRepository).findWithFilters(any(), any(), any(), any(), captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("department.name")).isNotNull();
    }

    @Test
    @DisplayName("Blank filter values are treated as absent rather than matched literally")
    void getEmployees_BlankFilters_BecomeNull() {
        when(employeeRepository.findWithFilters(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        employeeService.getEmployees("  ", "", "  ", "", 0, 20, "id", "asc");

        verify(employeeRepository).findWithFilters(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("Rejects an unknown employee status")
    void getEmployees_UnknownStatus_Throws() {
        assertThatThrownBy(() -> employeeService.getEmployees(
                null, null, null, "RETIRED", 0, 20, "id", "asc"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Unknown status");
    }

    @Test
    @DisplayName("Should get employee by ID successfully")
    void getEmployeeById_ExistingEmployee_ReturnsDTO() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(salaryRepository.findCurrentSalaryByEmployeeId(1L)).thenReturn(Optional.empty());

        EmployeeDTO result = employeeService.getEmployeeById(1L);

        assertThat(result.getEmployeeId()).isEqualTo("EMP-00001");
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
        CreateEmployeeRequest request = validCreateRequest();

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
        assertThat(result.getCurrentSalary()).isEqualByComparingTo("120000");
        verify(auditService).log(eq(2L), eq("EMPLOYEE_CREATED"), anyString(), eq("hr_manager"));
    }

    @Test
    @DisplayName("Derives currency from country and ignores a mismatched client value")
    void createEmployee_DerivesCurrencyFromCountry() {
        CreateEmployeeRequest request = validCreateRequest();
        request.setCountry("Germany");
        request.setCurrency("XXX"); // client-supplied value must not be trusted

        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            emp.setId(3L);
            emp.setCreatedAt(LocalDateTime.now());
            return emp;
        });
        when(salaryRepository.save(any(Salary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        employeeService.createEmployee(request, "hr_manager");

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertThat(captor.getValue().getCurrency()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("Rejects a country the organisation does not operate in")
    void createEmployee_UnsupportedCountry_Throws() {
        CreateEmployeeRequest request = validCreateRequest();
        request.setCountry("Atlantis");

        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));

        assertThatThrownBy(() -> employeeService.createEmployee(request, "hr_manager"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Unsupported country");
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
    @DisplayName("Rejects a country change without an explicit compensation transfer")
    void updateEmployee_CountryChange_RequiresCompensationTransfer() {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder().country("India").build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, request, "hr_manager"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("compensation transfer");

        assertThat(testEmployee.getCountry()).isEqualTo("USA");
        assertThat(testEmployee.getCurrency()).isEqualTo("USD");
        verify(employeeRepository, never()).save(any(Employee.class));
        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Rejects an email already used by a different employee")
    void updateEmployee_DuplicateEmail_Throws() {
        Employee other = Employee.builder().id(2L).email("taken@acme.com").build();
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder().email("taken@acme.com").build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findByEmail("taken@acme.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, request, "hr_manager"))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent employee")
    void updateEmployee_NonExistent_ThrowsException() {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder().firstName("Test").build();
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(999L, request, "hr_manager"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private CreateEmployeeRequest validCreateRequest() {
        return CreateEmployeeRequest.builder()
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
    }
}
