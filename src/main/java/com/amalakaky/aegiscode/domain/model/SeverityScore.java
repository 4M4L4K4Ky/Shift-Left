package com.amalakaky.aegiscode.domain.model;

/**
 * Value Object que encapsula la puntuación de severidad de una vulnerabilidad.
 * 
 * El rango válido es de 1 a 10. Se valida en la construcción para mantener
 * invariantes del dominio. Al ser un record, la igualdad es por valor.
 */
public record SeverityScore(int value) {

  private static final int MIN_SCORE = 1;
  private static final int MAX_SCORE = 10;
  private static final String EXCEPTION_MESSAGE = "Severity must be between 1 and 10";

  public SeverityScore {
    if (value < MIN_SCORE || value > MAX_SCORE) {
      throw new IllegalArgumentException(EXCEPTION_MESSAGE);
    }
  }
}



