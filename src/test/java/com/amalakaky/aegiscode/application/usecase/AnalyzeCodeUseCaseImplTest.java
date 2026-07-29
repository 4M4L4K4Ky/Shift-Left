package com.amalakaky.aegiscode.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amalakaky.aegiscode.application.port.out.ai.AuditorAgentPort;
import com.amalakaky.aegiscode.application.port.out.ai.RemediationAgentPort;
import com.amalakaky.aegiscode.application.port.out.ai.RepositoryScannerPort;
import com.amalakaky.aegiscode.application.port.out.db.AuditRepositoryPort;
import com.amalakaky.aegiscode.domain.model.AuditReport;
import com.amalakaky.aegiscode.domain.model.AuditReport.AuditStatus;
import com.amalakaky.aegiscode.domain.model.SeverityScore;
import com.amalakaky.aegiscode.domain.model.Vulnerability;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyzeCodeUseCaseImplTest {

  @Mock
  private AuditorAgentPort auditorAgent;

  @Mock
  private RemediationAgentPort remediationAgent;

  @Mock
  private RepositoryScannerPort repositoryScanner;

  @Mock
  private AuditRepositoryPort repository;

  @InjectMocks
  private AnalyzeCodeUseCaseImpl useCase;

  @Captor
  private ArgumentCaptor<AuditReport> reportCaptor;

  @Test
  void executeScan_shouldProcessWhenVulnerabilityFound() {
    var sourceCode = "public class Test {}";
    var detected = new Vulnerability("CWE-89", new SeverityScore(8), "SQLi", null);
    when(auditorAgent.analyze(sourceCode)).thenReturn(Optional.of(detected));
    when(remediationAgent.generateCleanPatch(sourceCode, "CWE-89")).thenReturn("PATCHED");
    when(repository.save(reportCaptor.capture())).thenAnswer(i -> i.getArgument(0));

    var result = useCase.executeScan("scan-1", sourceCode);

    assertThat(result.getScanId()).isEqualTo("scan-1");
    assertThat(result.getRepositoryUrl()).isEqualTo("INLINE_SOURCE_SNIPPET");
    assertThat(result.getBranchName()).isEqualTo("DIRECT_PAYLOAD");
    assertThat(result.getStatus()).isEqualTo(AuditStatus.COMPLETED);
    assertThat(result.getVulnerabilities()).hasSize(1);
    assertThat(result.getVulnerabilities().get(0).getRemediationPatch()).isEqualTo("PATCHED");

    verify(auditorAgent).analyze(sourceCode);
    verify(remediationAgent).generateCleanPatch(sourceCode, "CWE-89");
    verify(repository).save(result);
  }

  @Test
  void executeScan_shouldSkipRemediationWhenNoVulnerability() {
    var sourceCode = "safe code";
    when(auditorAgent.analyze(sourceCode)).thenReturn(Optional.empty());
    when(repository.save(reportCaptor.capture())).thenAnswer(i -> i.getArgument(0));

    var result = useCase.executeScan("scan-2", sourceCode);

    assertThat(result.getVulnerabilities()).isEmpty();
    assertThat(result.getStatus()).isEqualTo(AuditStatus.COMPLETED);
    verify(auditorAgent).analyze(sourceCode);
    verify(repository).save(result);
  }

  @Test
  void executeRepositoryScan_shouldAddAllDetectedVulnerabilities() {
    var code = "code from repo";
    var vulns = List.of(
        new Vulnerability("CWE-79", new SeverityScore(6), "XSS", "fix1"),
        new Vulnerability("CWE-89", new SeverityScore(9), "SQLi", "fix2")
    );
    when(repositoryScanner.scanRepository(code)).thenReturn(vulns);
    when(repository.save(reportCaptor.capture())).thenAnswer(i -> i.getArgument(0));

    var result = useCase.executeRepositoryScan("scan-3", code,
        "https://github.com/amalakaky/demo.git", "main");

    assertThat(result.getScanId()).isEqualTo("scan-3");
    assertThat(result.getRepositoryUrl()).isEqualTo("https://github.com/amalakaky/demo.git");
    assertThat(result.getBranchName()).isEqualTo("main");
    assertThat(result.getStatus()).isEqualTo(AuditStatus.COMPLETED);
    assertThat(result.getVulnerabilities()).hasSize(2);
    assertThat(result.getVulnerabilities().get(0).getCweId()).isEqualTo("CWE-79");

    verify(repositoryScanner).scanRepository(code);
    verify(repository).save(result);
  }

  @Test
  void executeRepositoryScan_shouldHandleEmptyResults() {
    when(repositoryScanner.scanRepository("empty code")).thenReturn(List.of());
    when(repository.save(reportCaptor.capture())).thenAnswer(i -> i.getArgument(0));

    var result = useCase.executeRepositoryScan("scan-4", "empty code",
        "https://example.com/repo.git", "dev");

    assertThat(result.getVulnerabilities()).isEmpty();
    assertThat(result.getStatus()).isEqualTo(AuditStatus.COMPLETED);
  }
}
