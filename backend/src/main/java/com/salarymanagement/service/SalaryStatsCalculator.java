package com.salarymanagement.service;

import com.salarymanagement.dto.SalaryStatsDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Computes a pay distribution for one comparable group of salaries.
 *
 * <p>Kept separate from the dashboard service so the percentile maths is unit-testable in
 * isolation and reusable for any grouping (country, department, designation).
 */
@Component
public class SalaryStatsCalculator {

    private static final int MONEY_SCALE = 2;

    /**
     * @param baseSalaries base pay for each employee in the group
     * @param netSalaries  base + bonus - deductions for each employee in the group
     */
    public SalaryStatsDTO calculate(String group, String currency,
                                    List<BigDecimal> baseSalaries, List<BigDecimal> netSalaries) {
        if (baseSalaries.isEmpty()) {
            return SalaryStatsDTO.builder()
                    .group(group)
                    .currency(currency)
                    .employeeCount(0)
                    .minSalary(BigDecimal.ZERO)
                    .percentile25(BigDecimal.ZERO)
                    .medianSalary(BigDecimal.ZERO)
                    .percentile75(BigDecimal.ZERO)
                    .maxSalary(BigDecimal.ZERO)
                    .averageSalary(BigDecimal.ZERO)
                    .totalPayroll(BigDecimal.ZERO)
                    .build();
        }

        List<BigDecimal> sorted = new ArrayList<>(baseSalaries);
        sorted.sort(Comparator.naturalOrder());

        BigDecimal sum = baseSalaries.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = sum.divide(BigDecimal.valueOf(sorted.size()), MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal payroll = netSalaries.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        return SalaryStatsDTO.builder()
                .group(group)
                .currency(currency)
                .employeeCount(sorted.size())
                .minSalary(scale(sorted.get(0)))
                .percentile25(percentile(sorted, 25))
                .medianSalary(percentile(sorted, 50))
                .percentile75(percentile(sorted, 75))
                .maxSalary(scale(sorted.get(sorted.size() - 1)))
                .averageSalary(average)
                .totalPayroll(scale(payroll))
                .build();
    }

    /**
     * Linear-interpolated percentile over an ascending list. Interpolating rather than
     * picking the nearest element means the median of an even-sized group sits between the
     * two middle salaries, which is what an HR manager expects to see.
     */
    BigDecimal percentile(List<BigDecimal> ascending, int percentile) {
        if (ascending.size() == 1) {
            return scale(ascending.get(0));
        }
        double rank = (percentile / 100.0) * (ascending.size() - 1);
        int lowerIndex = (int) Math.floor(rank);
        int upperIndex = (int) Math.ceil(rank);

        BigDecimal lower = ascending.get(lowerIndex);
        if (lowerIndex == upperIndex) {
            return scale(lower);
        }
        BigDecimal upper = ascending.get(upperIndex);
        BigDecimal fraction = BigDecimal.valueOf(rank - lowerIndex);
        return scale(lower.add(upper.subtract(lower).multiply(fraction)));
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
