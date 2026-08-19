package com.salarymanagement.controller;

import com.salarymanagement.dto.DashboardDTO;
import com.salarymanagement.service.CurrencyResolver;
import com.salarymanagement.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrencyResolver currencyResolver;

    public DashboardController(DashboardService dashboardService, CurrencyResolver currencyResolver) {
        this.dashboardService = dashboardService;
        this.currencyResolver = currencyResolver;
    }

    /**
     * @param country optional. Narrowing to one country makes every money figure
     *                single-currency and unlocks the department and designation breakdowns.
     */
    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboard(@RequestParam(required = false) String country) {
        return ResponseEntity.ok(dashboardService.getDashboard(country));
    }

    /** Reference list so the UI does not hardcode the countries the org operates in. */
    @GetMapping("/countries")
    public ResponseEntity<List<String>> getCountries() {
        return ResponseEntity.ok(currencyResolver.supportedCountries());
    }
}
