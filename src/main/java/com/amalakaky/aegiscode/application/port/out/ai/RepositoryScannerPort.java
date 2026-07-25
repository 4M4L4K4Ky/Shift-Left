package com.amalakaky.aegiscode.application.port.out.ai;

import com.amalakaky.aegiscode.domain.model.Vulnerability;
import java.util.List;

/**
 * Puerto de salida para el agente scanner de repositorios.
 * <p>
 * Escanea todo el código fuente de un repositorio (múltiples archivos)
 * y retorna todas las vulnerabilidades detectadas en un solo análisis.
 * A diferencia de {@link AuditorAgentPort}, este puerto soporta detección
 * masiva de vulnerabilidades OWASP Top 10.
 */
public interface RepositoryScannerPort {

  /**
   * Escanea el código fuente del repositorio en busca de vulnerabilidades.
   *
   * @param sourceCodePayload código completo del repositorio concatenado
   * @return lista de vulnerabilidades detectadas (vacía si no hay hallazgos)
   */
  List<Vulnerability> scanRepository(String sourceCodePayload);
}



