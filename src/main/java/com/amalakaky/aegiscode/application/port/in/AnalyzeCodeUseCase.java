package com.amalakaky.aegiscode.application.port.in;

import com.amalakaky.aegiscode.domain.model.AuditReport;

public interface AnalyzeCodeUseCase {
    AuditReport executeScan(String scanId, String sourceCode);
    AuditReport executeRepositoryScan(String scanId, String concatenatedSourceCode, String repositoryUrl, String branchName);
}