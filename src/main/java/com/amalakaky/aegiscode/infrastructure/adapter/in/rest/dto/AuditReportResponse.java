package com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto;

import com.amalakaky.aegiscode.domain.model.AuditReport;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para los resultados de una auditoría.
 * 
 * Proyecta los datos del {@link AuditReport} del dominio hacia la capa REST,
 * excluyendo detalles internos como metadatos de repositorio.
 */
public record AuditReportResponse(
    String scanId,
    AuditReport.AuditStatus status,
    List<VulnerabilityDto> vulnerabilities,
    LocalDateTime timestamp
) {
  /** Convierte un reporte del dominio a este DTO de respuesta. */
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

  /** DTO anidado que representa una vulnerabilidad individual en la respuesta. */
  public record VulnerabilityDto(
      String cweId,
      String severity,
      String description,
      String remediationPatch
  ) {}
}



