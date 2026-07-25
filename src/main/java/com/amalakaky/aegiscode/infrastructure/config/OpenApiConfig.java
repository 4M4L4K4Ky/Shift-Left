package com.amalakaky.aegiscode.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI/Swagger para la documentación de la API.
 * <p>
 * Expone la especificación en {@code /v3/api-docs} y la interfaz Swagger UI
 * en {@code /swagger-ui.html} gracias a SpringDoc.
 */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI aegisCodeOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("AegisCode DevSecOps API")
            .description("Shift-Left Multi-Agent Architecture API")
            .version("Current")
            .contact(new Contact().name("AegisCode Team")));
  }
}



