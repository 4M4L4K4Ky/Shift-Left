package com.amalakaky.aegiscode.infrastructure.adapter.out.ai;

import com.amalakaky.aegiscode.application.port.out.ai.RemediationAgentPort;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class SpringAiRemediationAdapter implements RemediationAgentPort {

    private final ChatClient chatClient;

    public SpringAiRemediationAdapter(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("Eres un arquitecto de software experto en refactorización segura y mitigación de vulnerabilidades. " +
                        "Devuelve únicamente el fragmento de código corregido y limpio, aplicando parches seguros frente al CWE indicado.")
                .build();
    }

    @Override
    public String generateCleanPatch(String vulnerableCode, String cweId) {
        return chatClient.prompt()
                .user("Corrige la vulnerabilidad " + cweId + " en el siguiente código:\n\n" + vulnerableCode)
                .call()
                .content();
    }
}