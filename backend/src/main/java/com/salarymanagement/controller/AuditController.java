package com.salarymanagement.controller;

import com.salarymanagement.entity.AuditLog;
import com.salarymanagement.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes the change history for one employee. The audit trail was being written but had
 * no way to read it, which made the compliance record invisible to the HR manager it was
 * recorded for.
 */
@RestController
@RequestMapping("/api/employees/{employeeId}/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditService.getAuditLogs(employeeId, page, size));
    }
}
