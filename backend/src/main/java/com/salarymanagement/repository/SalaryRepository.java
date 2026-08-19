package com.salarymanagement.repository;

import com.salarymanagement.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {

    List<Salary> findByEmployeeIdOrderByEffectiveDateDesc(Long employeeId);

    @Query("SELECT s FROM Salary s WHERE s.employee.id = :employeeId AND s.endDate IS NULL")
    Optional<Salary> findCurrentSalaryByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT AVG(s.baseSalary) FROM Salary s WHERE s.endDate IS NULL AND s.employee.department.name = :department")
    BigDecimal findAverageSalaryByDepartment(@Param("department") String department);

    @Query("SELECT AVG(s.baseSalary) FROM Salary s WHERE s.endDate IS NULL AND s.employee.country = :country")
    BigDecimal findAverageSalaryByCountry(@Param("country") String country);

    @Query("SELECT MIN(s.baseSalary) FROM Salary s WHERE s.endDate IS NULL")
    BigDecimal findMinSalary();

    @Query("SELECT MAX(s.baseSalary) FROM Salary s WHERE s.endDate IS NULL")
    BigDecimal findMaxSalary();

    @Query("SELECT AVG(s.baseSalary) FROM Salary s WHERE s.endDate IS NULL")
    BigDecimal findAverageSalary();

    @Query("SELECT SUM(s.baseSalary + COALESCE(s.bonus, 0) - COALESCE(s.deductions, 0)) FROM Salary s WHERE s.endDate IS NULL")
    BigDecimal findTotalPayroll();

    @Query("SELECT SUM(s.baseSalary + COALESCE(s.bonus, 0) - COALESCE(s.deductions, 0)) FROM Salary s WHERE s.endDate IS NULL AND s.employee.department.name = :department")
    BigDecimal findTotalPayrollByDepartment(@Param("department") String department);

    @Query("SELECT SUM(s.baseSalary + COALESCE(s.bonus, 0) - COALESCE(s.deductions, 0)) FROM Salary s WHERE s.endDate IS NULL AND s.employee.country = :country")
    BigDecimal findTotalPayrollByCountry(@Param("country") String country);

    @Query("SELECT s FROM Salary s WHERE s.endDate IS NULL")
    List<Salary> findAllCurrentSalaries();
}
