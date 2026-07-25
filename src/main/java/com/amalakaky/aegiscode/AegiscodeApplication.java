package com.amalakaky.aegiscode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación AegisCode DevSecOps.
 * 
 * Plataforma Shift-Left con arquitectura hexagonal (Puertos y Adaptadores)
 * que utiliza agentes de IA (Groq/LLaMA 3.3 70B) para auditoría automatizada
 * de seguridad en código Java/Spring.
 */
@SpringBootApplication
public class AegiscodeApplication {

  public static void main(String[] args) {
    SpringApplication.run(AegiscodeApplication.class, args);
  }
}




