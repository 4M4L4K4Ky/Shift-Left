package com.amalakaky.aegiscode.infrastructure.adapter.in.rest.controller;

import com.amalakaky.aegiscode.infrastructure.config.security.JwtService;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class TokenController {

  private final JwtService jwtService;
  private final String expectedClientId;
  private final String expectedClientSecret;

  /**
   * Constructor que inyecta el servicio JWT y las credenciales esperadas.
   */
  public TokenController(JwtService jwtService,
      @Value("${app.api.client-id}") String expectedClientId,
      @Value("${app.api.client-secret}") String expectedClientSecret) {
    this.jwtService = jwtService;
    this.expectedClientId = expectedClientId;
    this.expectedClientSecret = expectedClientSecret;
  }

  /**
   * Genera un token de acceso JWT para un cliente autenticado.
   */
  @PostMapping("/token")
  public ResponseEntity<Map<String, String>> getToken(
      @RequestBody Map<String, String> body) {
    String clientId = body.get("client_id");
    String clientSecret = body.get("client_secret");

    if (clientId == null || clientSecret == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", "client_id and client_secret are required"));
    }

    if (!expectedClientId.equals(clientId) || !expectedClientSecret.equals(clientSecret)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Invalid client credentials"));
    }

    String token = jwtService.generateToken(
        UUID.randomUUID().toString(), clientId, "READER,WRITER");

    return ResponseEntity.ok(Map.of(
        "access_token", token,
        "token_type", "Bearer",
        "scopes", "READER,WRITER"
    ));
  }
}
