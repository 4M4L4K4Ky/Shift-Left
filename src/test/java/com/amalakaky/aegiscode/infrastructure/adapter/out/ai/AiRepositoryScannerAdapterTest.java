package com.amalakaky.aegiscode.infrastructure.adapter.out.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

@ExtendWith(MockitoExtension.class)
class AiRepositoryScannerAdapterTest {

  @Mock
  private ChatModel chatModel;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldReturnVulnerabilitiesWhenValidJsonResponse() {
    var jsonResponse = """
        {
          "vulnerabilities": [
            {"cweId": "CWE-89", "severity": 9, "description": "SQLi", "remediationPatch": "fix1"},
            {"cweId": "CWE-79", "severity": 5, "description": "XSS", "remediationPatch": "fix2"}
          ]
        }
        """;

    var generation = new Generation(jsonResponse);
    var chatResponse = new ChatResponse(List.of(generation));
    when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

    var adapter = new AiRepositoryScannerAdapter(chatModel, objectMapper);
    var result = adapter.scanRepository("source code");

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getCweId()).isEqualTo("CWE-89");
    assertThat(result.get(0).getSeverity().value()).isEqualTo(9);
    assertThat(result.get(1).getCweId()).isEqualTo("CWE-79");
  }

  @Test
  void shouldReturnEmptyWhenResponseIsNull() {
    when(chatModel.call(any(Prompt.class))).thenReturn(null);

    var adapter = new AiRepositoryScannerAdapter(chatModel, objectMapper);
    var result = adapter.scanRepository("code");

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyWhenResponseResultIsNull() {
    var chatResponse = new ChatResponse(List.of());
    when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

    var adapter = new AiRepositoryScannerAdapter(chatModel, objectMapper);
    var result = adapter.scanRepository("code");

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyWhenContentIsNull() {
    var generation = new Generation((String) null);
    var chatResponse = new ChatResponse(List.of(generation));
    when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

    var adapter = new AiRepositoryScannerAdapter(chatModel, objectMapper);
    var result = adapter.scanRepository("code");

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyWhenResponseIsEmptyString() {
    var generation = new Generation("");
    var chatResponse = new ChatResponse(List.of(generation));
    when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

    var adapter = new AiRepositoryScannerAdapter(chatModel, objectMapper);
    var result = adapter.scanRepository("code");

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyWhenVulnerabilitiesFieldIsNull() {
    var jsonResponse = "{\"vulnerabilities\": null}";
    var generation = new Generation(jsonResponse);
    var chatResponse = new ChatResponse(List.of(generation));
    when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

    var adapter = new AiRepositoryScannerAdapter(chatModel, objectMapper);
    var result = adapter.scanRepository("code");

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyOnJsonParseError() {
    var jsonResponse = "{\"invalid\": json}";
    var generation = new Generation(jsonResponse);
    var chatResponse = new ChatResponse(List.of(generation));
    when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

    var adapter = new AiRepositoryScannerAdapter(chatModel, objectMapper);
    var result = adapter.scanRepository("code");

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyWhenVulnerabilitiesFieldIsMissing() {
    var jsonResponse = "{}";
    var generation = new Generation(jsonResponse);
    var chatResponse = new ChatResponse(List.of(generation));
    when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

    var adapter = new AiRepositoryScannerAdapter(chatModel, objectMapper);
    var result = adapter.scanRepository("code");

    assertThat(result).isEmpty();
  }

  @Test
  void shouldHandleJsonWithMarkdownFences() {
    var jsonResponse = "```json\n{\"vulnerabilities\": [{\"cweId\": \"CWE-22\", \"severity\": 7, \"description\": \"Path traversal\", \"remediationPatch\": \"fix\"}]}\n```";

    var generation = new Generation(jsonResponse);
    var chatResponse = new ChatResponse(List.of(generation));
    when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

    var adapter = new AiRepositoryScannerAdapter(chatModel, objectMapper);
    var result = adapter.scanRepository("code");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getCweId()).isEqualTo("CWE-22");
  }

    @Test
    void shouldReturnEmptyWhenJsonIsNullLiteral() {
        var generation = new Generation("null");
        var chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        var adapter = new AiRepositoryScannerAdapter(chatModel, objectMapper);
        var result = adapter.scanRepository("code");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyOnException() {
    when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("API error"));

    var adapter = new AiRepositoryScannerAdapter(chatModel, objectMapper);
    var result = adapter.scanRepository("code");

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyWhenChatResponseResultIsNull() {
    when(chatModel.call(any(Prompt.class)))
        .thenReturn(new ChatResponse(List.of()));

    var adapter = new AiRepositoryScannerAdapter(chatModel, objectMapper);
    var result = adapter.scanRepository("code");

    assertThat(result).isEmpty();
  }

}
