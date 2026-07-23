package com.amalakaky.aegiscode.infrastructure.adapter.out.ai;

import com.amalakaky.aegiscode.application.port.out.ai.RepositoryScannerPort;
import com.amalakaky.aegiscode.domain.model.SeverityScore;
import com.amalakaky.aegiscode.domain.model.Vulnerability;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SpringAiRepositoryScannerAdapter implements RepositoryScannerPort {

    private static final String REPO_SYSTEM_PROMPT = """
        Eres un Auditor DevSecOps experto en Java y Spring Boot. Analiza el código fuente proporcionado.
        Detecta TODAS las vulnerabilidades OWASP Top 10 presentes.
        RESPONDE ÚNICAMENTE CON UN JSON VÁLIDO. No incluyas texto explicativo antes ni después, ni saludos, ni bloques de código markdown.
        Estructura obligatoria:
        {
          "vulnerabilities": [
            {
              "cweId": "CWE-89",
              "severity": 9,
              "description": "Descripción detallada",
              "remediationPatch": "Código corregido"
            }
          ]
        }
        """;

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public SpringAiRepositoryScannerAdapter(ChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Vulnerability> scanRepository(String sourceCodePayload) {
        log.info("=== TRACING AI SCANNER ===");
        log.info("Tamaño del payload de código enviado a Groq: {} caracteres", sourceCodePayload.length());

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

            // TRACE CRÍTICO: Ver exactamente qué responde Groq en la consola
            log.info("--- INICIO RESPUESTA RAW DE GROQ ---");
            log.info("\n{}", rawJsonResponse);
            log.info("--- FIN RESPUESTA RAW DE GROQ ---");

            if (rawJsonResponse == null || rawJsonResponse.isBlank()) {
                log.warn("ADVERTENCIA: El LLM devolvió contenido vacío.");
                return List.of();
            }

            // Limpieza robusta de markdown y posibles espacios
            String cleanJson = rawJsonResponse
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            log.info("JSON limpio para parsear: {}", cleanJson);

            // Deserialización con Jackson
            RepoAuditorResponse response = objectMapper.readValue(cleanJson, RepoAuditorResponse.class);

            if (response == null || response.vulnerabilities() == null) {
                log.warn("ADVERTENCIA: Jackson parseó el JSON pero la lista 'vulnerabilities' es nula.");
                return List.of();
            }

            log.info("ÉXITO: Se mapearon {} vulnerabilidades desde el LLM.", response.vulnerabilities().size());

            return response.vulnerabilities().stream()
                    .map(v -> new Vulnerability(
                            v.cweId(),
                            new SeverityScore(v.severity()),
                            v.description(),
                            v.remediationPatch()
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            // TRACE DE EXCEPCIÓN: Si falla el mapeo o hay texto basura, lo veremos con pelos y señales
            log.error("EXCEPCIÓN CRÍTICA procesando la respuesta de Groq: {}", e.getMessage(), e);
            return List.of();
        }
    }
}

record RepoAuditorResponse(List<RepoVulnDto> vulnerabilities) {}
record RepoVulnDto(String cweId, int severity, String description, String remediationPatch) {}