package com.amalakaky.aegiscode.application.port.out.db;

import com.amalakaky.aegiscode.domain.model.CweCount;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Puerto de salida para estadisticas de vulnerabilidades.
 * 
 * Proporciona datos agregados desde la vista de base de datos
 * {@code view_vulnerability_severity_stats} y consultas analiticas
 * adicionales sobre auditorias y vulnerabilidades.
 */
public interface AuditStatisticsRepositoryPort {

  Map<Integer, Long> getVulnerabilitiesBySeverity();

  long totalAudits();

  long totalVulnerabilities();

  List<CweCount> countByCwe();

  List<AuditSummary> findRecentAudits(int limit);

  record AuditSummary(String scanId, LocalDateTime date, String repositoryUrl,
                      String branchName, long totalVulnerabilities) {}
}



