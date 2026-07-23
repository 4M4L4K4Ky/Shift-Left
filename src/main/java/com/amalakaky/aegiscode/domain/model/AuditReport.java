package com.amalakaky.aegiscode.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
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

    // Sin @Override. Protege la colección interna frente a modificaciones externas no controladas.
    public List<Vulnerability> getVulnerabilities() {
        return Collections.unmodifiableList(this.vulnerabilities);
    }
}
