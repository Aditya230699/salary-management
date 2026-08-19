package com.salarymanagement.service;

import com.salarymanagement.dto.DashboardDTO;
import com.salarymanagement.dto.SalaryStatsDTO;
import com.salarymanagement.entity.Employee;
import com.salarymanagement.repository.EmployeeRepository;
import com.salarymanagement.repository.SalaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    /** Index positions of the projection returned by the aggregation queries. */
    private static final int COUNTRY = 0;
    private static final int CURRENCY = 1;
    private static final int DEPARTMENT = 2;
    private static final int DESIGNATION = 3;
    private static final int BASE_SALARY = 4;
    private static final int NET_SALARY = 5;

    private final EmployeeRepository employeeRepository;
    private final SalaryRepository salaryRepository;
    private final SalaryStatsCalculator statsCalculator;
    private final CurrencyResolver currencyResolver;

    public DashboardService(EmployeeRepository employeeRepository,
                            SalaryRepository salaryRepository,
                            SalaryStatsCalculator statsCalculator,
                            CurrencyResolver currencyResolver) {
        this.employeeRepository = employeeRepository;
        this.salaryRepository = salaryRepository;
        this.statsCalculator = statsCalculator;
        this.currencyResolver = currencyResolver;
    }

    /**
     * @param country optional filter. When supplied, department and designation pay
     *                statistics are included because they then share a single currency.
     */
    public DashboardDTO getDashboard(String country) {
        String normalisedCountry = (country == null || country.isBlank()) ? null : country.trim();
        if (normalisedCountry != null) {
            // Validates against the supported list and fails with a 400 rather than
            // silently returning an empty dashboard for a typo.
            currencyResolver.currencyFor(normalisedCountry);
        }

        List<Object[]> rows = normalisedCountry == null
                ? salaryRepository.findCurrentSalaryAggregationRows()
                : salaryRepository.findCurrentSalaryAggregationRowsByCountry(normalisedCountry);

        DashboardDTO.DashboardDTOBuilder builder = DashboardDTO.builder()
                .totalEmployees(employeeRepository.count())
                .activeEmployees(employeeRepository.countByStatus(Employee.EmployeeStatus.ACTIVE))
                .employeesByDepartment(headcountBy(rows, DEPARTMENT))
                .employeesByCountry(headcountBy(rows, COUNTRY))
                .salaryStatsByCountry(statsGroupedBy(rows, COUNTRY));

        if (normalisedCountry != null) {
            builder.filteredCountry(normalisedCountry)
                    .salaryStatsByDepartment(statsGroupedBy(rows, DEPARTMENT))
                    .salaryStatsByDesignation(statsGroupedBy(rows, DESIGNATION));
        }

        return builder.build();
    }

    private Map<String, Long> headcountBy(List<Object[]> rows, int keyIndex) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Object[] row : rows) {
            counts.merge((String) row[keyIndex], 1L, Long::sum);
        }
        return sortedByValueDescending(counts);
    }

    /**
     * Groups rows by the given column and computes a distribution per group. Groups are
     * returned largest-first so the dashboard leads with the most significant populations.
     */
    private List<SalaryStatsDTO> statsGroupedBy(List<Object[]> rows, int keyIndex) {
        Map<String, List<BigDecimal>> baseByGroup = new LinkedHashMap<>();
        Map<String, List<BigDecimal>> netByGroup = new LinkedHashMap<>();
        Map<String, String> currencyByGroup = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String group = (String) row[keyIndex];
            baseByGroup.computeIfAbsent(group, k -> new ArrayList<>()).add((BigDecimal) row[BASE_SALARY]);
            netByGroup.computeIfAbsent(group, k -> new ArrayList<>()).add((BigDecimal) row[NET_SALARY]);
            // Within a country grouping the currency is constant. Within a department or
            // designation grouping it is only constant because the caller filtered to one
            // country, which is enforced by the caller of this method.
            currencyByGroup.putIfAbsent(group, (String) row[CURRENCY]);
        }

        List<SalaryStatsDTO> stats = new ArrayList<>();
        for (String group : baseByGroup.keySet()) {
            stats.add(statsCalculator.calculate(
                    group, currencyByGroup.get(group), baseByGroup.get(group), netByGroup.get(group)));
        }
        stats.sort(Comparator.comparingLong(SalaryStatsDTO::getEmployeeCount).reversed());
        return stats;
    }

    private static Map<String, Long> sortedByValueDescending(Map<String, Long> input) {
        return input.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
    }
}
