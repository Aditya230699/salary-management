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
        return auditLogRepository.findByEmployeeIdOrderByTimestampDesc(
                employeeId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
    }
}
