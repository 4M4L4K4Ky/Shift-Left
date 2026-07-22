package com.amalakaky.aegiscode.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI aegisCodeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AegisCode DevSecOps API")
                        .description("Shift-Left Multi-Agent Architecture API")
                        .version("Current")
                        .contact(new Contact().name("AegisCode Team")));
    }
}