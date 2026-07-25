package com.amalakaky.aegiscode.application.usecase;

import com.amalakaky.aegiscode.application.port.in.GetAuditStatisticsUseCase;
import com.amalakaky.aegiscode.application.port.out.db.AuditStatisticsRepositoryPort;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para consultar estadisticas agregadas de vulnerabilidades.
 * 
 * Delega en el puerto de salida {@link AuditStatisticsRepositoryPort}
 * para poblar todas las secciones del dashboard.
 */
@Service
public class GetAuditStatisticsUseCaseImpl implements GetAuditStatisticsUseCase {

  private final AuditStatisticsRepositoryPort statisticsRepositoryPort;

  public GetAuditStatisticsUseCaseImpl(AuditStatisticsRepositoryPort statisticsRepositoryPort) {
    this.statisticsRepositoryPort = statisticsRepositoryPort;
  }

  @Override
  public DashboardStats getDashboardStats() {
    List<CweCountItem> byCwe = statisticsRepositoryPort.countByCwe().stream()
        .map(cc -> new CweCountItem(cc.cweId(), cc.count()))
        .collect(Collectors.toList());
    List<RecentAuditItem> recent = statisticsRepositoryPort.findRecentAudits(10).stream()
        .map(a -> new RecentAuditItem(a.scanId(), a.date(), a.repositoryUrl(),
            a.branchName(), a.totalVulnerabilities()))
        .collect(Collectors.toList());
    return new DashboardStats(
        statisticsRepositoryPort.totalAudits(),
        statisticsRepositoryPort.totalVulnerabilities(),
        statisticsRepositoryPort.getVulnerabilitiesBySeverity(),
        byCwe,
        recent
    );
  }
}
