package com.amalakaky.aegiscode.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Agregado raíz del dominio que representa una auditoría de seguridad.
 * 
 * Orquesta el ciclo de vida de un análisis: desde {@link AuditStatus#IN_PROGRESS} hasta
 * {@link AuditStatus#COMPLETED} o {@link AuditStatus#FAILED}. Contiene la lista de
 * vulnerabilidades detectadas y el contexto del repositorio escaneado.
 * 
 * Esta clase es pura del dominio (sin dependencias de frameworks) siguiendo
 * los principios de Domain-Driven Design.
 */
@Builder
@Data
@AllArgsConstructor
public class AuditReport {

  private final String scanId;
  private final List<Vulnerability> vulnerabilities;
  private AuditStatus status;
  private String repositoryUrl;
  private String branchName;

  /**
   * Estados posibles del ciclo de vida de una auditoría.
   */
  public enum AuditStatus {
    IN_PROGRESS, COMPLETED, FAILED
  }

  /**
   * Crea un nuevo reporte en estado IN_PROGRESS.
   *
   * @param scanId identificador único del escaneo (UUID)
   */
  public AuditReport(String scanId) {
    this.scanId = scanId;
    this.vulnerabilities = new ArrayList<>();
    this.status = AuditStatus.IN_PROGRESS;
  }

  /**
   * Añade una vulnerabilidad al reporte si no es nula.
   */
  public void addVulnerability(Vulnerability vulnerability) {
    if (vulnerability != null) {
      this.vulnerabilities.add(vulnerability);
    }
  }

  /** Marca la auditoría como completada exitosamente. */
  public void markAsCompleted() {
    this.status = AuditStatus.COMPLETED;
  }

  /** Marca la auditoría como fallida. */
  public void markAsFailed() {
    this.status = AuditStatus.FAILED;
  }

  /**
   * Retorna una vista inmutable de las vulnerabilidades para proteger la integridad
   * del agregado frente a modificaciones externas no controladas.
   */
  public List<Vulnerability> getVulnerabilities() {
    return Collections.unmodifiableList(this.vulnerabilities);
  }
}




