package com.amalakaky.aegiscode.infrastructure.adapter.out.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.amalakaky.aegiscode.domain.model.Vulnerability;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

@ExtendWith(MockitoExtension.class)
class AiAuditorAdapterTest {

  @Mock
  private ChatModel chatModel;

  @Test
  void analyze_shouldReturnVulnerabilityWhenValidResponse() {
    var auditorJson = """
        {"cweId":"CWE-89","severity":8,"description":"SQL Injection"}
        """;
    var generation = new Generation(auditorJson);
    var chatResponse = new ChatResponse(List.of(generation));
    when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

    var adapter = new AiAuditorAdapter(ChatClient.builder(chatModel));
    var result = adapter.analyze("source code");

    assertThat(result).isPresent();
    assertThat(result.get().getCweId()).isEqualTo("CWE-89");
    assertThat(result.get().getSeverity().value()).isEqualTo(8);
    assertThat(result.get().getDescription()).isEqualTo("SQL Injection");
    assertThat(result.get().getRemediationPatch()).isEqualTo("Pending architectural review");
  }

  @SneakyThrows
  @Test
  void mapToDomain_shouldReturnVulnerabilityWhenResponseIsValid() {
    var response = new AiAuditorAdapter.AuditorResponse("CWE-89", 8, "SQL Injection");
    var result = invokeMapToDomain(response);

    assertThat(result).isPresent();
    assertThat(result.get().getCweId()).isEqualTo("CWE-89");
    assertThat(result.get().getSeverity().value()).isEqualTo(8);
    assertThat(result.get().getDescription()).isEqualTo("SQL Injection");
  }

  @SneakyThrows
  @Test
  void mapToDomain_shouldReturnEmptyWhenCweIdIsNull() {
    var result = invokeMapToDomain(new AiAuditorAdapter.AuditorResponse(null, 5, "desc"));
    assertThat(result).isEmpty();
  }

  @SneakyThrows
  @Test
  void mapToDomain_shouldReturnEmptyWhenResponseIsNull() {
    var result = invokeMapToDomain(null);
    assertThat(result).isEmpty();
  }

  @SneakyThrows
  @Test
  void mapToDomain_shouldDefaultRemediationToNull() {
    var result = invokeMapToDomain(new AiAuditorAdapter.AuditorResponse("CWE-79", 5, "XSS"));
    assertThat(result).isPresent();
    assertThat(result.get().getRemediationPatch()).isEqualTo("Pending architectural review");
  }

  @SuppressWarnings("unchecked")
  @SneakyThrows
  private Optional<Vulnerability> invokeMapToDomain(AiAuditorAdapter.AuditorResponse response) {
    var adapter = new AiAuditorAdapter(ChatClient.builder(chatModel));
    var method = AiAuditorAdapter.class.getDeclaredMethod(
        "mapToDomain", AiAuditorAdapter.AuditorResponse.class);
    method.setAccessible(true);
    return (Optional<Vulnerability>) method.invoke(adapter, response);
  }
}
