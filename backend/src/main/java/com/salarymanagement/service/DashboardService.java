package com.salarymanagement.service;

import com.salarymanagement.dto.DashboardDTO;
import com.salarymanagement.entity.Department;
import com.salarymanagement.entity.Employee;
import com.salarymanagement.repository.DepartmentRepository;
import com.salarymanagement.repository.EmployeeRepository;
import com.salarymanagement.repository.SalaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final SalaryRepository salaryRepository;
    private final DepartmentRepository departmentRepository;

    private static final List<String> COUNTRIES = Arrays.asList("India", "USA", "UK", "Germany", "Australia");

    public DashboardService(EmployeeRepository employeeRepository,
                            SalaryRepository salaryRepository,
                            DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.salaryRepository = salaryRepository;
        this.departmentRepository = departmentRepository;
    }

    public DashboardDTO getDashboard() {
        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.countByStatus(Employee.EmployeeStatus.ACTIVE);

        BigDecimal avgSalary = salaryRepository.findAverageSalary();
        BigDecimal totalPayroll = salaryRepository.findTotalPayroll();
        BigDecimal minSalary = salaryRepository.findMinSalary();
        BigDecimal maxSalary = salaryRepository.findMaxSalary();

        List<Department> departments = departmentRepository.findAll();

        Map<String, Long> empByDept = new LinkedHashMap<>();
        Map<String, BigDecimal> avgByDept = new LinkedHashMap<>();
        Map<String, BigDecimal> payrollByDept = new LinkedHashMap<>();

        for (Department dept : departments) {
            long count = employeeRepository.countByDepartmentName(dept.getName());
            if (count > 0) {
                empByDept.put(dept.getName(), count);
                BigDecimal avg = salaryRepository.findAverageSalaryByDepartment(dept.getName());
                avgByDept.put(dept.getName(), avg != null ? avg : BigDecimal.ZERO);
                BigDecimal payroll = salaryRepository.findTotalPayrollByDepartment(dept.getName());
                payrollByDept.put(dept.getName(), payroll != null ? payroll : BigDecimal.ZERO);
            }
        }

        Map<String, Long> empByCountry = new LinkedHashMap<>();
        Map<String, BigDecimal> avgByCountry = new LinkedHashMap<>();
        Map<String, BigDecimal> payrollByCountry = new LinkedHashMap<>();

        for (String country : COUNTRIES) {
            long count = employeeRepository.countByCountry(country);
            if (count > 0) {
                empByCountry.put(country, count);
                BigDecimal avg = salaryRepository.findAverageSalaryByCountry(country);
                avgByCountry.put(country, avg != null ? avg : BigDecimal.ZERO);
                BigDecimal payroll = salaryRepository.findTotalPayrollByCountry(country);
                payrollByCountry.put(country, payroll != null ? payroll : BigDecimal.ZERO);
            }
        }

        return DashboardDTO.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .averageSalary(avgSalary != null ? avgSalary : BigDecimal.ZERO)
                .totalPayroll(totalPayroll != null ? totalPayroll : BigDecimal.ZERO)
                .minSalary(minSalary != null ? minSalary : BigDecimal.ZERO)
                .maxSalary(maxSalary != null ? maxSalary : BigDecimal.ZERO)
                .employeesByDepartment(empByDept)
                .employeesByCountry(empByCountry)
                .avgSalaryByDepartment(avgByDept)
                .avgSalaryByCountry(avgByCountry)
                .payrollByDepartment(payrollByDept)
                .payrollByCountry(payrollByCountry)
                .build();
    }
}
