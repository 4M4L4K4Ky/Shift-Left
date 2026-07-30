package com.amalakaky.aegiscode.infrastructure.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    // Clave de al menos 256 bits (32 caracteres/bytes) requerida por HMAC-SHA256
    private static final String SECRET = "secretKeyThatIsAtLeast32BytesLongForHmacSha256!";
    private static final long EXPIRATION_MS = 3600000L; // 1 hora

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
    }

    @Test
    void generateTokenAndExtractClaims_shouldWorkForValidTokenWithScopes() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String username = "developer";
        String scopes = "READER,WRITER";

        String token = jwtService.generateToken(userId, username, scopes);

        assertThat(token).isNotBlank();
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractUsername(token)).isEqualTo(username);
        assertThat(jwtService.extractScopes(token)).containsExactly("READER", "WRITER");
    }

    @Test
    void generateToken_shouldHandleNullScopes() {
        String userId = "user-null-scopes";
        String username = "guest";

        String token = jwtService.generateToken(userId, username, null);

        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractUsername(token)).isEqualTo(username);

        @SuppressWarnings("unchecked")
        List<String> scopesResult = jwtService.extractScopes(token);
        assertThat(scopesResult).isEmpty();
    }

    @Test
    void generateToken_shouldHandleBlankScopes() {
        String userId = "user-blank-scopes";
        String username = "guestBlank";

        String token = jwtService.generateToken(userId, username, "   ");

        assertThat(jwtService.validateToken(token)).isTrue();

        @SuppressWarnings("unchecked")
        List<String> scopesResult = jwtService.extractScopes(token);
        assertThat(scopesResult).isEmpty();
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidOrMalformedToken() {
        assertThat(jwtService.validateToken("invalid.jwt.token")).isFalse();
        assertThat(jwtService.validateToken("")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        // Instanciamos JwtService con un tiempo de expiración negativo para generar un token ya caducado
        JwtService expiredJwtService = new JwtService(SECRET, -1000L);
        String expiredToken = expiredJwtService.generateToken("user-id", "expiredUser", "READER");

        assertThat(jwtService.validateToken(expiredToken)).isFalse();
    }
}