package com.amalakaky.aegiscode.infrastructure.adapter.out.db;

import com.amalakaky.aegiscode.application.port.out.db.AuditRepositoryPort;
import com.amalakaky.aegiscode.domain.model.AuditReport;
import com.amalakaky.aegiscode.domain.model.SeverityScore;
import com.amalakaky.aegiscode.domain.model.Vulnerability;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity.AuditReportEntity;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity.VulnerabilityEntity;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository.SpringDataAuditRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SupabaseAuditRepositoryAdapter implements AuditRepositoryPort {

    private final SpringDataAuditRepository jpaRepository;

    public SupabaseAuditRepositoryAdapter(SpringDataAuditRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AuditReport save(AuditReport report) {
        // 1. Mapeo de Dominio a JPA (con cascada de vulnerabilidades)
        AuditReportEntity entity = AuditReportEntity.fromDomain(report);

        // 2. Persistencia en Supabase (o H2 en local)
        AuditReportEntity savedEntity = jpaRepository.save(entity);

        // 3. Retorno al caso de uso mapeando de vuelta la entidad completa con sus vulnerabilidades
        return mapToDomain(savedEntity);
    }

    @Override
    public AuditReport findById(String scanId) {
        Optional<AuditReportEntity> entityOpt = jpaRepository.findById(scanId);
        return entityOpt.map(this::mapToDomain).orElse(null);
    }

    private AuditReport mapToDomain(AuditReportEntity entity) {
        AuditReport report = new AuditReport(entity.getScanId());

        if (entity.getStatus() == AuditReport.AuditStatus.COMPLETED) {
            report.markAsCompleted();
        } else if (entity.getStatus() == AuditReport.AuditStatus.FAILED) {
            report.markAsFailed();
        }

        // Mapeo inverso: de VulnerabilityEntity (JPA) a Vulnerability (Dominio)
        if (entity.getVulnerabilities() != null) {
            for (VulnerabilityEntity vEntity : entity.getVulnerabilities()) {
                Vulnerability vulnerability = new Vulnerability(
                        vEntity.getCweId(),
                        new SeverityScore(vEntity.getSeverity()),
                        vEntity.getDescription(),
                        vEntity.getRemediationPatch()
                );
                report.addVulnerability(vulnerability);
            }
        }

        return report;
    }
}