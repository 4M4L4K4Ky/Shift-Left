package com.amalakaky.aegiscode.infrastructure.adapter.out.ai;

import com.amalakaky.aegiscode.application.port.out.ai.AuditorAgentPort;
import com.amalakaky.aegiscode.domain.model.SeverityScore;
import com.amalakaky.aegiscode.domain.model.Vulnerability;
import java.util.Optional;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * Adaptador de infraestructura que implementa {@link AuditorAgentPort} usando
 * Spring AI con Groq (LLaMA 3.3 70B).
 * 
 * Envía el código fuente al modelo con un prompt de "Auditor Agent" y mapea
 * la respuesta JSON estructurada a una entidad {@link Vulnerability} del dominio.
 * Detecta una única vulnerabilidad por invocación (la principal).
 */
@Component
public class AiAuditorAdapter implements AuditorAgentPort {

  private static final String SYSTEM_PROMPT = """
      Eres Auditor Agent, experto en ciberseguridad ofensiva.
      Analiza el siguiente codigo y detecta la vulnerabilidad principal.
      Devuelve un JSON con las claves: cweId, severity (1-10), description.
      """;

  private final ChatClient chatClient;

  public AiAuditorAdapter(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();
  }

  @Override
  public Optional<Vulnerability> analyze(String sourceCode) {
    AuditorResponse response = chatClient.prompt()
        .user(sourceCode)
        .call()
        .entity(AuditorResponse.class);

    return mapToDomain(response);
  }

  /** Record interno para deserializar la respuesta JSON del LLM. */
  record AuditorResponse(String cweId, int severity, String description) {}

  private Optional<Vulnerability> mapToDomain(AuditorResponse response) {
    if (response == null || response.cweId() == null) {
      return Optional.empty();
    }

    return Optional.of(new Vulnerability(
        response.cweId(),
        new SeverityScore(response.severity()),
        response.description(),
        null
    ));
  }
}



