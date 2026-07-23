package com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity;

import com.amalakaky.aegiscode.domain.model.AuditReport;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_reports")
@Builder
@Data
public class AuditReportEntity {

    @Id
    @Column(name = "scan_id", nullable = false, updatable = false)
    private String scanId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditReport.AuditStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Métodos de mapeo bidireccional con el dominio
    public static AuditReportEntity fromDomain(AuditReport report) {
        return AuditReportEntity.builder()
                .scanId(report.getScanId())
                .status(report.getStatus())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
