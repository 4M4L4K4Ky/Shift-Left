package com.amalakaky.aegiscode.infrastructure.adapter.in.rest;

import com.amalakaky.aegiscode.application.port.in.AnalyzeCodeUseCase;
import com.amalakaky.aegiscode.domain.model.AuditReport;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.AuditReportResponse;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.SourceCodeRequest;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController("restAuditController")
@RequestMapping("/api/v1/audit")
@Tag(name = "Aegis Security Auditor", description = "Motor de análisis estático y remediación autónoma con IA")
public class AuditController {

    private final AnalyzeCodeUseCase analyzeCodeUseCase;

    public AuditController(AnalyzeCodeUseCase analyzeCodeUseCase) {
        this.analyzeCodeUseCase = analyzeCodeUseCase;
    }

    @PostMapping("/scan")
    @Operation(summary = "Analiza código fuente en busca de vulnerabilidades y genera parches de remediación")
    public ResponseEntity<AuditReportResponse> analyzeSourceCode(@RequestBody @Valid SourceCodeRequest request) {
        String scanId = UUID.randomUUID().toString();

        int codeLength = request.sourceCode() != null ? request.sourceCode().length() : 0;
        log.info("INICIO [ScanID: {}] - Recibida petición de análisis de código. Tamaño de fuente: [{}] caracteres", scanId, codeLength);

        long startTime = System.currentTimeMillis();

        try {
            AuditReport report = analyzeCodeUseCase.executeScan(scanId, request.sourceCode());

            long duration = System.currentTimeMillis() - startTime;
            log.info("ÉXITO [ScanID: {}] - Análisis completado y persistido en {} ms", scanId, duration);

            return ResponseEntity.ok(AuditReportResponse.fromDomain(report));

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("ERROR CRÍTICO [ScanID: {}] - Fallo tras {} ms en el pipeline de IA o persistencia: {}",
                    scanId, duration, e.getMessage(), e);
            throw e;
        }
    }
}