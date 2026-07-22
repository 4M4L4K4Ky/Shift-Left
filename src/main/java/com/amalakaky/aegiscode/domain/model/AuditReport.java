package com.amalakaky.aegiscode.domain.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class AuditReport {

    private final String scanId;
    private final List<Vulnerability> vulnerabilities;
    private AuditStatus status;

    public enum AuditStatus {
        IN_PROGRESS, COMPLETED, FAILED
    }

    public AuditReport(String scanId) {
        this.scanId = scanId;
        this.vulnerabilities = new ArrayList<>();
        this.status = AuditStatus.IN_PROGRESS;
    }

    public void addVulnerability(Vulnerability vulnerability) {
        if (vulnerability != null) {
            this.vulnerabilities.add(vulnerability);
        }
    }

    public void markAsCompleted() {
        this.status = AuditStatus.COMPLETED;
    }

    public void markAsFailed() {
        this.status = AuditStatus.FAILED;
    }

    // Sobrescribimos el getter para mantener tu protección explícita con Collections.unmodifiableList
    public List<Vulnerability> getVulnerabilities() {
        return Collections.unmodifiableList(this.vulnerabilities);
    }
}