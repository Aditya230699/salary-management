package com.salarymanagement.repository;

import com.salarymanagement.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeId(String employeeId);

    Optional<Employee> findByEmail(String email);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:search IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:department IS NULL OR e.department.name = :department) " +
           "AND (:country IS NULL OR e.country = :country) " +
           "AND (:status IS NULL OR e.status = :status)")
    Page<Employee> findWithFilters(
            @Param("search") String search,
            @Param("department") String department,
            @Param("country") String country,
            @Param("status") Employee.EmployeeStatus status,
            Pageable pageable);

    long countByDepartmentName(String departmentName);

    long countByCountry(String country);

    long countByStatus(Employee.EmployeeStatus status);
}
