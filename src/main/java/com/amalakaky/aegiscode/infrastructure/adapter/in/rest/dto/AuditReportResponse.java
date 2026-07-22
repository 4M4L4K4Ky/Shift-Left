package com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto;

import com.amalakaky.aegiscode.domain.model.AuditReport;

import java.time.LocalDateTime;
import java.util.List;

public record AuditReportResponse(
        String scanId,
        AuditReport.AuditStatus status,
        List<VulnerabilityDto> vulnerabilities,
        LocalDateTime timestamp
) {
    public static AuditReportResponse fromDomain(AuditReport report) {
        List<VulnerabilityDto> vulnDtos = report.getVulnerabilities().stream()
                .map(v -> new VulnerabilityDto(
                        v.getCweId(),
                        v.getSeverity() != null ? String.valueOf(v.getSeverity().value()) : "UNKNOWN",
                        v.getDescription(),
                        v.getRemediationPatch()
                ))
                .toList();

        return new AuditReportResponse(
                report.getScanId(),
                report.getStatus(),
                vulnDtos,
                LocalDateTime.now()
        );
    }

    public record VulnerabilityDto(
            String cweId,
            String severity,
            String description,
            String remediationPatch
    ) {}
}