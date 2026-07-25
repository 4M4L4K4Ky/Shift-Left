package com.amalakaky.aegiscode.application.port.out;

import com.amalakaky.aegiscode.application.port.in.GetAuditReportUseCase.GlobalReportData;

/**
 * Puerto de salida para la generación de informes PDF.
 * <p>
 * Define el contrato para transformar los datos globales de la plataforma
 * ({@link GlobalReportData}) en un documento PDF listo para descargar.
 */
public interface ReportGeneratorPort {

  /**
   * Genera un informe PDF con los datos globales de la plataforma.
   *
   * @param data datos completos del informe (estadísticas + auditorías recientes)
   * @return array de bytes del documento PDF
   */
  byte[] generateGlobalPdf(GlobalReportData data);
}
