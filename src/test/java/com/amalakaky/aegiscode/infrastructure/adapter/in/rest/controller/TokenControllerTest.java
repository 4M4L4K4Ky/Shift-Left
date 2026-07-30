package com.amalakaky.aegiscode.infrastructure.adapter.in.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amalakaky.aegiscode.infrastructure.config.security.JwtService;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TokenControllerTest {

    @Mock
    private JwtService jwtService;

    private TokenController tokenController;

    private static final String CLIENT_ID = "valid-client-id";
    private static final String CLIENT_SECRET = "valid-client-secret";

    @BeforeEach
    void setUp() {
        tokenController = new TokenController(jwtService, CLIENT_ID, CLIENT_SECRET);
    }

    @Test
    void getToken_shouldReturnBadRequestWhenClientIdIsNull() {
        // Given: client_id no enviado (null)
        Map<String, String> body = Map.of("client_secret", CLIENT_SECRET);

        // When
        ResponseEntity<Map<String, String>> response = tokenController.getToken(body);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "client_id and client_secret are required");
    }

    @Test
    void getToken_shouldReturnBadRequestWhenClientSecretIsNull() {
        // Given: client_secret no enviado (null)
        Map<String, String> body = Map.of("client_id", CLIENT_ID);

        // When
        ResponseEntity<Map<String, String>> response = tokenController.getToken(body);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "client_id and client_secret are required");
    }

    @Test
    void getToken_shouldReturnBadRequestWhenBothCredentialsAreNull() {
        // Given: cuerpo vacío o con valores nulos explícitos
        Map<String, String> body = new HashMap<>();
        body.put("client_id", null);
        body.put("client_secret", null);

        // When
        ResponseEntity<Map<String, String>> response = tokenController.getToken(body);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "client_id and client_secret are required");
    }

    @Test
    void getToken_shouldReturnUnauthorizedWhenClientIdIsInvalid() {
        // Given: client_id incorrecto
        Map<String, String> body = Map.of(
                "client_id", "invalid-id",
                "client_secret", CLIENT_SECRET
        );

        // When
        ResponseEntity<Map<String, String>> response = tokenController.getToken(body);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody())
                .containsEntry("error", "Invalid client credentials");
    }

    @Test
    void getToken_shouldReturnUnauthorizedWhenClientSecretIsInvalid() {
        // Given: client_secret incorrecto
        Map<String, String> body = Map.of(
                "client_id", CLIENT_ID,
                "client_secret", "invalid-secret"
        );

        // When
        ResponseEntity<Map<String, String>> response = tokenController.getToken(body);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody())
                .containsEntry("error", "Invalid client credentials");
    }

    @Test
    void getToken_shouldReturnTokenWhenCredentialsAreValid() {
        // Given: credenciales válidas y mock del generador de token JWT
        Map<String, String> body = Map.of(
                "client_id", CLIENT_ID,
                "client_secret", CLIENT_SECRET
        );

        String mockJwtToken = "mocked.jwt.token";
        when(jwtService.generateToken(anyString(), eq(CLIENT_ID), eq("READER,WRITER")))
                .thenReturn(mockJwtToken);

        // When
        ResponseEntity<Map<String, String>> response = tokenController.getToken(body);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsEntry("access_token", mockJwtToken)
                .containsEntry("token_type", "Bearer")
                .containsEntry("scopes", "READER,WRITER");

        verify(jwtService).generateToken(anyString(), eq(CLIENT_ID), eq("READER,WRITER"));
    }
}