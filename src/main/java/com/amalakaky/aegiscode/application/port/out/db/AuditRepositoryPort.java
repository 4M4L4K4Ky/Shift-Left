package com.amalakaky.aegiscode.application.port.out.db;

import com.amalakaky.aegiscode.domain.model.AuditReport;

public interface AuditRepositoryPort {
    AuditReport save(AuditReport report);
    AuditReport findById(String scanId);
}