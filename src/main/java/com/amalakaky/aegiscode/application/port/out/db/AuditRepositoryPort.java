package com.amalakaky.aegiscode.application.port.out.db;

import com.amalakaky.aegiscode.domain.model.AuditReport;

/**
 * Puerto de salida para la persistencia de auditorías.
 * <p>
 * Define el contrato para guardar y recuperar reportes de auditoría
 * desde el sistema de almacenamiento (Oracle ATP en producción, H2 en local).
 */
public interface AuditRepositoryPort {

  /**
   * Persiste un reporte de auditoría completo con sus vulnerabilidades.
   *
   * @param report reporte del dominio a guardar
   * @return reporte persistido con datos actualizados
   */
  AuditReport save(AuditReport report);

  /**
   * Busca un reporte por su identificador único.
   *
   * @param scanId identificador del escaneo
   * @return reporte encontrado o null si no existe
   */
  AuditReport findById(String scanId);
}



