package com.amalakaky.aegiscode.infrastructure.adapter.out.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.amalakaky.aegiscode.domain.model.CweCount;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity.VulnerabilitySeverityStatsEntity;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository.AuditSummaryProjection;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository.DataAuditRepository;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository.VulnerabilityCweCountProjection;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository.VulnerabilitySeverityStatsRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AuditStatisticsAdapterTest {

  @Mock
  private VulnerabilitySeverityStatsRepository statsRepository;

  @Mock
  private DataAuditRepository dataAuditRepository;

  private AuditStatisticsAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new AuditStatisticsAdapter(statsRepository, dataAuditRepository);
  }

  @Test
  void getVulnerabilitiesBySeverity_shouldMapStatsToMap() {
    var entity1 = mockSeverityStats(9, 50L);
    var entity2 = mockSeverityStats(7, 30L);
    when(statsRepository.findAll()).thenReturn(List.of(entity1, entity2));

    var result = adapter.getVulnerabilitiesBySeverity();

    assertThat(result).hasSize(2);
    assertThat(result).containsEntry(9, 50L).containsEntry(7, 30L);
  }

  @Test
  void getVulnerabilitiesBySeverity_shouldReturnEmptyMapWhenNoStats() {
    when(statsRepository.findAll()).thenReturn(List.of());

    var result = adapter.getVulnerabilitiesBySeverity();

    assertThat(result).isEmpty();
  }

  @Test
  void totalAudits_shouldReturnCountFromRepository() {
    when(dataAuditRepository.count()).thenReturn(42L);

    var result = adapter.totalAudits();

    assertThat(result).isEqualTo(42L);
  }

  @Test
  void totalVulnerabilities_shouldReturnCountFromRepository() {
    when(dataAuditRepository.countVulnerabilities()).thenReturn(100L);

    var result = adapter.totalVulnerabilities();

    assertThat(result).isEqualTo(100L);
  }

  @Test
  void countByCwe_shouldMapProjectionToCweCount() {
    var proj = mockCweProjection("CWE-89", 30L);
    when(dataAuditRepository.countByCwe()).thenReturn(List.of(proj));

    var result = adapter.countByCwe();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).cweId()).isEqualTo("CWE-89");
    assertThat(result.get(0).count()).isEqualTo(30L);
  }

  @Test
  void countByCwe_shouldReturnEmptyListWhenNoCwes() {
    when(dataAuditRepository.countByCwe()).thenReturn(List.of());

    var result = adapter.countByCwe();

    assertThat(result).isEmpty();
  }

  @Test
  void findRecentAudits_shouldMapProjectionToAuditSummary() {
    var now = LocalDateTime.now();
    var proj = mockAuditSummaryProjection("scan-1", now, "https://github.com/a/b.git", "main", 5L);
    when(dataAuditRepository.findRecentAudits(anyPageable())).thenReturn(List.of(proj));

    var result = adapter.findRecentAudits(10);

    assertThat(result).hasSize(1);
    var summary = result.get(0);
    assertThat(summary.scanId()).isEqualTo("scan-1");
    assertThat(summary.date()).isEqualTo(now);
    assertThat(summary.repositoryUrl()).isEqualTo("https://github.com/a/b.git");
    assertThat(summary.branchName()).isEqualTo("main");
    assertThat(summary.totalVulnerabilities()).isEqualTo(5L);
  }

  @Test
  void findRecentAudits_whenTotalVulnerabilitiesNull_shouldDefaultToZero() {
    var proj = mockAuditSummaryProjection("scan-2", LocalDateTime.now(), null, null, null);
    when(dataAuditRepository.findRecentAudits(anyPageable())).thenReturn(List.of(proj));

    var result = adapter.findRecentAudits(5);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).totalVulnerabilities()).isZero();
  }

  @Test
  void findRecentAudits_shouldReturnEmptyListWhenNoAudits() {
    when(dataAuditRepository.findRecentAudits(anyPageable())).thenReturn(List.of());

    var result = adapter.findRecentAudits(10);

    assertThat(result).isEmpty();
  }

  private static VulnerabilitySeverityStatsEntity mockSeverityStats(int severity, long count) {
    var entity = org.mockito.Mockito.mock(VulnerabilitySeverityStatsEntity.class);
    when(entity.getSeverity()).thenReturn(severity);
    when(entity.getTotalCount()).thenReturn(count);
    return entity;
  }

  private static VulnerabilityCweCountProjection mockCweProjection(String cweId, long count) {
    var proj = org.mockito.Mockito.mock(VulnerabilityCweCountProjection.class);
    when(proj.getCweId()).thenReturn(cweId);
    when(proj.getCount()).thenReturn(count);
    return proj;
  }

  private static AuditSummaryProjection mockAuditSummaryProjection(
      String scanId, LocalDateTime date, String repoUrl, String branch, Long totalVulns) {
    var proj = org.mockito.Mockito.mock(AuditSummaryProjection.class);
    when(proj.getScanId()).thenReturn(scanId);
    when(proj.getCreatedAt()).thenReturn(date);
    when(proj.getRepositoryUrl()).thenReturn(repoUrl);
    when(proj.getBranchName()).thenReturn(branch);
    when(proj.getTotalVulnerabilities()).thenReturn(totalVulns);
    return proj;
  }

  private static Pageable anyPageable() {
    return org.mockito.ArgumentMatchers.any(Pageable.class);
  }
}
