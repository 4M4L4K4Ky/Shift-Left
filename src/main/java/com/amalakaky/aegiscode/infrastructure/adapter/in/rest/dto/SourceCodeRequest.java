package com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para el endpoint {@code POST /api/scans}.
 * <p>
 * Contiene el código fuente a auditar con validaciones de tamaño máximo (50KB).
 */
public record SourceCodeRequest(
    @NotBlank(message = "El código fuente no puede estar vacío")
    @Size(max = 50000, message = "El tamaño máximo permitido para el análisis estático es de 50KB")
    String sourceCode
) {}



