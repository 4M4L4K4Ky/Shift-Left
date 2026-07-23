package com.amalakaky.aegiscode.infrastructure.adapter.in.rest.delegate;

import com.amalakaky.aegiscode.application.port.in.AnalyzeCodeUseCase;
import com.amalakaky.aegiscode.application.port.in.GetAuditStatisticsUseCase;
import com.amalakaky.aegiscode.application.port.out.vcs.GitProviderPort;
import com.amalakaky.aegiscode.domain.model.AuditReport;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.AuditEngineApiDelegate;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AuditApiDelegateImpl implements AuditEngineApiDelegate {

    private final AnalyzeCodeUseCase analyzeCodeUseCase;
    private final GitProviderPort gitProviderPort;
    private final GetAuditStatisticsUseCase getAuditStatisticsUseCase;

    public AuditApiDelegateImpl(AnalyzeCodeUseCase analyzeCodeUseCase,
                                GitProviderPort gitProviderPort,
                                GetAuditStatisticsUseCase getAuditStatisticsUseCase) {
        this.analyzeCodeUseCase = analyzeCodeUseCase;
        this.gitProviderPort = gitProviderPort;
        this.getAuditStatisticsUseCase = getAuditStatisticsUseCase;
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
    public ResponseEntity auditGithubPost(GitHubScanRequestDto gitHubScanRequestDto) {
        String scanId = UUID.randomUUID().toString();
        String repositoryUrl = gitHubScanRequestDto.getRepositoryUrl();
        String branch = gitHubScanRequestDto.getBranch();

        log.info("INICIO [ScanID: {}] - Clonando repositorio GitHub: {} [Rama: {}]",
                scanId, repositoryUrl, branch);

        long startTime = System.currentTimeMillis();
        try {
            List<File> sourceFiles = gitProviderPort.fetchSourceFiles(repositoryUrl, branch);

            log.info("JGit [ScanID: {}] - Extraídos {} ficheros .java para auditoría estática", scanId, sourceFiles.size());

            StringBuilder codePayload = new StringBuilder();
            for (File file : sourceFiles) {
                try {
                    codePayload.append("--- Archivo: ").append(file.getName()).append(" ---\n");
                    codePayload.append(Files.readString(file.toPath(), StandardCharsets.UTF_8)).append("\n\n");
                } catch (java.io.IOException e) {
                    log.warn("No se pudo leer el archivo {}", file.getName());
                }
            }

            AuditReport domainReport = analyzeCodeUseCase.executeRepositoryScan(
                    scanId,
                    codePayload.toString(),
                    gitHubScanRequestDto.getRepositoryUrl(),
                    gitHubScanRequestDto.getBranch()
            );
            // Si tu caso de uso no los recibe, los asignamos aquí antes de mapear/persistir:
            domainReport.setRepositoryUrl(repositoryUrl);
            domainReport.setBranchName(branch);

            long duration = System.currentTimeMillis() - startTime;
            log.info("ÉXITO [ScanID: {}] - Auditoría de repositorio completada en {} ms", scanId, duration);

            return ResponseEntity.ok(mapDomainToDto(domainReport));

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("ERROR CRÍTICO [ScanID: {}] - Fallo en pipeline GitHub tras {} ms: {}", scanId, duration, e.getMessage(), e);
            throw e;
        }
    }

    //Y para garantizar que sourceFiles no venga vacío, en tu JGitAdapter.java,
    // asegúrate de que tu método extractJavaFiles sea exactamente este:

    private List extractJavaFiles(File directory) {
        try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(directory.toPath())) {
            return paths
                    .filter(java.nio.file.Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(java.nio.file.Path::toFile)
                    .toList();
        } catch (java.io.IOException e) {
            log.error("Fallo de I/O al recorrer el árbol del repositorio en {}: {}", directory.getAbsolutePath(), e.getMessage());
            throw new IllegalStateException("Error al extraer archivos Java", e);
        }
    }


    @Override
    public ResponseEntity<AuditStatisticsResponseDto> auditsStatsGet() {
        log.info("INICIO - Consultando estadísticas globales de severidad de vulnerabilidades");

        Map<Integer, Long> severityStats = getAuditStatisticsUseCase.getSeverityStatistics();

        // Mapeo limpio al DTO generado por OpenAPI
        AuditStatisticsResponseDto responseDto = new AuditStatisticsResponseDto();
        // (Aquí asignas el mapa o la estructura que defina tu yaml de OpenAPI)

        log.info("ÉXITO - Estadísticas analíticas consultadas correctamente");
        return ResponseEntity.ok(responseDto);
    }

    private AuditReportDto mapDomainToDto(AuditReport domain) {
        AuditReportDto dto = new AuditReportDto();
        dto.scanId(domain.getScanId());

        if (domain.getStatus() != null) {
            dto.status(AuditReportDto.StatusEnum.valueOf(domain.getStatus().name()));
        }

        if (domain.getVulnerabilities() != null) {
            List<VulnerabilityDto> vulnDtos = domain.getVulnerabilities().stream()
                    .map(v -> {
                        VulnerabilityDto vDto = new VulnerabilityDto();
                        vDto.cweId(v.getCweId());

                        if (v.getSeverity() != null) {
                            VulnerabilitySeverityDto severityDto = new VulnerabilitySeverityDto();
                            severityDto.setValue(v.getSeverity().value());
                            vDto.severity(severityDto);
                        }

                        vDto.description(v.getDescription());
                        vDto.remediationPatch(v.getRemediationPatch());
                        return vDto;
                    })
                    .toList();

            dto.vulnerabilities(vulnDtos);
        }

        return dto;
    }
}
