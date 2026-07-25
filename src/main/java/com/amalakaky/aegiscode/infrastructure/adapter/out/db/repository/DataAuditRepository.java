package com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository;

import com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity.AuditReportEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link AuditReportEntity}.
 * <p>
 * Proporciona operaciones CRUD básicas sobre la tabla {@code audit_reports}
 * heredadas de {@link JpaRepository}, además de consultas agregadas para
 * estadísticas de vulnerabilidades.
 */
@Repository
public interface DataAuditRepository extends JpaRepository<AuditReportEntity, String> {

  @Query("SELECT COUNT(v) FROM VulnerabilityEntity v")
  long countVulnerabilities();

  @Query("SELECT v.cweId AS cweId, COUNT(v) AS count FROM VulnerabilityEntity v "
      + "GROUP BY v.cweId ORDER BY count DESC")
  List<VulnerabilityCweCountProjection> countByCwe();

  @Query("SELECT a.scanId AS scanId, a.createdAt AS createdAt, "
      + "a.repositoryUrl AS repositoryUrl, a.branchName AS branchName, "
      + "COALESCE((SELECT COUNT(v) FROM VulnerabilityEntity v "
      + "WHERE v.auditReport.scanId = a.scanId), 0) AS totalVulnerabilities "
      + "FROM AuditReportEntity a ORDER BY a.createdAt DESC")
  List<AuditSummaryProjection> findRecentAudits(Pageable pageable);
}



