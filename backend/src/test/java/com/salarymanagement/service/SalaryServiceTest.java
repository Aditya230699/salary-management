package com.salarymanagement.service;

import com.salarymanagement.dto.SalaryDTO;
import com.salarymanagement.dto.UpdateSalaryRequest;
import com.salarymanagement.entity.Department;
import com.salarymanagement.entity.Employee;
import com.salarymanagement.entity.Salary;
import com.salarymanagement.exception.ResourceNotFoundException;
import com.salarymanagement.exception.ValidationException;
import com.salarymanagement.repository.EmployeeRepository;
import com.salarymanagement.repository.SalaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryServiceTest {

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private SalaryService salaryService;

    private Employee testEmployee;
    private Salary testSalary;

    @BeforeEach
    void setUp() {
        Department dept = Department.builder().id(1L).name("Engineering").build();

        testEmployee = Employee.builder()
                .id(1L)
                .employeeId("EMP-00001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@acme.com")
                .department(dept)
                .country("USA")
                .currency("USD")
                .build();

        testSalary = Salary.builder()
                .id(1L)
                .employee(testEmployee)
                .baseSalary(new BigDecimal("100000"))
                .bonus(new BigDecimal("10000"))
                .deductions(new BigDecimal("5000"))
                .currency("USD")
                .effectiveDate(LocalDate.of(2022, 1, 1))
                .createdBy("SYSTEM")
                .build();
        testSalary.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return current salary for employee")
    void getCurrentSalary_ExistingEmployee_ReturnsSalary() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(salaryRepository.findCurrentSalaryByEmployeeId(1L)).thenReturn(Optional.of(testSalary));

        SalaryDTO result = salaryService.getCurrentSalary(1L);

        assertThat(result.getBaseSalary()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(result.getBonus()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(result.getDeductions()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(result.getNetSalary()).isEqualByComparingTo(new BigDecimal("105000"));
    }

    @Test
    @DisplayName("Should throw exception when employee not found")
    void getCurrentSalary_NonExistentEmployee_ThrowsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> salaryService.getCurrentSalary(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should return salary history")
    void getSalaryHistory_ReturnsOrderedHistory() {
        Salary oldSalary = Salary.builder()
                .id(2L)
                .employee(testEmployee)
                .baseSalary(new BigDecimal("80000"))
                .bonus(BigDecimal.ZERO)
                .deductions(BigDecimal.ZERO)
                .currency("USD")
                .effectiveDate(LocalDate.of(2020, 1, 1))
                .endDate(LocalDate.of(2021, 12, 31))
                .createdBy("SYSTEM")
                .build();
        oldSalary.setCreatedAt(LocalDateTime.now().minusYears(2));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(salaryRepository.findByEmployeeIdOrderByEffectiveDateDesc(1L))
                .thenReturn(List.of(testSalary, oldSalary));

        List<SalaryDTO> history = salaryService.getSalaryHistory(1L);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getBaseSalary()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(history.get(1).getBaseSalary()).isEqualByComparingTo(new BigDecimal("80000"));
    }

    @Test
    @DisplayName("Should update salary with proper closing of previous record")
    void updateSalary_ValidRequest_CreatesNewRecordAndClosesPrevious() {
        UpdateSalaryRequest request = UpdateSalaryRequest.builder()
                .baseSalary(new BigDecimal("120000"))
                .bonus(new BigDecimal("15000"))
                .deductions(new BigDecimal("6000"))
                .effectiveDate(LocalDate.of(2024, 4, 1))
                .notes("Annual raise")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(salaryRepository.findCurrentSalaryByEmployeeId(1L)).thenReturn(Optional.of(testSalary));
        when(salaryRepository.save(any(Salary.class))).thenAnswer(invocation -> {
            Salary s = invocation.getArgument(0);
            s.setId(s.getId() != null ? s.getId() : 3L);
            s.setCreatedAt(LocalDateTime.now());
            return s;
        });

        SalaryDTO result = salaryService.updateSalary(1L, request, "hr_manager");

        assertThat(result.getBaseSalary()).isEqualByComparingTo(new BigDecimal("120000"));
        assertThat(result.getNotes()).isEqualTo("Annual raise");

        // The superseded record is closed the day before the new one takes effect.
        assertThat(testSalary.getEndDate()).isEqualTo(LocalDate.of(2024, 3, 31));
        verify(salaryRepository, times(2)).save(any(Salary.class));
        verify(auditService).log(eq(1L), eq("SALARY_UPDATED"), anyString(), eq("hr_manager"));
    }

    @Test
    @DisplayName("Rejects a salary change dated before the record it would supersede")
    void updateSalary_EffectiveDateBeforeCurrent_Throws() {
        UpdateSalaryRequest request = UpdateSalaryRequest.builder()
                .baseSalary(new BigDecimal("120000"))
                .effectiveDate(LocalDate.of(2021, 6, 1)) // current record starts 2022-01-01
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(salaryRepository.findCurrentSalaryByEmployeeId(1L)).thenReturn(Optional.of(testSalary));

        assertThatThrownBy(() -> salaryService.updateSalary(1L, request, "hr_manager"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("must be after");

        // Nothing is persisted, so the existing history stays intact.
        assertThat(testSalary.getEndDate()).isNull();
        verify(salaryRepository, never()).save(any(Salary.class));
    }

    @Test
    @DisplayName("Rejects a salary change dated the same day as the current record")
    void updateSalary_EffectiveDateSameAsCurrent_Throws() {
        UpdateSalaryRequest request = UpdateSalaryRequest.builder()
                .baseSalary(new BigDecimal("120000"))
                .effectiveDate(LocalDate.of(2022, 1, 1))
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(salaryRepository.findCurrentSalaryByEmployeeId(1L)).thenReturn(Optional.of(testSalary));

        assertThatThrownBy(() -> salaryService.updateSalary(1L, request, "hr_manager"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Omitted bonus and deductions are stored as zero, not null")
    void updateSalary_NullBonusAndDeductions_DefaultToZero() {
        UpdateSalaryRequest request = UpdateSalaryRequest.builder()
                .baseSalary(new BigDecimal("150000"))
                .effectiveDate(LocalDate.of(2024, 6, 1))
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(salaryRepository.findCurrentSalaryByEmployeeId(1L)).thenReturn(Optional.of(testSalary));
        when(salaryRepository.save(any(Salary.class))).thenAnswer(invocation -> {
            Salary s = invocation.getArgument(0);
            s.setCreatedAt(LocalDateTime.now());
            return s;
        });

        SalaryDTO result = salaryService.updateSalary(1L, request, "hr_manager");

        assertThat(result.getBonus()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getDeductions()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getNetSalary()).isEqualByComparingTo("150000");
    }

    @Test
    @DisplayName("Rejects deductions that exceed base salary plus bonus")
    void updateSalary_DeductionsExceedEarnings_Throws() {
        UpdateSalaryRequest request = UpdateSalaryRequest.builder()
                .baseSalary(new BigDecimal("100000"))
                .bonus(new BigDecimal("5000"))
                .deductions(new BigDecimal("200000"))
                .effectiveDate(LocalDate.of(2024, 6, 1))
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        assertThatThrownBy(() -> salaryService.updateSalary(1L, request, "hr_manager"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Deductions cannot exceed");
    }

    @Test
    @DisplayName("New salary inherits the employee's currency of record")
    void updateSalary_UsesEmployeeCurrency() {
        UpdateSalaryRequest request = UpdateSalaryRequest.builder()
                .baseSalary(new BigDecimal("160000"))
                .effectiveDate(LocalDate.of(2024, 7, 1))
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(salaryRepository.findCurrentSalaryByEmployeeId(1L)).thenReturn(Optional.empty());
        when(salaryRepository.save(any(Salary.class))).thenAnswer(invocation -> {
            Salary s = invocation.getArgument(0);
            s.setCreatedAt(LocalDateTime.now());
            return s;
        });

        SalaryDTO result = salaryService.updateSalary(1L, request, "hr_manager");

        assertThat(result.getCurrency()).isEqualTo("USD");
    }
}
