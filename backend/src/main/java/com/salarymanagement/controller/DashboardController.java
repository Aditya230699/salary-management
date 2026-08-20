package com.salarymanagement.controller;

import com.salarymanagement.dto.DashboardDTO;
import com.salarymanagement.service.CurrencyResolver;
import com.salarymanagement.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<DashboardDTO> getDashboard(@RequestParam(required = false) String country) {
        return ResponseEntity.ok(dashboardService.getDashboard(country));
    }

    @GetMapping("/countries")
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<List<String>> getCountries() {
        return ResponseEntity.ok(currencyResolver.supportedCountries());
    }
}
