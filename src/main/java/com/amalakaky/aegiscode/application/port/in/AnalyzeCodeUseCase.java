package com.amalakaky.aegiscode.application.port.in;

import com.amalakaky.aegiscode.domain.model.AuditReport;

public interface AnalyzeCodeUseCase {
    AuditReport executeScan(String scanId, String sourceCode);
    // Nueva firma para el escaneo masivo
    AuditReport executeRepositoryScan(String scanId, String concatenatedSourceCode);
}