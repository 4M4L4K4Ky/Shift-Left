package com.amalakaky.aegiscode.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

class OpenApiConfigTest {

  @Test
  void shouldCreateOpenApiBean() {
    var config = new OpenApiConfig();
    var api = config.aegisCodeOpenApi();
    assertThat(api).isNotNull();
    assertThat(api.getInfo()).isNotNull();
    assertThat(api.getInfo().getTitle()).isEqualTo("AegisCode DevSecOps API");
    assertThat(api.getInfo().getContact()).isNotNull();
    assertThat(api.getInfo().getContact().getName()).isEqualTo("AegisCode Team");
  }
}
