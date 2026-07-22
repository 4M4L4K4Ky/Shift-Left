package com.amalakaky.aegiscode.infrastructure.adapter.out.ai;

import com.amalakaky.aegiscode.application.port.out.ai.AuditorAgentPort;
import com.amalakaky.aegiscode.domain.model.SeverityScore;
import com.amalakaky.aegiscode.domain.model.Vulnerability;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class SpringAiAuditorAdapter implements AuditorAgentPort {

    private static final String SYSTEM_PROMPT = """
        Eres Auditor Agent, experto en ciberseguridad ofensiva.
        Analiza el siguiente código y detecta la vulnerabilidad principal.
        Devuelve estrictamente un JSON con las claves: cweId, severity (1-10), description.
        """;

    private final ChatClient chatClient;

    public SpringAiAuditorAdapter(ChatClient.Builder chatClientBuilder) {
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

record AuditorResponse(String cweId, int severity, String description) {}