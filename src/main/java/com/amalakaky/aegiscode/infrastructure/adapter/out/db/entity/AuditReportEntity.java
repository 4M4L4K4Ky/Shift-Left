package com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity;

import com.amalakaky.aegiscode.domain.model.AuditReport;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA que mapea la tabla {@code audit_reports}.
 * 
 * Contiene el resultado de una auditoría: estado, metadatos del repositorio
 * y la colección de vulnerabilidades detectadas (relación OneToMany).
 * Se utiliza exclusivamente en la capa de infraestructura; el dominio trabaja
 * con {@link AuditReport}.
 */
@Entity
@Table(name = "audit_reports")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditReportEntity {

  @Id
  @Column(name = "scan_id", length = 36, nullable = false, updatable = false)
  private String scanId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private AuditReport.AuditStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "repository_url", length = 512)
  private String repositoryUrl;

  @Column(name = "branch_name", length = 100)
  private String branchName;

  @OneToMany(mappedBy = "auditReport", cascade = CascadeType.ALL,
      orphanRemoval = true, fetch = FetchType.LAZY)
  private List<VulnerabilityEntity> vulnerabilities = new ArrayList<>();

  @Builder
  public AuditReportEntity(String scanId, AuditReport.AuditStatus status,
      LocalDateTime createdAt, String repositoryUrl, String branchName,
      List<VulnerabilityEntity> vulnerabilities) {
    this.scanId = scanId;
    this.status = status;
    this.createdAt = createdAt;
    this.repositoryUrl = repositoryUrl;
    this.branchName = branchName;
    if (vulnerabilities != null) {
      this.vulnerabilities = vulnerabilities;
    }
  }

  /** Convierte un {@link AuditReport} del dominio a esta entidad JPA. */
  public static AuditReportEntity fromDomain(AuditReport report) {
    AuditReportEntity entity = AuditReportEntity.builder()
        .scanId(report.getScanId())
        .status(report.getStatus())
        .createdAt(LocalDateTime.now())
        .repositoryUrl(report.getRepositoryUrl())
        .branchName(report.getBranchName())
        .build();

    if (report.getVulnerabilities() != null) {
      List<VulnerabilityEntity> vulnEntities = report.getVulnerabilities().stream()
          .map(v -> VulnerabilityEntity.fromDomain(v, entity))
          .collect(Collectors.toList());
      entity.setVulnerabilities(vulnEntities);
    }

    return entity;
  }
}




