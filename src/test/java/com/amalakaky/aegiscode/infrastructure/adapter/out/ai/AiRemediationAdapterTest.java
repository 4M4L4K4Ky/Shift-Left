package com.amalakaky.aegiscode.infrastructure.adapter.out.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
class AiRemediationAdapterTest {

  @Mock
  private ChatModel chatModel;

  private AiRemediationAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new AiRemediationAdapter(ChatClient.builder(chatModel));
  }

  @Test
  void shouldReturnPatchFromLlm() {
    var expectedPatch = "public class Safe { /* patched */ }";

    when(chatModel.call(any(Prompt.class)))
        .thenReturn(new ChatResponse(List.of(new Generation(expectedPatch))));

    var result = adapter.generateCleanPatch("vulnerable code", "CWE-89");

    assertThat(result).isEqualTo(expectedPatch);
  }
}
