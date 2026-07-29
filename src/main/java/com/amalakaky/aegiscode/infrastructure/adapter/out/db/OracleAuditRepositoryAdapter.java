package com.amalakaky.aegiscode.infrastructure.adapter.out.db;

import com.amalakaky.aegiscode.application.port.out.db.AuditRepositoryPort;
import com.amalakaky.aegiscode.domain.model.AuditReport;
import com.amalakaky.aegiscode.domain.model.SeverityScore;
import com.amalakaky.aegiscode.domain.model.Vulnerability;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity.AuditReportEntity;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity.VulnerabilityEntity;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository.DataAuditRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de infraestructura JPA que implementa {@link AuditRepositoryPort}.
 * 
 * Convierte entre el modelo del dominio ({@link AuditReport}) y la entidad
 * JPA ({@link AuditReportEntity}) para persistencia en Oracle ATP (producción)
 * o H2 (desarrollo local). Las transacciones de escritura son gestionadas
 * por Spring {@link Transactional}.
 */
@Repository
public class OracleAuditRepositoryAdapter implements AuditRepositoryPort {

  private final DataAuditRepository jpaRepository;

  public OracleAuditRepositoryAdapter(DataAuditRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  @Transactional
  public AuditReport save(AuditReport report) {
    AuditReportEntity entity = AuditReportEntity.fromDomain(report);
    AuditReportEntity savedEntity = jpaRepository.save(entity);
    return mapToDomain(savedEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public AuditReport findById(String scanId) {
    Optional<AuditReportEntity> entityOpt = jpaRepository.findById(scanId);
    return entityOpt.map(this::mapToDomain).orElse(null);
  }

  /** Convierte entidad JPA a modelo de dominio. */
  private AuditReport mapToDomain(AuditReportEntity entity) {
    AuditReport report = new AuditReport(entity.getScanId());
    report.setRepositoryUrl(entity.getRepositoryUrl());
    report.setBranchName(entity.getBranchName());

    if (entity.getStatus() == AuditReport.AuditStatus.COMPLETED) {
      report.markAsCompleted();
    } else if (entity.getStatus() == AuditReport.AuditStatus.FAILED) {
      report.markAsFailed();
    }

    if (entity.getVulnerabilities() != null) {
      for (VulnerabilityEntity vulnEntity : entity.getVulnerabilities()) {
        Vulnerability vulnerability = new Vulnerability(
                vulnEntity.getCweId(),
                new SeverityScore(vulnEntity.getSeverity()),
                vulnEntity.getDescription(),
                vulnEntity.getRemediationPatch()
        );
        report.addVulnerability(vulnerability);
      }
    }

    return report;
  }
}



