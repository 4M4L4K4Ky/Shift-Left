package com.amalakaky.aegiscode.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amalakaky.aegiscode.application.port.out.db.AuditStatisticsRepositoryPort;
import com.amalakaky.aegiscode.domain.model.CweCount;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAuditStatisticsUseCaseImplTest {

  @Mock
  private AuditStatisticsRepositoryPort port;

  @InjectMocks
  private GetAuditStatisticsUseCaseImpl useCase;

  @Test
  void getDashboardStats_shouldCombineAllStats() {
    when(port.totalAudits()).thenReturn(10L);
    when(port.totalVulnerabilities()).thenReturn(100L);
    when(port.getVulnerabilitiesBySeverity()).thenReturn(Map.of(5, 50L, 3, 30L));
    when(port.countByCwe()).thenReturn(List.of(
        new CweCount("CWE-89", 20L),
        new CweCount("CWE-79", 15L)
    ));
    when(port.findRecentAudits(10)).thenReturn(List.of(
        new AuditStatisticsRepositoryPort.AuditSummary(
            "scan-1",
            LocalDateTime.of(2026, 7, 25, 10, 0),
            "https://github.com/amalakaky/demo.git",
            "main",
            5L
        )
    ));

    var result = useCase.getDashboardStats();

    assertThat(result.totalAudits()).isEqualTo(10L);
    assertThat(result.totalVulnerabilities()).isEqualTo(100L);
    assertThat(result.bySeverity()).containsEntry(5, 50L).containsEntry(3, 30L);
    assertThat(result.byCwe()).hasSize(2);
    assertThat(result.byCwe().get(0).cweId()).isEqualTo("CWE-89");
    assertThat(result.byCwe().get(0).count()).isEqualTo(20L);
    assertThat(result.recentAudits()).hasSize(1);
    assertThat(result.recentAudits().get(0).scanId()).isEqualTo("scan-1");
    assertThat(result.recentAudits().get(0).totalVulnerabilities()).isEqualTo(5L);

    verify(port).totalAudits();
    verify(port).totalVulnerabilities();
    verify(port).getVulnerabilitiesBySeverity();
    verify(port).countByCwe();
    verify(port).findRecentAudits(10);
  }

  @Test
  void getDashboardStats_shouldHandleEmptyData() {
    when(port.totalAudits()).thenReturn(0L);
    when(port.totalVulnerabilities()).thenReturn(0L);
    when(port.getVulnerabilitiesBySeverity()).thenReturn(Map.of());
    when(port.countByCwe()).thenReturn(List.of());
    when(port.findRecentAudits(10)).thenReturn(List.of());

    var result = useCase.getDashboardStats();

    assertThat(result.totalAudits()).isZero();
    assertThat(result.totalVulnerabilities()).isZero();
    assertThat(result.bySeverity()).isEmpty();
    assertThat(result.byCwe()).isEmpty();
    assertThat(result.recentAudits()).isEmpty();
  }
}
