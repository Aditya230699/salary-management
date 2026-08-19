package com.salarymanagement.service;

import com.salarymanagement.dto.SalaryStatsDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SalaryStatsCalculatorTest {

    private final SalaryStatsCalculator calculator = new SalaryStatsCalculator();

    private static List<BigDecimal> money(String... values) {
        return java.util.Arrays.stream(values).map(BigDecimal::new).collect(Collectors.toList());
    }

    @Test
    @DisplayName("Median of an odd-sized group is the middle salary")
    void median_OddCount() {
        List<BigDecimal> salaries = money("100", "200", "300");

        SalaryStatsDTO stats = calculator.calculate("Engineering", "USD", salaries, salaries);

        assertThat(stats.getMedianSalary()).isEqualByComparingTo("200");
        assertThat(stats.getMinSalary()).isEqualByComparingTo("100");
        assertThat(stats.getMaxSalary()).isEqualByComparingTo("300");
        assertThat(stats.getEmployeeCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Median of an even-sized group interpolates between the two middle salaries")
    void median_EvenCount() {
        List<BigDecimal> salaries = money("100", "200", "300", "400");

        SalaryStatsDTO stats = calculator.calculate("Engineering", "USD", salaries, salaries);

        assertThat(stats.getMedianSalary()).isEqualByComparingTo("250");
    }

    @Test
    @DisplayName("Quartiles split a uniform 1..101 distribution at 26 and 76")
    void quartiles_UniformDistribution() {
        List<BigDecimal> salaries = IntStream.rangeClosed(1, 101)
                .mapToObj(BigDecimal::valueOf)
                .collect(Collectors.toList());

        SalaryStatsDTO stats = calculator.calculate("All", "USD", salaries, salaries);

        assertThat(stats.getPercentile25()).isEqualByComparingTo("26");
        assertThat(stats.getMedianSalary()).isEqualByComparingTo("51");
        assertThat(stats.getPercentile75()).isEqualByComparingTo("76");
    }

    @Test
    @DisplayName("Order of the input does not change the result")
    void statsAreOrderIndependent() {
        SalaryStatsDTO ascending = calculator.calculate("A", "USD", money("100", "200", "300"), money("100", "200", "300"));
        SalaryStatsDTO shuffled = calculator.calculate("A", "USD", money("300", "100", "200"), money("300", "100", "200"));

        assertThat(shuffled.getMedianSalary()).isEqualByComparingTo(ascending.getMedianSalary());
        assertThat(shuffled.getAverageSalary()).isEqualByComparingTo(ascending.getAverageSalary());
        assertThat(shuffled.getMinSalary()).isEqualByComparingTo(ascending.getMinSalary());
    }

    @Test
    @DisplayName("Single employee group reports that salary for every percentile")
    void singleEmployee() {
        SalaryStatsDTO stats = calculator.calculate("Solo", "GBP", money("90000"), money("95000"));

        assertThat(stats.getMinSalary()).isEqualByComparingTo("90000");
        assertThat(stats.getMedianSalary()).isEqualByComparingTo("90000");
        assertThat(stats.getMaxSalary()).isEqualByComparingTo("90000");
        assertThat(stats.getTotalPayroll()).isEqualByComparingTo("95000");
    }

    @Test
    @DisplayName("Empty group returns zeros rather than failing")
    void emptyGroup() {
        SalaryStatsDTO stats = calculator.calculate("Empty", "EUR", List.of(), List.of());

        assertThat(stats.getEmployeeCount()).isZero();
        assertThat(stats.getMedianSalary()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(stats.getTotalPayroll()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Payroll sums net pay, not base pay")
    void payrollUsesNetSalaries() {
        SalaryStatsDTO stats = calculator.calculate("Team", "USD",
                money("100000", "200000"),
                money("110000", "190000"));

        assertThat(stats.getAverageSalary()).isEqualByComparingTo("150000");
        assertThat(stats.getTotalPayroll()).isEqualByComparingTo("300000");
    }
}
