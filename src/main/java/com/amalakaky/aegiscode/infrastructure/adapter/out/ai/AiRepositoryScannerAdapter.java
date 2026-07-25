package com.amalakaky.aegiscode.infrastructure.adapter.out.ai;

import com.amalakaky.aegiscode.application.port.out.ai.RepositoryScannerPort;
import com.amalakaky.aegiscode.domain.model.SeverityScore;
import com.amalakaky.aegiscode.domain.model.Vulnerability;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

/**
 * Adaptador de infraestructura que implementa {@link RepositoryScannerPort}
 * usando Groq (LLaMA 3.3 70B) vía Spring AI {@link ChatModel}.
 * 
 * A diferencia de {@link AiAuditorAdapter}, este adaptador escanea el código
 * completo de un repositorio y detecta todas las vulnerabilidades OWASP
 * Top 10 en una sola invocación. Procesa la respuesta JSON del LLM limpiando
 * posibles artefactos markdown antes de deserializar.
 */
@Slf4j
@Component
public class AiRepositoryScannerAdapter implements RepositoryScannerPort {

  private static final String REPO_SYSTEM_PROMPT = """
      Eres un Auditor DevSecOps experto en Java y Spring Boot.
      Analiza el codigo fuente y detecta todas las vulnerabilidades OWASP Top 10.
      Responde unicamente con un JSON valido, sin texto explicativo ni markdown.
      Estructura obligatoria:
      {
        "vulnerabilities": [
        {
          "cweId": "CWE-89",
          "severity": 9,
          "description": "Descripcion detallada",
          "remediationPatch": "Codigo corregido"
        }
        ]
      }
      """;

  private final ChatModel chatModel;
  private final ObjectMapper objectMapper;

  public AiRepositoryScannerAdapter(ChatModel chatModel, ObjectMapper objectMapper) {
    this.chatModel = chatModel;
    this.objectMapper = objectMapper;
  }

  @Override
  public List<Vulnerability> scanRepository(String sourceCodePayload) {
    log.info("=== TRACING AI SCANNER ===");
    log.info("Payload de código enviado a Groq: {} caracteres",
        sourceCodePayload.length());

    try {
      List<Message> messages = List.of(
          new SystemMessage(REPO_SYSTEM_PROMPT),
          new UserMessage(sourceCodePayload)
      );

      ChatResponse chatResponse = chatModel.call(new Prompt(messages));

      if (chatResponse == null || chatResponse.getResult() == null) {
        log.error("ERROR: ChatResponse o Result es nulo.");
        return List.of();
      }

      String rawJsonResponse = chatResponse.getResult().getOutput().getContent();

      log.info("--- INICIO RESPUESTA RAW DE GROQ ---");
      log.info("\n{}", rawJsonResponse);
      log.info("--- FIN RESPUESTA RAW DE GROQ ---");

      if (rawJsonResponse == null || rawJsonResponse.isBlank()) {
        log.warn("ADVERTENCIA: El LLM devolvió contenido vacío.");
        return List.of();
      }

      String cleanJson = rawJsonResponse
          .replaceAll("(?s)```json\\s*", "")
          .replaceAll("(?s)```\\s*", "")
          .trim();

      log.info("JSON limpio para parsear: {}", cleanJson);

      RepoAuditorResponse response = objectMapper.readValue(cleanJson, RepoAuditorResponse.class);

      if (response == null || response.vulnerabilities() == null) {
        log.warn("ADVERTENCIA: Jackson parseó el JSON pero la lista 'vulnerabilities' es nula.");
        return List.of();
      }

      log.info("ÉXITO: {} vulnerabilidades desde el LLM.",
          response.vulnerabilities().size());

      return response.vulnerabilities().stream()
          .map(v -> new Vulnerability(
              v.cweId(),
              new SeverityScore(v.severity()),
              v.description(),
              v.remediationPatch()
          ))
          .collect(Collectors.toList());

    } catch (Exception e) {
      log.error("EXCEPCIÓN CRÍTICA procesando la respuesta de Groq: {}", e.getMessage(), e);
      return List.of();
    }
  }

  /** Record interno para deserializar la respuesta JSON del LLM (lista de vulnerabilidades). */
  record RepoAuditorResponse(List<RepoVulnDto> vulnerabilities) {}

  /** Record interno con los campos de cada vulnerabilidad detectada por el LLM. */
  record RepoVulnDto(String cweId, int severity, String description, String remediationPatch) {}
}



