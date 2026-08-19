package com.salarymanagement.repository;

import com.salarymanagement.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {

    List<Salary> findByEmployeeIdOrderByEffectiveDateDesc(Long employeeId);

    @Query("SELECT s FROM Salary s WHERE s.employee.id = :employeeId AND s.endDate IS NULL")
    Optional<Salary> findCurrentSalaryByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Projects every in-force salary row for aggregation.
     *
     * <p>The dashboard previously issued three aggregate queries per department and per
     * country, which meant roughly 45 round trips to render one page. Pulling the
     * in-force rows once and grouping them in memory keeps the dashboard to a single
     * query. At the organisation's stated size (10k employees, so 10k in-force rows) this
     * is a small payload; if the dataset grew by orders of magnitude the grouping would
     * move back into SQL with a materialised summary table.
     *
     * <p>Column order: country, currency, department name, designation, base salary, net salary.
     */
    @Query("""
           SELECT e.country,
                  s.currency,
                  e.department.name,
                  e.designation,
                  s.baseSalary,
                  s.baseSalary + COALESCE(s.bonus, 0) - COALESCE(s.deductions, 0)
           FROM Salary s
           JOIN s.employee e
           WHERE s.endDate IS NULL
           """)
    List<Object[]> findCurrentSalaryAggregationRows();

    /** Same projection as above, narrowed to one country so figures share a currency. */
    @Query("""
           SELECT e.country,
                  s.currency,
                  e.department.name,
                  e.designation,
                  s.baseSalary,
                  s.baseSalary + COALESCE(s.bonus, 0) - COALESCE(s.deductions, 0)
           FROM Salary s
           JOIN s.employee e
           WHERE s.endDate IS NULL AND e.country = :country
           """)
    List<Object[]> findCurrentSalaryAggregationRowsByCountry(@Param("country") String country);
}
