package com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity;

import com.amalakaky.aegiscode.domain.model.AuditReport;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    // Relación OneToMany con Cascade para persistir los hijos automáticamente
// Inicialización directa inline sin requerir @Builder.Default
    @OneToMany(mappedBy = "auditReport", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<VulnerabilityEntity> vulnerabilities = new ArrayList<>();

    @Builder
    public AuditReportEntity(String scanId, AuditReport.AuditStatus status,
                             LocalDateTime createdAt, List<VulnerabilityEntity> vulnerabilities) {
        this.scanId = scanId;
        this.status = status;
        this.createdAt = createdAt;
        if (vulnerabilities != null) {
            this.vulnerabilities = vulnerabilities;
        }
    }

    public static AuditReportEntity fromDomain(AuditReport report) {
        AuditReportEntity entity = AuditReportEntity.builder()
                .scanId(report.getScanId())
                .status(report.getStatus())
                .createdAt(LocalDateTime.now())
                .build();

        if (report.getVulnerabilities() != null) {
            List<VulnerabilityEntity> vulnEntities = report.getVulnerabilities().stream()
                    .map(v -> VulnerabilityEntity.fromDomain(v, entity))
                    .collect(Collectors.toList());
            entity.setVulnerabilities(vulnEntities);
        }

        return entity;
    }
}