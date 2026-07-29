package com.amalakaky.aegiscode.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AuditReportTest {

  @Test
  void shouldStartInProgress() {
    var report = new AuditReport("scan-1");
    assertThat(report.getScanId()).isEqualTo("scan-1");
    assertThat(report.getStatus()).isEqualTo(AuditReport.AuditStatus.IN_PROGRESS);
    assertThat(report.getVulnerabilities()).isEmpty();
  }

  @Test
  void shouldAddVulnerability() {
    var report = new AuditReport("scan-1");
    var vuln = new Vulnerability("CWE-89", new SeverityScore(8), "test", null);

    report.addVulnerability(vuln);

    assertThat(report.getVulnerabilities()).hasSize(1);
    assertThat(report.getVulnerabilities().get(0).getCweId()).isEqualTo("CWE-89");
  }

  @Test
  void shouldIgnoreNullVulnerability() {
    var report = new AuditReport("scan-1");
    report.addVulnerability(null);
    assertThat(report.getVulnerabilities()).isEmpty();
  }

  @Test
  void shouldMarkAsCompleted() {
    var report = new AuditReport("scan-1");
    report.markAsCompleted();
    assertThat(report.getStatus()).isEqualTo(AuditReport.AuditStatus.COMPLETED);
  }

  @Test
  void shouldMarkAsFailed() {
    var report = new AuditReport("scan-1");
    report.markAsFailed();
    assertThat(report.getStatus()).isEqualTo(AuditReport.AuditStatus.FAILED);
  }

  @Test
  void shouldReturnUnmodifiableList() {
    var report = new AuditReport("scan-1");
    assertThatThrownBy(() -> report.getVulnerabilities().add(null))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldBuildWithLombokBuilder() {
    var report = AuditReport.builder()
        .scanId("scan-2")
        .status(AuditReport.AuditStatus.COMPLETED)
        .repositoryUrl("https://github.com/amalakaky/demo.git")
        .branchName("main")
        .build();

    assertThat(report.getScanId()).isEqualTo("scan-2");
    assertThat(report.getRepositoryUrl()).isEqualTo("https://github.com/amalakaky/demo.git");
  }
}
