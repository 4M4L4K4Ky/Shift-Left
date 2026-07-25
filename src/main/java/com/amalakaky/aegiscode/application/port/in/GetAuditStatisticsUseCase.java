package com.amalakaky.aegiscode.application.port.in;

import com.amalakaky.aegiscode.application.port.out.db.AuditStatisticsRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Puerto de entrada para consultar estadísticas de auditoría.
 * 
 * Proporciona métricas agregadas sobre las vulnerabilidades almacenadas,
 * incluyendo distribución por severidad, top CWEs y auditorías recientes.
 */
public interface GetAuditStatisticsUseCase {

  DashboardStats getDashboardStats();

  record DashboardStats(
      long totalAudits,
      long totalVulnerabilities,
      Map<Integer, Long> bySeverity,
      List<CweCountItem> byCwe,
      List<RecentAuditItem> recentAudits
  ) {}

  record CweCountItem(String cweId, long count) {}

  record RecentAuditItem(String scanId, LocalDateTime date, String repositoryUrl,
                         String branchName, long totalVulnerabilities) {}
}



