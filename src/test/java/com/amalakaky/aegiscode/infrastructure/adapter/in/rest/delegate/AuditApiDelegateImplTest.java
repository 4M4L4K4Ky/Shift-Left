package com.amalakaky.aegiscode.infrastructure.adapter.in.rest.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.any;

import com.amalakaky.aegiscode.application.port.in.AnalyzeCodeUseCase;
import com.amalakaky.aegiscode.application.port.in.GetAuditReportUseCase;
import com.amalakaky.aegiscode.application.port.in.GetAuditStatisticsUseCase;
import com.amalakaky.aegiscode.application.port.in.GetAuditStatisticsUseCase.DashboardStats;
import com.amalakaky.aegiscode.application.port.in.GetAuditStatisticsUseCase.RecentAuditItem;
import com.amalakaky.aegiscode.application.port.out.vcs.GitProviderPort;
import com.amalakaky.aegiscode.domain.model.AuditReport;
import com.amalakaky.aegiscode.domain.model.AuditReport.AuditStatus;
import com.amalakaky.aegiscode.domain.model.CweCount;
import com.amalakaky.aegiscode.domain.model.SeverityScore;
import com.amalakaky.aegiscode.domain.model.Vulnerability;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.AuditEngineApiDelegate;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.AuditReportDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.AuditReportDto.StatusEnum;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.GitHubScanRequestDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.ScanRequestDto;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuditApiDelegateImplTest {

  @Mock
  private AnalyzeCodeUseCase analyzeCodeUseCase;

  @Mock
  private GitProviderPort gitProviderPort;

  @Mock
  private GetAuditStatisticsUseCase getAuditStatisticsUseCase;

  @Mock
  private GetAuditReportUseCase getAuditReportUseCase;

  @InjectMocks
  private AuditApiDelegateImpl delegate;

  private static AuditReport completedReport(String scanId, List<Vulnerability> vulns) {
    var report = new AuditReport(scanId);
    if (vulns != null) {
      vulns.forEach(report::addVulnerability);
    }
    report.markAsCompleted();
    return report;
  }

  @Nested
  class AuditsInlinePost {

    @Test
    @SneakyThrows
    void shouldReturn202WithFullMapping() {
      var request = new ScanRequestDto();
      request.setSourceCode("public class Test {}");
      var report = completedReport("scan-1", List.of(
          new Vulnerability("CWE-89", new SeverityScore(9), "SQL Injection", "fix"),
          new Vulnerability("CWE-79", new SeverityScore(7), "XSS", null)
      ));
      when(analyzeCodeUseCase.executeScan(anyString(), any())).thenReturn(report);

      ResponseEntity<AuditReportDto> response = delegate.auditsInlinePost(request);

      assertThat(response.getStatusCodeValue()).isEqualTo(202);
      AuditReportDto body = response.getBody();
      assertThat(body.getScanId()).isEqualTo("scan-1");
      assertThat(body.getStatus()).isEqualTo(StatusEnum.COMPLETED);
      assertThat(body.getVulnerabilities()).hasSize(2);
      assertThat(body.getVulnerabilities().get(0).getSeverity().getValue()).isEqualTo(9);
      assertThat(body.getVulnerabilities().get(1).getSeverity().getValue()).isEqualTo(7);
      assertThat(body.getVulnerabilities().get(1).getRemediationPatch())
          .isEqualTo("Pending architectural review");
    }

    @Test
    void whenSourceCodeNull_shouldHandleGracefully() {
      var request = new ScanRequestDto();
      var report = completedReport("scan-2", List.of());
      when(analyzeCodeUseCase.executeScan(anyString(), any())).thenReturn(report);

      ResponseEntity<AuditReportDto> response = delegate.auditsInlinePost(request);

      assertThat(response.getStatusCodeValue()).isEqualTo(202);
    }

    @Test
    void whenStatusNull_shouldNotSetStatusInDto() {
      var request = new ScanRequestDto();
      request.setSourceCode("code");
      var report = AuditReport.builder()
          .scanId("scan-3").vulnerabilities(List.of()).status(null).repositoryUrl(null).branchName(null)
          .build();
      when(analyzeCodeUseCase.executeScan(anyString(), any())).thenReturn(report);

      ResponseEntity<AuditReportDto> response = delegate.auditsInlinePost(request);

      assertThat(response.getBody().getStatus()).isNull();
    }

    @Test
    void whenVulnerabilitySeverityNull_shouldNotSetSeverityInDto() {
      var request = new ScanRequestDto();
      request.setSourceCode("code");
      var report = completedReport("scan-4", List.of(
          Vulnerability.builder().cweId("CWE-89").severity(null).description("desc").build()
      ));
      when(analyzeCodeUseCase.executeScan(anyString(), any())).thenReturn(report);

      ResponseEntity<AuditReportDto> response = delegate.auditsInlinePost(request);

      assertThat(response.getBody().getVulnerabilities().get(0).getSeverity()).isNull();
    }

    @Test
    void whenUseCaseThrows_shouldRethrowSameException() {
      var request = new ScanRequestDto();
      request.setSourceCode("code");
      when(analyzeCodeUseCase.executeScan(anyString(), anyString()))
          .thenThrow(new RuntimeException("scan failed"));

      assertThatThrownBy(() -> delegate.auditsInlinePost(request))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("scan failed");
    }
  }

  @Nested
  class AuditsRepositoryPost {

    @Test
    void shouldReturn200WithMappedReport() throws Exception {
      var request = new GitHubScanRequestDto();
      request.setRepositoryUrl("https://github.com/test/repo.git");
      request.setBranch("main");
      var file = new File("pom.xml");
      when(gitProviderPort.fetchSourceFiles(anyString(), anyString())).thenReturn(List.of(file));
      var report = completedReport("scan-repo-1", List.of(
          new Vulnerability("CWE-22", new SeverityScore(5), "Path Traversal", "fix")
      ));
      when(analyzeCodeUseCase.executeRepositoryScan(anyString(), anyString(), anyString(), anyString()))
          .thenReturn(report);

      ResponseEntity<AuditReportDto> response = delegate.auditsRepositoryPost(request);

      assertThat(response.getStatusCodeValue()).isEqualTo(200);
      assertThat(response.getBody().getVulnerabilities()).hasSize(1);
      assertThat(response.getBody().getVulnerabilities().get(0).getCweId()).isEqualTo("CWE-22");
    }

    @Test
    void whenFileReadFails_shouldLogAndContinue() throws Exception {
      var request = new GitHubScanRequestDto();
      request.setRepositoryUrl("https://github.com/test/repo.git");
      request.setBranch("main");
      var dir = new File(System.getProperty("java.io.tmpdir"));
      when(gitProviderPort.fetchSourceFiles(anyString(), anyString())).thenReturn(List.of(dir));
      var report = completedReport("scan-repo-2", List.of());
      when(analyzeCodeUseCase.executeRepositoryScan(anyString(), anyString(), anyString(), anyString()))
          .thenReturn(report);

      ResponseEntity<AuditReportDto> response = delegate.auditsRepositoryPost(request);

      assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    void whenUseCaseThrows_shouldRethrowSameException() {
      var request = new GitHubScanRequestDto();
      request.setRepositoryUrl("https://github.com/test/repo.git");
      request.setBranch("main");
      when(gitProviderPort.fetchSourceFiles(anyString(), anyString()))
          .thenThrow(new RuntimeException("clone failed"));

      assertThatThrownBy(() -> delegate.auditsRepositoryPost(request))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("clone failed");
    }
  }

  @Nested
  class AuditsStatisticsGet {

    @Test
    void shouldMapAllFields() {
      var stats = new DashboardStats(
          10L, 100L,
          Map.of(9, 50L, 7, 30L),
          List.of(new CweCount("CWE-89", 30L), new CweCount("CWE-79", 20L)),
          List.of(
              new RecentAuditItem("s1", LocalDateTime.of(2026, 7, 25, 10, 0),
                  "https://github.com/a/b.git", "main", 5L)
          )
      );
      when(getAuditStatisticsUseCase.getDashboardStats()).thenReturn(stats);

      var response = delegate.auditsStatisticsGet();

      assertThat(response.getStatusCodeValue()).isEqualTo(200);
      var body = response.getBody();
      assertThat(body.getTotalAudits()).isEqualTo(10L);
      assertThat(body.getTotalVulnerabilities()).isEqualTo(100L);
      assertThat(body.getSeverityCounts()).containsEntry("9", 50L).containsEntry("7", 30L);
      assertThat(body.getByCwe()).hasSize(2);
      assertThat(body.getByCwe().get(0).getCweId()).isEqualTo("CWE-89");
      assertThat(body.getRecentAudits()).hasSize(1);
      assertThat(body.getRecentAudits().get(0).getScanId()).isEqualTo("s1");
      assertThat(body.getRecentAudits().get(0).getDate()).isNotNull();
    }

    @Test
    void withEmptyData_shouldHandleGracefully() {
      var stats = new DashboardStats(0L, 0L, Map.of(), List.of(), List.of());
      when(getAuditStatisticsUseCase.getDashboardStats()).thenReturn(stats);

      var response = delegate.auditsStatisticsGet();

      assertThat(response.getStatusCodeValue()).isEqualTo(200);
      assertThat(response.getBody().getByCwe()).isEmpty();
      assertThat(response.getBody().getRecentAudits()).isEmpty();
      assertThat(response.getBody().getSeverityCounts()).isEmpty();
    }
  }

  @Nested
  class GetAuditReport {

    @Test
    void shouldReturnPdfAttachment() {
      byte[] pdfBytes = "PDF_CONTENT".getBytes();
      when(getAuditReportUseCase.getGlobalReport()).thenReturn(pdfBytes);

      var response = delegate.getAuditReport();

      assertThat(response.getStatusCodeValue()).isEqualTo(200);
      assertThat(response.getHeaders().getContentDisposition().toString())
          .contains("informe-global.pdf");
      assertThat(response.getHeaders().getContentLength()).isEqualTo(pdfBytes.length);
    }

    @Test
    void whenUseCaseFails_shouldRethrow() {
      when(getAuditReportUseCase.getGlobalReport())
          .thenThrow(new RuntimeException("PDF generation failed"));

      assertThatThrownBy(() -> delegate.getAuditReport())
          .isInstanceOf(RuntimeException.class)
          .hasMessage("PDF generation failed");
    }
  }
}
