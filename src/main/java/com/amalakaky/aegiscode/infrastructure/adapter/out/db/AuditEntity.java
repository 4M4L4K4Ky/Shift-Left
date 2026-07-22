package com.amalakaky.aegiscode.infrastructure.adapter.out.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_reports")
public class AuditEntity {

    @Id
    @Column(name = "scan_id")
    private String scanId;

    @Column(nullable = false)
    private String status;

    public AuditEntity() {
    }

    public AuditEntity(String scanId, String status) {
        this.scanId = scanId;
        this.status = status;
    }

    public String getScanId() {
        return scanId;
    }

    public String getStatus() {
        return status;
    }
}