package com.amalakaky.aegiscode.application.usecase;

import com.amalakaky.aegiscode.application.port.in.AnalyzeCodeUseCase;
import com.amalakaky.aegiscode.application.port.out.ai.AuditorAgentPort;
import com.amalakaky.aegiscode.application.port.out.ai.RemediationAgentPort;
import com.amalakaky.aegiscode.application.port.out.ai.RepositoryScannerPort;
import com.amalakaky.aegiscode.application.port.out.db.AuditRepositoryPort;
import com.amalakaky.aegiscode.domain.model.AuditReport;
import com.amalakaky.aegiscode.domain.model.Vulnerability;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso principal que orquesta los pipelines de auditoria de codigo.
 * 
 * Implementa dos flujos:
 * 
 *   Scan inline (POST /api/scans): agente Auditor → agente Remediation
 *   Scan repositorio (POST /api/v1/audit/github): agente Scanner → todas las vulnerabilidades
 * 
 * Actua como orquestador entre los puertos de salida (IA y persistencia).
 */
@Service
@Transactional
public class AnalyzeCodeUseCaseImpl implements AnalyzeCodeUseCase {

  private final AuditorAgentPort auditorAgent;
  private final RemediationAgentPort remediationAgent;
  private final RepositoryScannerPort repositoryScanner;
  private final AuditRepositoryPort repository;

  public AnalyzeCodeUseCaseImpl(AuditorAgentPort auditorAgent,
                  RemediationAgentPort remediationAgent,
                  RepositoryScannerPort repositoryScanner,
                  AuditRepositoryPort repository) {
    this.auditorAgent = auditorAgent;
    this.remediationAgent = remediationAgent;
    this.repositoryScanner = repositoryScanner;
    this.repository = repository;
  }

  @Override
  public AuditReport executeScan(String scanId, String sourceCode) {
    AuditReport report = new AuditReport(scanId);
    report.setRepositoryUrl("INLINE_SOURCE_SNIPPET");
    report.setBranchName("DIRECT_PAYLOAD");

    Optional<Vulnerability> detectedVulnerability = auditorAgent.analyze(sourceCode);

    detectedVulnerability.ifPresent(vuln -> applyRemediation(sourceCode, vuln, report));

    report.markAsCompleted();
    return repository.save(report);
  }

  /**
   * Aplica el agente de remediation a una vulnerabilidad detectada y la agrega al reporte.
   */
  private void applyRemediation(String sourceCode, Vulnerability vuln, AuditReport report) {
    String patch = remediationAgent.generateCleanPatch(sourceCode, vuln.getCweId());

    Vulnerability mitigatedVuln = new Vulnerability(
        vuln.getCweId(),
        vuln.getSeverity(),
        vuln.getDescription(),
        patch
    );

    report.addVulnerability(mitigatedVuln);
  }

  @Override
  public AuditReport executeRepositoryScan(String scanId, String concatenatedSourceCode,
      String repositoryUrl, String branchName) {
    AuditReport report = new AuditReport(scanId);
    report.setRepositoryUrl(repositoryUrl);
    report.setBranchName(branchName);

    List<Vulnerability> detectedVulns = repositoryScanner.scanRepository(concatenatedSourceCode);
    detectedVulns.forEach(report::addVulnerability);

    report.markAsCompleted();
    return repository.save(report);
  }
}
