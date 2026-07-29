package com.amalakaky.aegiscode.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.amalakaky.aegiscode.application.port.in.GetAuditReportUseCase.AuditDetail;
import com.amalakaky.aegiscode.application.port.in.GetAuditReportUseCase.GlobalReportData;
import com.amalakaky.aegiscode.application.port.in.GetAuditStatisticsUseCase;
import com.amalakaky.aegiscode.application.port.in.GetAuditStatisticsUseCase.DashboardStats;
import com.amalakaky.aegiscode.application.port.in.GetAuditStatisticsUseCase.RecentAuditItem;
import com.amalakaky.aegiscode.application.port.out.ReportGeneratorPort;
import com.amalakaky.aegiscode.application.port.out.db.AuditStatisticsRepositoryPort;
import com.amalakaky.aegiscode.application.port.out.db.AuditStatisticsRepositoryPort.AuditSummary;
import com.amalakaky.aegiscode.domain.model.CweCount;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAuditReportUseCaseImplTest {

  @Mock
  private GetAuditStatisticsUseCase statsUseCase;

  @Mock
  private AuditStatisticsRepositoryPort statsPort;

  @Mock
  private ReportGeneratorPort reportGenerator;

  @InjectMocks
  private GetAuditReportUseCaseImpl useCase;

  @Captor
  private ArgumentCaptor<GlobalReportData> dataCaptor;

  @Test
  void shouldGenerateGlobalReport() {
    var dashboard = new DashboardStats(10L, 100L, Map.of(5, 50L), List.of(), List.of());
    when(statsUseCase.getDashboardStats()).thenReturn(dashboard);

    var summaries = List.of(
        new AuditSummary("s1", LocalDateTime.of(2026, 7, 25, 10, 0),
            "https://github.com/amalakaky/demo.git", "main", 5L)
    );
    when(statsPort.findRecentAudits(10)).thenReturn(summaries);

    var pdfBytes = "PDF".getBytes();
    when(reportGenerator.generateGlobalPdf(dataCaptor.capture())).thenReturn(pdfBytes);

    byte[] result = useCase.getGlobalReport();

    assertThat(result).isEqualTo(pdfBytes);

    var captured = dataCaptor.getValue();
    assertThat(captured.totalAudits()).isEqualTo(10L);
    assertThat(captured.totalVulnerabilities()).isEqualTo(100L);
    assertThat(captured.bySeverity()).containsEntry(5, 50L);
    assertThat(captured.recentAudits()).hasSize(1);
    assertThat(captured.recentAudits().get(0).scanId()).isEqualTo("s1");
  }

  @Test
  void shouldGenerateReportWithEmptyData() {
    var dashboard = new DashboardStats(0L, 0L, Map.of(), List.of(), List.of());
    when(statsUseCase.getDashboardStats()).thenReturn(dashboard);
    when(statsPort.findRecentAudits(10)).thenReturn(List.of());

    var pdfBytes = "EMPTY_PDF".getBytes();
    when(reportGenerator.generateGlobalPdf(dataCaptor.capture())).thenReturn(pdfBytes);

    byte[] result = useCase.getGlobalReport();

    assertThat(result).isEqualTo(pdfBytes);
    var captured = dataCaptor.getValue();
    assertThat(captured.totalAudits()).isZero();
    assertThat(captured.recentAudits()).isEmpty();
  }
}
