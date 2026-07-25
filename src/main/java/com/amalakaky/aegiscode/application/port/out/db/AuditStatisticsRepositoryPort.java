package com.amalakaky.aegiscode.application.port.out.db;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Puerto de salida para estadísticas de vulnerabilidades.
 * 
 * Proporciona datos agregados desde la vista de base de datos
 * {@code view_vulnerability_severity_stats} y consultas analíticas
 * adicionales sobre auditorías y vulnerabilidades.
 */
public interface AuditStatisticsRepositoryPort {

  /**
   * Retorna el recuento de vulnerabilidades agrupadas por nivel de severidad.
   *
   * @return mapa con severidad como clave y total como valor
   */
  Map getVulnerabilitiesBySeverity();

  /**
   * Total de auditorías realizadas en la plataforma.
   */
  long totalAudits();

  /**
   * Total de vulnerabilidades detectadas en todas las auditorías.
   */
  long totalVulnerabilities();

  /**
   * Top CWEs más frecuentes entre todas las vulnerabilidades detectadas.
   *
   * @return lista de pares (cweId, count) ordenados de mayor a menor
   */
  List<CweCount> countByCwe();

  /**
   * Últimas auditorías realizadas con resumen de vulnerabilidades.
   *
   * @param limit número máximo de resultados
   * @return lista de resúmenes de auditoría
   */
  List<AuditSummary> findRecentAudits(int limit);

  /** Par simple CWE + conteo. */
  record CweCount(String cweId, long count) {}

  /** Resumen de una auditoría individual. */
  record AuditSummary(String scanId, LocalDateTime date, String repositoryUrl,
                      String branchName, long totalVulnerabilities) {}
}



