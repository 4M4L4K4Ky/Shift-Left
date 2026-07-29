package com.amalakaky.aegiscode.application.port.in;

import com.amalakaky.aegiscode.domain.model.CweCount;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface GetAuditStatisticsUseCase {

  DashboardStats getDashboardStats();

  record DashboardStats(
      long totalAudits,
      long totalVulnerabilities,
      Map<Integer, Long> bySeverity,
      List<CweCount> byCwe,
      List<RecentAuditItem> recentAudits
  ) {}

  record RecentAuditItem(String scanId, LocalDateTime date, String repositoryUrl,
                         String branchName, long totalVulnerabilities) {}
}



