package com.amalakaky.aegiscode.infrastructure.adapter.out.db;

import com.amalakaky.aegiscode.application.port.out.db.AuditStatisticsRepositoryPort;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity.VulnerabilitySeverityStatsEntity;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository.AuditSummaryProjection;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository.DataAuditRepository;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository.VulnerabilityCweCountProjection;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository.VulnerabilitySeverityStatsRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de infraestructura que implementa {@link AuditStatisticsRepositoryPort}
 * consultando la vista de base de datos {@code view_vulnerability_severity_stats}
 * y el repositorio {@link DataAuditRepository} para estadísticas agregadas.
 * <p>
 * La entidad {@link VulnerabilitySeverityStatsEntity} está mapeada como
 * {@link org.hibernate.annotations.Immutable} porque proviene de una vista
 * de solo lectura.
 */
@Repository
public class AuditStatisticsAdapter implements AuditStatisticsRepositoryPort {

  private final VulnerabilitySeverityStatsRepository statsRepository;
  private final DataAuditRepository dataAuditRepository;

  public AuditStatisticsAdapter(VulnerabilitySeverityStatsRepository statsRepository,
      DataAuditRepository dataAuditRepository) {
    this.statsRepository = statsRepository;
    this.dataAuditRepository = dataAuditRepository;
  }

  @Override
  public Map<Integer, Long> getVulnerabilitiesBySeverity() {
    return statsRepository.findAll().stream()
        .collect(Collectors.toMap(
            VulnerabilitySeverityStatsEntity::getSeverity,
            VulnerabilitySeverityStatsEntity::getTotalCount
        ));
  }

  @Override
  public long totalAudits() {
    return dataAuditRepository.count();
  }

  @Override
  public long totalVulnerabilities() {
    return dataAuditRepository.countVulnerabilities();
  }

  @Override
  public List<CweCount> countByCwe() {
    return dataAuditRepository.countByCwe().stream()
        .map(p -> new CweCount(p.getCweId(), p.getCount()))
        .collect(Collectors.toList());
  }

  @Override
  public List<AuditSummary> findRecentAudits(int limit) {
    return dataAuditRepository.findRecentAudits(PageRequest.of(0, limit))
        .stream()
        .map(this::toAuditSummary)
        .collect(Collectors.toList());
  }

  private AuditSummary toAuditSummary(AuditSummaryProjection p) {
    return new AuditSummary(
        p.getScanId(),
        p.getCreatedAt(),
        p.getRepositoryUrl(),
        p.getBranchName(),
        p.getTotalVulnerabilities() != null ? p.getTotalVulnerabilities() : 0L
    );
  }
}
