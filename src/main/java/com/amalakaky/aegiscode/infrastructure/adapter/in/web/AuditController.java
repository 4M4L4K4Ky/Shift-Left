package com.amalakaky.aegiscode.infrastructure.adapter.in.web;

import com.amalakaky.aegiscode.application.port.in.AnalyzeCodeUseCase;
import com.amalakaky.aegiscode.domain.model.AuditReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/scans")
@Tag(name = "Audit", description = "Endpoints para el motor de análisis DevSecOps")
public class AuditController {

    private final AnalyzeCodeUseCase useCase;

    public AuditController(AnalyzeCodeUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @Operation(summary = "Ejecuta un escaneo estático", description = "Lanza el pipeline de análisis multi-agente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Escaneo aceptado y en proceso"),
            @ApiResponse(responseCode = "429", description = "Rate limit excedido")
    })
    public ResponseEntity<AuditReport> scanCode(@RequestBody ScanRequest request) {
        String scanId = UUID.randomUUID().toString();
        AuditReport report = useCase.executeScan(scanId, request.sourceCode());
        return ResponseEntity.accepted().body(report);
    }
}

record ScanRequest(String sourceCode) {}
