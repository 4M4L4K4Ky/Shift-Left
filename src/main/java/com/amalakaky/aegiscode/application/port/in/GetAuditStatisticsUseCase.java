package com.amalakaky.aegiscode.application.port.in;

import java.util.Map;

public interface GetAuditStatisticsUseCase {
    Map<Integer, Long> getSeverityStatistics();
}