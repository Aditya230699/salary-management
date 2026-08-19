package com.salarymanagement.service;

import com.salarymanagement.dto.DashboardDTO;
import com.salarymanagement.entity.Department;
import com.salarymanagement.entity.Employee;
import com.salarymanagement.repository.DepartmentRepository;
import com.salarymanagement.repository.EmployeeRepository;
import com.salarymanagement.repository.SalaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Should return dashboard with aggregated metrics")
    void getDashboard_ReturnsAggregatedData() {
        Department engineering = Department.builder().id(1L).name("Engineering").build();
        Department product = Department.builder().id(2L).name("Product").build();

        when(employeeRepository.count()).thenReturn(10000L);
        when(employeeRepository.countByStatus(Employee.EmployeeStatus.ACTIVE)).thenReturn(9000L);
        when(salaryRepository.findAverageSalary()).thenReturn(new BigDecimal("85000"));
        when(salaryRepository.findTotalPayroll()).thenReturn(new BigDecimal("850000000"));
        when(salaryRepository.findMinSalary()).thenReturn(new BigDecimal("35000"));
        when(salaryRepository.findMaxSalary()).thenReturn(new BigDecimal("250000"));
        when(departmentRepository.findAll()).thenReturn(List.of(engineering, product));
        when(employeeRepository.countByDepartmentName("Engineering")).thenReturn(5000L);
        when(employeeRepository.countByDepartmentName("Product")).thenReturn(2000L);
        when(salaryRepository.findAverageSalaryByDepartment("Engineering")).thenReturn(new BigDecimal("95000"));
        when(salaryRepository.findAverageSalaryByDepartment("Product")).thenReturn(new BigDecimal("90000"));
        when(salaryRepository.findTotalPayrollByDepartment("Engineering")).thenReturn(new BigDecimal("475000000"));
        when(salaryRepository.findTotalPayrollByDepartment("Product")).thenReturn(new BigDecimal("180000000"));
        when(employeeRepository.countByCountry(anyString())).thenReturn(0L);
        when(employeeRepository.countByCountry("India")).thenReturn(4000L);
        when(salaryRepository.findAverageSalaryByCountry("India")).thenReturn(new BigDecimal("2000000"));
        when(salaryRepository.findTotalPayrollByCountry("India")).thenReturn(new BigDecimal("8000000000"));

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.getTotalEmployees()).isEqualTo(10000L);
        assertThat(result.getActiveEmployees()).isEqualTo(9000L);
        assertThat(result.getAverageSalary()).isEqualByComparingTo(new BigDecimal("85000"));
        assertThat(result.getTotalPayroll()).isEqualByComparingTo(new BigDecimal("850000000"));
        assertThat(result.getEmployeesByDepartment()).containsEntry("Engineering", 5000L);
        assertThat(result.getAvgSalaryByDepartment()).containsEntry("Engineering", new BigDecimal("95000"));
    }

    @Test
    @DisplayName("Should handle null salary aggregates gracefully")
    void getDashboard_NullSalaries_ReturnsZeros() {
        when(employeeRepository.count()).thenReturn(0L);
        when(employeeRepository.countByStatus(Employee.EmployeeStatus.ACTIVE)).thenReturn(0L);
        when(salaryRepository.findAverageSalary()).thenReturn(null);
        when(salaryRepository.findTotalPayroll()).thenReturn(null);
        when(salaryRepository.findMinSalary()).thenReturn(null);
        when(salaryRepository.findMaxSalary()).thenReturn(null);
        when(departmentRepository.findAll()).thenReturn(List.of());
        when(employeeRepository.countByCountry(anyString())).thenReturn(0L);

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.getTotalEmployees()).isEqualTo(0L);
        assertThat(result.getAverageSalary()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalPayroll()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
