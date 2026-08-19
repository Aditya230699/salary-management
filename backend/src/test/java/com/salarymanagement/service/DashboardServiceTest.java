package com.salarymanagement.service;

import com.salarymanagement.dto.DashboardDTO;
import com.salarymanagement.dto.SalaryStatsDTO;
import com.salarymanagement.entity.Employee;
import com.salarymanagement.exception.ValidationException;
import com.salarymanagement.repository.EmployeeRepository;
import com.salarymanagement.repository.SalaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryRepository salaryRepository;

    private DashboardService dashboardService;

    private DashboardService service() {
        if (dashboardService == null) {
            dashboardService = new DashboardService(
                    employeeRepository, salaryRepository, new SalaryStatsCalculator(), new CurrencyResolver());
        }
        return dashboardService;
    }

    /** country, currency, department, designation, baseSalary, netSalary */
    private static Object[] row(String country, String currency, String dept, String designation,
                                String base, String net) {
        return new Object[]{country, currency, dept, designation, new BigDecimal(base), new BigDecimal(net)};
    }

    @Test
    @DisplayName("Reports pay statistics per country so currencies are never mixed")
    void getDashboard_GroupsStatsByCountry() {
        when(employeeRepository.count()).thenReturn(4L);
        when(employeeRepository.countByStatus(Employee.EmployeeStatus.ACTIVE)).thenReturn(3L);
        when(salaryRepository.findCurrentSalaryAggregationRows()).thenReturn(List.of(
                row("USA", "USD", "Engineering", "Software Engineer", "100000", "105000"),
                row("USA", "USD", "Engineering", "Staff Engineer", "200000", "210000"),
                row("India", "INR", "Engineering", "Software Engineer", "1000000", "1100000"),
                row("India", "INR", "Product", "Product Manager", "3000000", "3100000")
        ));

        DashboardDTO dashboard = service().getDashboard(null);

        assertThat(dashboard.getTotalEmployees()).isEqualTo(4L);
        assertThat(dashboard.getActiveEmployees()).isEqualTo(3L);
        assertThat(dashboard.getSalaryStatsByCountry()).hasSize(2);

        SalaryStatsDTO usa = dashboard.getSalaryStatsByCountry().stream()
                .filter(s -> s.getGroup().equals("USA")).findFirst().orElseThrow();
        assertThat(usa.getCurrency()).isEqualTo("USD");
        assertThat(usa.getEmployeeCount()).isEqualTo(2);
        assertThat(usa.getAverageSalary()).isEqualByComparingTo("150000");
        assertThat(usa.getMedianSalary()).isEqualByComparingTo("150000");
        assertThat(usa.getTotalPayroll()).isEqualByComparingTo("315000");

        SalaryStatsDTO india = dashboard.getSalaryStatsByCountry().stream()
                .filter(s -> s.getGroup().equals("India")).findFirst().orElseThrow();
        assertThat(india.getCurrency()).isEqualTo("INR");
        assertThat(india.getAverageSalary()).isEqualByComparingTo("2000000");
    }

    @Test
    @DisplayName("Department and designation breakdowns only appear when narrowed to one country")
    void getDashboard_WithoutCountry_OmitsSingleCurrencyBreakdowns() {
        when(employeeRepository.count()).thenReturn(1L);
        when(salaryRepository.findCurrentSalaryAggregationRows()).thenReturn(List.<Object[]>of(
                row("USA", "USD", "Engineering", "Software Engineer", "100000", "100000")
        ));

        DashboardDTO dashboard = service().getDashboard(null);

        assertThat(dashboard.getFilteredCountry()).isNull();
        assertThat(dashboard.getSalaryStatsByDepartment()).isNull();
        assertThat(dashboard.getSalaryStatsByDesignation()).isNull();
        assertThat(dashboard.getEmployeesByDepartment()).containsEntry("Engineering", 1L);
    }

    @Test
    @DisplayName("Filtering by country populates department and designation statistics")
    void getDashboard_WithCountry_IncludesBreakdowns() {
        when(employeeRepository.count()).thenReturn(2L);
        when(salaryRepository.findCurrentSalaryAggregationRowsByCountry("USA")).thenReturn(List.of(
                row("USA", "USD", "Engineering", "Software Engineer", "100000", "100000"),
                row("USA", "USD", "Product", "Product Manager", "140000", "140000")
        ));

        DashboardDTO dashboard = service().getDashboard("USA");

        assertThat(dashboard.getFilteredCountry()).isEqualTo("USA");
        assertThat(dashboard.getSalaryStatsByDepartment()).hasSize(2);
        assertThat(dashboard.getSalaryStatsByDesignation()).hasSize(2);
        assertThat(dashboard.getSalaryStatsByDepartment())
                .allSatisfy(stats -> assertThat(stats.getCurrency()).isEqualTo("USD"));
    }

    @Test
    @DisplayName("Unknown country is rejected instead of returning an empty dashboard")
    void getDashboard_UnknownCountry_Throws() {
        assertThatThrownBy(() -> service().getDashboard("Atlantis"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Unsupported country");
    }

    @Test
    @DisplayName("Handles an empty organisation without failing")
    void getDashboard_NoData() {
        when(employeeRepository.count()).thenReturn(0L);
        when(salaryRepository.findCurrentSalaryAggregationRows()).thenReturn(List.of());

        DashboardDTO dashboard = service().getDashboard(null);

        assertThat(dashboard.getTotalEmployees()).isZero();
        assertThat(dashboard.getSalaryStatsByCountry()).isEmpty();
        assertThat(dashboard.getEmployeesByCountry()).isEmpty();
    }
}
