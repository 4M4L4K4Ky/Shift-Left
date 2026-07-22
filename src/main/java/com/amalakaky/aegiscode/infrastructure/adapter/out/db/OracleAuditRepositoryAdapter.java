package com.amalakaky.aegiscode.infrastructure.adapter.out.db;

import com.amalakaky.aegiscode.application.port.out.db.AuditRepositoryPort;
import com.amalakaky.aegiscode.domain.model.AuditReport;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OracleAuditRepositoryAdapter implements AuditRepositoryPort {

    private final SpringDataAuditRepository jpaRepository;

    public OracleAuditRepositoryAdapter(SpringDataAuditRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AuditReport save(AuditReport report) {
        AuditEntity entity = new AuditEntity(report.getScanId(), report.getStatus().name());
        AuditEntity savedEntity = jpaRepository.save(entity);
        return mapToDomain(savedEntity);
    }

    @Override
    public AuditReport findById(String scanId) {
        Optional<AuditEntity> entityOpt = jpaRepository.findById(scanId);
        return entityOpt.map(this::mapToDomain).orElse(null);
    }

    private AuditReport mapToDomain(AuditEntity entity) {
        AuditReport report = new AuditReport(entity.getScanId());
        if ("COMPLETED".equals(entity.getStatus())) {
            report.markAsCompleted();
        }
        if ("FAILED".equals(entity.getStatus())) {
            report.markAsFailed();
        }
        return report;
    }
}
