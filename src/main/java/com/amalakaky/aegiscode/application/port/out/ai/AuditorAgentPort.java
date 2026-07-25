package com.amalakaky.aegiscode.application.port.out.ai;

import com.amalakaky.aegiscode.domain.model.Vulnerability;
import java.util.Optional;

/**
 * Puerto de salida para el agente auditor de IA.
 * 
 * Analiza código fuente en busca de vulnerabilidades de seguridad.
 * La implementación concreta utiliza Groq (LLaMA 3.3 70B) vía Spring AI.
 */
public interface AuditorAgentPort {

  /**
   * Analiza el código fuente y retorna la vulnerabilidad principal detectada.
   *
   * @param sourceCode código fuente a auditar
   * @return Optional con la vulnerabilidad encontrada, o vacío si no se detectó ninguna
   */
  Optional<Vulnerability> analyze(String sourceCode);
}



