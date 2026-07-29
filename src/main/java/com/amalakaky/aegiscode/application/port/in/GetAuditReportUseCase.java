package com.amalakaky.aegiscode.application.port.in;

import com.amalakaky.aegiscode.domain.model.CweCount;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface GetAuditReportUseCase {

  byte[] getGlobalReport();

  record GlobalReportData(
      long totalAudits,
      long totalVulnerabilities,
      Map<Integer, Long> bySeverity,
      List<CweCount> topCwes,
      List<AuditDetail> recentAudits
  ) {}

  record VulnerabilityDetail(String cweId, int severity, String description,
                             String remediationPatch) {}

  record AuditDetail(String scanId, LocalDateTime date, String repositoryUrl,
                     String branchName, List<VulnerabilityDetail> vulnerabilities) {}
}
