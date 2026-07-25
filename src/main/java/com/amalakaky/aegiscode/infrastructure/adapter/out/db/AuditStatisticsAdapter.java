package com.amalakaky.aegiscode.infrastructure.adapter.out.db;

import com.amalakaky.aegiscode.application.port.out.db.AuditStatisticsRepositoryPort;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity.VulnerabilitySeverityStatsEntity;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository.VulnerabilitySeverityStatsRepository;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class AuditStatisticsAdapter implements AuditStatisticsRepositoryPort {

  private final VulnerabilitySeverityStatsRepository statsRepository;

  public AuditStatisticsAdapter(VulnerabilitySeverityStatsRepository statsRepository) {
    this.statsRepository = statsRepository;
  }

  @Override
  public Map<Integer, Long> getVulnerabilitiesBySeverity() {
    return statsRepository.findAll().stream()
        .collect(Collectors.toMap(
            VulnerabilitySeverityStatsEntity::getSeverity,
            VulnerabilitySeverityStatsEntity::getTotalCount
        ));
  }
}
