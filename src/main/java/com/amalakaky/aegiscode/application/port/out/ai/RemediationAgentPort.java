package com.amalakaky.aegiscode.application.port.out.ai;

/**
 * Puerto de salida para el agente de remediación de IA.
 * <p>
 * Genera código corregido para una vulnerabilidad específica dado su CWE.
 * La implementación utiliza Groq (LLaMA 3.3 70B) con un prompt especializado.
 */
public interface RemediationAgentPort {

  /**
   * Genera un parche de código limpio y seguro para la vulnerabilidad indicada.
   *
   * @param vulnerableCode código fuente original con la vulnerabilidad
   * @param cweId          identificador CWE a corregir (ej. "CWE-89")
   * @return fragmento de código corregido
   */
  String generateCleanPatch(String vulnerableCode, String cweId);
}



