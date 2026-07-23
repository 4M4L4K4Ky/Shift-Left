package com.amalakaky.aegiscode.infrastructure.adapter.in.rest.delegate;

import com.amalakaky.aegiscode.application.port.in.AnalyzeCodeUseCase;
import com.amalakaky.aegiscode.application.port.out.vcs.GitProviderPort;
import com.amalakaky.aegiscode.domain.model.AuditReport;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.AuditEngineApiDelegate;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.AuditReportDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.GitHubScanRequestDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.ScanRequestDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.VulnerabilityDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AuditApiDelegateImpl implements AuditEngineApiDelegate {

    private final AnalyzeCodeUseCase analyzeCodeUseCase;
    private final GitProviderPort gitProviderPort;

    public AuditApiDelegateImpl(AnalyzeCodeUseCase analyzeCodeUseCase, GitProviderPort gitProviderPort) {
        this.analyzeCodeUseCase = analyzeCodeUseCase;
        this.gitProviderPort = gitProviderPort;
    }

    @Override
    public ResponseEntity<AuditReportDto> scansPost(ScanRequestDto scanRequestDto) {
        String scanId = UUID.randomUUID().toString();
        int codeLength = scanRequestDto.getSourceCode() != null ? scanRequestDto.getSourceCode().length() : 0;

        log.info("INICIO [ScanID: {}] - Análisis de código plano. Tamaño: [{}] caracteres", scanId, codeLength);

        long startTime = System.currentTimeMillis();
        try {
            AuditReport domainReport = analyzeCodeUseCase.executeScan(scanId, scanRequestDto.getSourceCode());

            long duration = System.currentTimeMillis() - startTime;
            log.info("ÉXITO [ScanID: {}] - Escaneo completado en {} ms", scanId, duration);

            return ResponseEntity.accepted().body(mapDomainToDto(domainReport));

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("ERROR CRÍTICO [ScanID: {}] - Fallo tras {} ms: {}", scanId, duration, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<AuditReportDto> auditGithubPost(GitHubScanRequestDto gitHubScanRequestDto) {
        String scanId = UUID.randomUUID().toString();
        log.info("INICIO [ScanID: {}] - Clonando repositorio GitHub: {} [Rama: {}]",
                scanId, gitHubScanRequestDto.getRepositoryUrl(), gitHubScanRequestDto.getBranch());

        long startTime = System.currentTimeMillis();
        try {
            List<File> sourceFiles = gitProviderPort.fetchSourceFiles(
                    gitHubScanRequestDto.getRepositoryUrl(),
                    gitHubScanRequestDto.getBranch()
            );

            log.info("JGit [ScanID: {}] - Extraídos {} ficheros .java para auditoría estática", scanId, sourceFiles.size());

            AuditReport domainReport = analyzeCodeUseCase.executeScan(
                    scanId,
                    "// Repositorio clonado: " + sourceFiles.size() + " ficheros fuente analizados."
            );

            long duration = System.currentTimeMillis() - startTime;
            log.info("ÉXITO [ScanID: {}] - Auditoría de repositorio completada en {} ms", scanId, duration);

            return ResponseEntity.ok(mapDomainToDto(domainReport));

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("ERROR CRÍTICO [ScanID: {}] - Fallo en pipeline GitHub tras {} ms: {}", scanId, duration, e.getMessage(), e);
            throw e;
        }
    }

    private AuditReportDto mapDomainToDto(AuditReport report) {
        AuditReportDto dto = new AuditReportDto();
        dto.setScanId(report.getScanId());
        dto.setStatus(AuditReportDto.StatusEnum.valueOf(report.getStatus().name()));
        dto.setVulnerabilities(report.getVulnerabilities().stream().map(v -> {
            VulnerabilityDto vDto = new VulnerabilityDto();
            vDto.setCweId(v.getCweId());
            if (v.getSeverity() != null) {
                com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.VulnerabilitySeverityDto severityDto =
                        new com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.VulnerabilitySeverityDto();
                severityDto.setValue(v.getSeverity().value());
                vDto.setSeverity(severityDto);
            }
            vDto.setDescription(v.getDescription());
            vDto.setRemediationPatch(v.getRemediationPatch());
            return vDto;
        }).collect(Collectors.toList()));
        return dto;
    }
}
