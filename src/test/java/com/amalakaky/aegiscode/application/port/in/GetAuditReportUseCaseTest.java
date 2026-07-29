package com.amalakaky.aegiscode.application.port.in;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class GetAuditReportUseCaseTest {

  @Test
  void vulnerabilityDetail_shouldCreateAndAccessFields() {
        var detail = new GetAuditReportUseCase.VulnerabilityDetail(
            "CWE-89", 9, "SQL Injection", "use PreparedStatement");
    assertEquals("CWE-89", detail.cweId());
    assertEquals(9, detail.severity());
    assertEquals("SQL Injection", detail.description());
    assertEquals("use PreparedStatement", detail.remediationPatch());
  }

  @Test
  void vulnerabilityDetail_shouldHandleNullRemediation() {
    var detail = new GetAuditReportUseCase.VulnerabilityDetail("CWE-79", 5, "XSS", null);
    assertNull(detail.remediationPatch());
  }
}
