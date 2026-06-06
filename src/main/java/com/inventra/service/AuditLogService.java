package com.inventra.service;

import com.inventra.entity.AuditLog;
import com.inventra.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String username, String action) {
        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action(action)
                .build();
        auditLogRepository.save(auditLog);
    }
}
