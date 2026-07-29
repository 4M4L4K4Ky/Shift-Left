package com.amalakaky.aegiscode.infrastructure.adapter.in.rest.delegate;

import com.amalakaky.aegiscode.application.port.in.AnalyzeCodeUseCase;
import com.amalakaky.aegiscode.application.port.in.GetAuditReportUseCase;
import com.amalakaky.aegiscode.application.port.in.GetAuditStatisticsUseCase;
import com.amalakaky.aegiscode.application.port.in.GetAuditStatisticsUseCase.DashboardStats;
import com.amalakaky.aegiscode.application.port.out.vcs.GitProviderPort;
import com.amalakaky.aegiscode.domain.model.AuditReport;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.AuditEngineApiDelegate;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.AuditReportDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.AuditStatisticsResponseDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.AuditSummaryDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.CweCountDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.GitHubScanRequestDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.ScanRequestDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.VulnerabilityDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.VulnerabilitySeverityDto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementacion del delegado REST generado por OpenAPI.
 * 
 * Traduce las peticiones HTTP a llamadas a los puertos de entrada (casos de uso),
 * manteniendo la logica de presentacion fuera del dominio.
 * 
 * Endpoints:
 * 
 *   POST /api/v1/audits/inline     — audita codigo inline
 *   POST /api/v1/audits/repository  — audita repositorio Git
 *   GET  /api/v1/audits/statistics  — estadisticas de severidad
 *   GET  /api/v1/audits/report      — informe PDF global
 */
@Slf4j
@Component
public class AuditApiDelegateImpl implements AuditEngineApiDelegate {

  private final AnalyzeCodeUseCase analyzeCodeUseCase;
  private final GitProviderPort gitProviderPort;
  private final GetAuditStatisticsUseCase getAuditStatisticsUseCase;
  private final GetAuditReportUseCase getAuditReportUseCase;

  /**
   * Constructor que inyecta los casos de uso y puertos necesarios.
   */
  public AuditApiDelegateImpl(AnalyzeCodeUseCase analyzeCodeUseCase,
      GitProviderPort gitProviderPort,
      GetAuditStatisticsUseCase getAuditStatisticsUseCase,
      GetAuditReportUseCase getAuditReportUseCase) {
    this.analyzeCodeUseCase = analyzeCodeUseCase;
    this.gitProviderPort = gitProviderPort;
    this.getAuditStatisticsUseCase = getAuditStatisticsUseCase;
    this.getAuditReportUseCase = getAuditReportUseCase;
  }

  @Override
  public ResponseEntity<AuditReportDto> auditsInlinePost(ScanRequestDto scanRequestDto) {
    String scanId = UUID.randomUUID().toString();
    int codeLength = scanRequestDto.getSourceCode() != null
        ? scanRequestDto.getSourceCode().length() : 0;

    log.info("INICIO [ScanID: {}] - Código plano, {} caracteres", scanId, codeLength);

    long startTime = System.currentTimeMillis();
    try {
      AuditReport domainReport = analyzeCodeUseCase.executeScan(scanId,
          scanRequestDto.getSourceCode());

      long duration = System.currentTimeMillis() - startTime;
      log.info("ÉXITO [ScanID: {}] - Escaneo completado en {} ms", scanId, duration);

      return ResponseEntity.accepted().body(mapDomainToDto(domainReport));

    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("ERROR CRÍTICO [ScanID: {}] - Fallo tras {} ms: {}",
          scanId, duration, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public ResponseEntity<AuditReportDto> auditsRepositoryPost(
      GitHubScanRequestDto gitHubScanRequestDto) {
    String scanId = UUID.randomUUID().toString();
    String repositoryUrl = gitHubScanRequestDto.getRepositoryUrl();
    String branch = gitHubScanRequestDto.getBranch();

    log.info("INICIO [ScanID: {}] - Clonando repositorio GitHub: {} [Rama: {}]",
        scanId, repositoryUrl, branch);

    long startTime = System.currentTimeMillis();
    try {
      String codePayload = gitProviderPort.fetchSourceFiles(repositoryUrl, branch);

      log.info("JGit [ScanID: {}] - Código extraído ({} caracteres)", scanId,
          codePayload.length());

      AuditReport domainReport = analyzeCodeUseCase.executeRepositoryScan(
          scanId,
          codePayload,
          gitHubScanRequestDto.getRepositoryUrl(),
          gitHubScanRequestDto.getBranch()
      );
      long duration = System.currentTimeMillis() - startTime;
      log.info("ÉXITO [ScanID: {}] - Repositorio auditado en {} ms", scanId, duration);

      return ResponseEntity.ok(mapDomainToDto(domainReport));

    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("ERROR CRÍTICO [ScanID: {}] - Fallo en pipeline GitHub tras {} ms: {}",
          scanId, duration, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public ResponseEntity<AuditStatisticsResponseDto> auditsStatisticsGet() {
    log.info("INICIO - Consultando estadisticas globales de severidad de vulnerabilidades");

    DashboardStats stats = getAuditStatisticsUseCase.getDashboardStats();

    AuditStatisticsResponseDto responseDto = new AuditStatisticsResponseDto();
    responseDto.setTotalAudits(stats.totalAudits());
    responseDto.setTotalVulnerabilities(stats.totalVulnerabilities());

    Map<String, Long> severityCounts = stats.bySeverity().entrySet().stream()
        .collect(Collectors.toMap(
            e -> String.valueOf(e.getKey()),
            Map.Entry::getValue
        ));
    responseDto.setSeverityCounts(severityCounts);

    List<CweCountDto> byCwe = stats.byCwe().stream()
        .map(item -> new CweCountDto(item.cweId(), item.count()))
        .collect(Collectors.toList());
    responseDto.setByCwe(byCwe);

    List<AuditSummaryDto> recentAudits = stats.recentAudits().stream()
        .map(item -> {
          AuditSummaryDto dto = new AuditSummaryDto();
          dto.setScanId(item.scanId());
          dto.setDate(OffsetDateTime.of(item.date(), ZoneOffset.UTC));
          dto.setRepositoryUrl(item.repositoryUrl());
          dto.setBranchName(item.branchName());
          dto.setTotalVulnerabilities(item.totalVulnerabilities());
          return dto;
        })
        .collect(Collectors.toList());
    responseDto.setRecentAudits(recentAudits);

    log.info("EXITO - Estadisticas analiticas consultadas correctamente");
    return ResponseEntity.ok(responseDto);
  }

  @Override
  public ResponseEntity<Resource> getAuditReport() {
    log.info("INICIO - Generando informe PDF global");
    long startTime = System.currentTimeMillis();

    try {
      byte[] pdfBytes = getAuditReportUseCase.getGlobalReport();
      long duration = System.currentTimeMillis() - startTime;
      log.info("EXITO - Informe PDF generado en {} ms ({} bytes)", duration, pdfBytes.length);

      ByteArrayResource resource = new ByteArrayResource(pdfBytes);

      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_PDF)
          .contentLength(pdfBytes.length)
          .header(HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename=\"informe-global.pdf\"")
          .body(resource);

    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("ERROR CRITICO - Fallo al generar informe PDF tras {} ms: {}",
          duration, e.getMessage(), e);
      throw e;
    }
  }

  /** Convierte un {@link AuditReport} del dominio al DTO de respuesta. */
  private AuditReportDto mapDomainToDto(AuditReport domain) {
    AuditReportDto dto = new AuditReportDto();
    dto.scanId(domain.getScanId());

    if (domain.getStatus() != null) {
      dto.status(AuditReportDto.StatusEnum.valueOf(domain.getStatus().name()));
    }

    List<VulnerabilityDto> vulnDtos = domain.getVulnerabilities().stream()
          .map(v -> {
            VulnerabilityDto vulnDto = new VulnerabilityDto();
            vulnDto.cweId(v.getCweId());

            if (v.getSeverity() != null) {
              VulnerabilitySeverityDto severityDto = new VulnerabilitySeverityDto();
              severityDto.setValue(v.getSeverity().value());
              vulnDto.severity(severityDto);
            }

            vulnDto.description(v.getDescription());
            vulnDto.remediationPatch(v.getRemediationPatch());
            return vulnDto;
          })
          .toList();

    dto.vulnerabilities(vulnDtos);

    return dto;
  }
}




