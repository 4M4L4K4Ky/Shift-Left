package com.amalakaky.aegiscode.application.usecase;

import com.amalakaky.aegiscode.application.port.in.GetAuditStatisticsUseCase;
import com.amalakaky.aegiscode.application.port.out.db.AuditStatisticsRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GetAuditStatisticsUseCaseImpl implements GetAuditStatisticsUseCase {

    private final AuditStatisticsRepositoryPort statisticsRepositoryPort;

    public GetAuditStatisticsUseCaseImpl(AuditStatisticsRepositoryPort statisticsRepositoryPort) {
        this.statisticsRepositoryPort = statisticsRepositoryPort;
    }

    @Override
    public Map<Integer, Long> getSeverityStatistics() {
        return statisticsRepositoryPort.getVulnerabilitiesBySeverity();
    }
}