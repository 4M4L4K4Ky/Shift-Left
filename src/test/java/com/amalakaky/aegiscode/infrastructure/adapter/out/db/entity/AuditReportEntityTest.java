package com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.amalakaky.aegiscode.domain.model.AuditReport;
import com.amalakaky.aegiscode.domain.model.AuditReport.AuditStatus;
import com.amalakaky.aegiscode.domain.model.SeverityScore;
import com.amalakaky.aegiscode.domain.model.Vulnerability;
import java.util.List;
import org.junit.jupiter.api.Test;

class AuditReportEntityTest {

  @Test
  void fromDomain_shouldMapAllFields() {
    var domain = new AuditReport("scan-1");
    domain.setRepositoryUrl("https://github.com/amalakaky/demo.git");
    domain.setBranchName("main");
    domain.addVulnerability(new Vulnerability("CWE-89", new SeverityScore(8), "SQLi", "fix"));
    domain.markAsCompleted();

    var entity = AuditReportEntity.fromDomain(domain);

    assertThat(entity.getScanId()).isEqualTo("scan-1");
    assertThat(entity.getStatus()).isEqualTo(AuditStatus.COMPLETED);
    assertThat(entity.getRepositoryUrl()).isEqualTo("https://github.com/amalakaky/demo.git");
    assertThat(entity.getBranchName()).isEqualTo("main");
    assertThat(entity.getCreatedAt()).isNotNull();
    assertThat(entity.getVulnerabilities()).hasSize(1);
    assertThat(entity.getVulnerabilities().get(0).getCweId()).isEqualTo("CWE-89");
  }

  @Test
  void fromDomain_shouldHandleNullVulnerabilities() {
    var domain = new AuditReport("scan-2");
    domain.markAsCompleted();

    var entity = AuditReportEntity.fromDomain(domain);

    assertThat(entity.getScanId()).isEqualTo("scan-2");
    assertThat(entity.getVulnerabilities()).isEmpty();
  }

  @Test
  void builder_shouldCreateEntity() {
    var entity = AuditReportEntity.builder()
        .scanId("scan-3")
        .status(AuditStatus.FAILED)
        .build();

    assertThat(entity.getScanId()).isEqualTo("scan-3");
    assertThat(entity.getStatus()).isEqualTo(AuditStatus.FAILED);
  }

  @Test
  void builder_withNullVulnerabilities_shouldUseEmptyList() {
    var entity = AuditReportEntity.builder()
        .scanId("scan-4")
        .status(AuditStatus.COMPLETED)
        .vulnerabilities(null)
        .build();

    assertThat(entity.getVulnerabilities()).isEmpty();
  }

  @Test
  void builder_withProvidedVulnerabilities_shouldUseThem() {
    var vuln = VulnerabilityEntity.builder()
        .cweId("CWE-89").severity(9).description("SQLi").build();
    var entity = AuditReportEntity.builder()
        .scanId("scan-5")
        .status(AuditStatus.COMPLETED)
        .vulnerabilities(List.of(vuln))
        .build();

    assertThat(entity.getVulnerabilities()).hasSize(1);
    assertThat(entity.getVulnerabilities().get(0).getCweId()).isEqualTo("CWE-89");
  }
}
