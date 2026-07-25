package com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository;

import java.time.LocalDateTime;

/**
 * Proyección de Spring Data JPA para el resumen de auditorías recientes.
 * 
 * Los nombres de los getters deben coincidir con los alias de la consulta JPQL
 * en {@link DataAuditRepository#findRecentAudits}.
 */
public interface AuditSummaryProjection {

  String getScanId();

  LocalDateTime getCreatedAt();

  String getRepositoryUrl();

  String getBranchName();

  Long getTotalVulnerabilities();
}
