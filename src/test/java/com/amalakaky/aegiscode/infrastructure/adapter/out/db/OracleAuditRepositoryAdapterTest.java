package com.amalakaky.aegiscode.infrastructure.adapter.out.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.amalakaky.aegiscode.domain.model.AuditReport;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity.AuditReportEntity;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity.VulnerabilityEntity;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository.DataAuditRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OracleAuditRepositoryAdapterTest {

  @Mock
  private DataAuditRepository jpaRepository;

  private OracleAuditRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new OracleAuditRepositoryAdapter(jpaRepository);
  }

  @Test
  void save_shouldPersistAndReturnDomainReport() {
    AuditReport inputReport = mock(AuditReport.class);
    AuditReportEntity mockEntity = mock(AuditReportEntity.class);
    AuditReportEntity mockSavedEntity = mock(AuditReportEntity.class);

    try (MockedStatic<AuditReportEntity> staticEntity = mockStatic(AuditReportEntity.class)) {
      staticEntity.when(() -> AuditReportEntity.fromDomain(inputReport)).thenReturn(mockEntity);
      when(jpaRepository.save(mockEntity)).thenReturn(mockSavedEntity);

      when(mockSavedEntity.getScanId()).thenReturn("scan-1");
      when(mockSavedEntity.getRepositoryUrl()).thenReturn("https://github.com/amalakaky/demo.git");
      when(mockSavedEntity.getBranchName()).thenReturn("main");
      when(mockSavedEntity.getStatus()).thenReturn(AuditReport.AuditStatus.COMPLETED);
      when(mockSavedEntity.getVulnerabilities()).thenReturn(null);

      AuditReport result = adapter.save(inputReport);

      assertThat(result.getScanId()).isEqualTo("scan-1");
      assertThat(result.getRepositoryUrl()).isEqualTo("https://github.com/amalakaky/demo.git");
      assertThat(result.getBranchName()).isEqualTo("main");
      assertThat(result.getStatus()).isEqualTo(AuditReport.AuditStatus.COMPLETED);
      assertThat(result.getVulnerabilities()).isEmpty();
    }
  }

  @Test
  void findById_shouldReturnNullWhenNotFound() {
    when(jpaRepository.findById("unknown-id")).thenReturn(Optional.empty());

    AuditReport result = adapter.findById("unknown-id");

    assertThat(result).isNull();
  }

  @Test
  void findById_shouldMapCOMPLETEDWithVulnerabilities() {
    VulnerabilityEntity vulnEntity = VulnerabilityEntity.builder()
        .cweId("CWE-89").severity(8).description("SQL Injection").remediationPatch("Use params")
        .build();
    AuditReportEntity entity = AuditReportEntity.builder()
        .scanId("scan-1").status(AuditReport.AuditStatus.COMPLETED)
        .repositoryUrl("https://github.com/amalakaky/demo.git")
        .branchName("main")
        .vulnerabilities(List.of(vulnEntity))
        .build();

    when(jpaRepository.findById("scan-1")).thenReturn(Optional.of(entity));

    AuditReport result = adapter.findById("scan-1");

    assertThat(result.getScanId()).isEqualTo("scan-1");
    assertThat(result.getRepositoryUrl()).isEqualTo("https://github.com/amalakaky/demo.git");
    assertThat(result.getBranchName()).isEqualTo("main");
    assertThat(result.getStatus()).isEqualTo(AuditReport.AuditStatus.COMPLETED);
    assertThat(result.getVulnerabilities()).hasSize(1);
    assertThat(result.getVulnerabilities().get(0).getCweId()).isEqualTo("CWE-89");
    assertThat(result.getVulnerabilities().get(0).getSeverity().value()).isEqualTo(8);
  }

  @Test
  void findById_shouldMapFAILEDWithNullVulnerabilities() {
    AuditReportEntity entity = AuditReportEntity.builder()
        .scanId("scan-2").status(AuditReport.AuditStatus.FAILED)
        .build();
    entity.setVulnerabilities(null);

    when(jpaRepository.findById("scan-2")).thenReturn(Optional.of(entity));

    AuditReport result = adapter.findById("scan-2");

    assertThat(result.getScanId()).isEqualTo("scan-2");
    assertThat(result.getStatus()).isEqualTo(AuditReport.AuditStatus.FAILED);
    assertThat(result.getVulnerabilities()).isEmpty();
  }

  @Test
  void findById_shouldMapIN_PROGRESSWithVulnerabilities() {
    VulnerabilityEntity vulnEntity = VulnerabilityEntity.builder()
        .cweId("CWE-79").severity(6).description("XSS").remediationPatch("Sanitize input")
        .build();
    AuditReportEntity entity = AuditReportEntity.builder()
        .scanId("scan-3").status(AuditReport.AuditStatus.IN_PROGRESS)
        .vulnerabilities(List.of(vulnEntity))
        .build();

    when(jpaRepository.findById("scan-3")).thenReturn(Optional.of(entity));

    AuditReport result = adapter.findById("scan-3");

    assertThat(result.getScanId()).isEqualTo("scan-3");
    assertThat(result.getStatus()).isEqualTo(AuditReport.AuditStatus.IN_PROGRESS);
    assertThat(result.getVulnerabilities()).hasSize(1);
    assertThat(result.getVulnerabilities().get(0).getCweId()).isEqualTo("CWE-79");
  }
}
