package com.salarymanagement.service;

import com.salarymanagement.entity.AuditLog;
import com.salarymanagement.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    /** Caps how many audit rows one request can pull, so the endpoint cannot be used to dump the table. */
    private static final int MAX_PAGE_SIZE = 100;

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(Long employeeId, String action, String details, String performedBy) {
        AuditLog log = AuditLog.builder()
                .employeeId(employeeId)
                .action(action)
                .details(details)
                .performedBy(performedBy)
                .build();
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Long employeeId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return auditLogRepository.findByEmployeeIdOrderByTimestampDesc(
                employeeId, PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "timestamp")));
    }
}
