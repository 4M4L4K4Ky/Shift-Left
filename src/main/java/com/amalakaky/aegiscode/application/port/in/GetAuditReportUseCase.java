package com.amalakaky.aegiscode.application.port.in;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Puerto de entrada para generar el informe PDF global de la plataforma.
 * <p>
 * Recopila todas las estadísticas del dashboard junto con el detalle
 * de las auditorías recientes y sus vulnerabilidades para generar
 * un documento PDF completo.
 */
public interface GetAuditReportUseCase {

  /**
   * Genera el informe PDF global con todas las estadísticas y vulnerabilidades.
   *
   * @return array de bytes del documento PDF
   */
  byte[] getGlobalReport();

  /** Datos completos del informe global. */
  record GlobalReportData(
      long totalAudits,
      long totalVulnerabilities,
      Map<Integer, Long> bySeverity,
      List<CweStat> topCwes,
      List<AuditDetail> recentAudits
  ) {}

  record CweStat(String cweId, long count) {}

  record VulnerabilityDetail(String cweId, int severity, String description,
                             String remediationPatch) {}

  record AuditDetail(String scanId, LocalDateTime date, String repositoryUrl,
                     String branchName, List<VulnerabilityDetail> vulnerabilities) {}
}
