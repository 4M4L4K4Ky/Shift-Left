package com.amalakaky.aegiscode.application.port.out.db;

import java.util.Map;

public interface AuditStatisticsRepositoryPort {
    Map<Integer, Long> getVulnerabilitiesBySeverity();
}