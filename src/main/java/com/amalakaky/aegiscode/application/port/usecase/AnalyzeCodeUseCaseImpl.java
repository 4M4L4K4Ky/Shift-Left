package com.amalakaky.aegiscode.application.port.usecase;

import com.amalakaky.aegiscode.application.port.in.AnalyzeCodeUseCase;
import com.amalakaky.aegiscode.application.port.out.ai.AuditorAgentPort;
import com.amalakaky.aegiscode.application.port.out.ai.RemediationAgentPort;
import com.amalakaky.aegiscode.application.port.out.db.AuditRepositoryPort;
import com.amalakaky.aegiscode.domain.model.AuditReport;
import com.amalakaky.aegiscode.domain.model.Vulnerability;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AnalyzeCodeUseCaseImpl implements AnalyzeCodeUseCase {

    private final AuditorAgentPort auditorAgent;
    private final RemediationAgentPort remediationAgent;
    private final AuditRepositoryPort repository;

    public AnalyzeCodeUseCaseImpl(AuditorAgentPort auditorAgent,
                                  RemediationAgentPort remediationAgent,
                                  AuditRepositoryPort repository) {
        this.auditorAgent = auditorAgent;
        this.remediationAgent = remediationAgent;
        this.repository = repository;
    }

    @Override
    public AuditReport executeScan(String scanId, String sourceCode) {
        AuditReport report = new AuditReport(scanId);

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
}
