package com.salarymanagement.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Dashboard payload.
 *
 * <p>Headcounts are currency-free and always global. Money is only ever reported inside a
 * single currency, which is why pay statistics arrive as a list of per-country groups
 * rather than one organisation-wide average. When the caller narrows to one country, the
 * department and designation breakdowns become single-currency and are populated too.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {

    private long totalEmployees;
    private long activeEmployees;

    private Map<String, Long> employeesByDepartment;
    private Map<String, Long> employeesByCountry;

    /** Pay distribution per country, each in that country's own currency. */
    private List<SalaryStatsDTO> salaryStatsByCountry;

    /** Populated only when a country filter is applied, so figures share one currency. */
    private String filteredCountry;
    private List<SalaryStatsDTO> salaryStatsByDepartment;
    private List<SalaryStatsDTO> salaryStatsByDesignation;
}
