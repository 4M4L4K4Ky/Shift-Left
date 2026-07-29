package com.amalakaky.aegiscode.application.port.in;

import com.amalakaky.aegiscode.domain.model.AuditReport;

/**
 * Puerto de entrada (inbound port) para análisis de código fuente.
 * 
 * Define los casos de uso de escaneo de seguridad: análisis de código inline
 * y análisis de repositorios completos. Implementado por {@code AnalyzeCodeUseCaseImpl}.
 */
public interface AnalyzeCodeUseCase {

  /**
   * Ejecuta el pipeline de auditoría sobre código fuente plano.
   *
   * @param scanId     identificador único del escaneo
   * @param sourceCode código fuente a analizar
   * @return reporte completo con vulnerabilidades detectadas
   */
  AuditReport executeScan(String scanId, String sourceCode);

  /**
   * Ejecuta el pipeline de auditoría sobre el código fuente de un repositorio.
   *
   * @param scanId                identificador único del escaneo
   * @param concatenatedSourceCode código fuente completo del repositorio
   * @param repositoryUrl         URL del repositorio auditado
   * @param branchName            rama auditada
   * @return reporte completo con todas las vulnerabilidades detectadas
   */
  AuditReport executeRepositoryScan(String scanId, String concatenatedSourceCode,
      String repositoryUrl, String branchName);
}



