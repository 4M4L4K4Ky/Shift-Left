package com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity;

import com.amalakaky.aegiscode.domain.model.AuditReport;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_reports")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditReportEntity {

    @Id
    @Column(name = "scan_id", nullable = false, updatable = false)
    private String scanId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditReport.AuditStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public AuditReportEntity(String scanId, AuditReport.AuditStatus status, LocalDateTime createdAt) {
        this.scanId = scanId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static AuditReportEntity fromDomain(AuditReport report) {
        return AuditReportEntity.builder()
                .scanId(report.getScanId())
                .status(report.getStatus())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
