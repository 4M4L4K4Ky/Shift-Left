package com.amalakaky.aegiscode.application.port.usecase;

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

@Service
@Transactional
public class AnalyzeCodeUseCaseImpl implements AnalyzeCodeUseCase {

  private final AuditorAgentPort auditorAgent;
  private final RemediationAgentPort remediationAgent;
  private final RepositoryScannerPort repositoryScanner;
  private final AuditRepositoryPort repository;

  // Inyectamos el nuevo puerto sin romper los anteriores
  public AnalyzeCodeUseCaseImpl(AuditorAgentPort auditorAgent,
                  RemediationAgentPort remediationAgent,
                  RepositoryScannerPort repositoryScanner,
                  AuditRepositoryPort repository) {
    this.auditorAgent = auditorAgent;
    this.remediationAgent = remediationAgent;
    this.repositoryScanner = repositoryScanner;
    this.repository = repository;
  }

  // ==========================================
  // FLUJO ORIGINAL INTACTO (/api/scans)
  // ==========================================
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

    // Asignamos el contexto de origen de manera inmutable/controlada al dominio
    report.setRepositoryUrl(repositoryUrl);
    report.setBranchName(branchName);

    // El adaptador LLaMA 3 analiza todo el código extraído por JGit
    List<Vulnerability> detectedVulns = repositoryScanner.scanRepository(concatenatedSourceCode);

    // Agregamos todas las vulnerabilidades detectadas al reporte
    detectedVulns.forEach(report::addVulnerability);

    report.markAsCompleted();
    return repository.save(report);
  }
}



