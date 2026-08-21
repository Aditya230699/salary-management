package com.salarymanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Unauthenticated liveness probe. The deployed container sleeps after inactivity on the
 * free tier, so this gives reviewers (and uptime pings) a way to wake it and confirm it
 * is serving without needing credentials.
 */
@RestController
public class HealthController {

    private final Instant startedAt = Instant.now();

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "salary-management-api",
                "startedAt", startedAt.toString()
        ));
    }
}
