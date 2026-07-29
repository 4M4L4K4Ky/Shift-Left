package com.amalakaky.aegiscode.infrastructure.config.security;

import com.amalakaky.aegiscode.application.port.out.security.TokenGeneratorPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService implements TokenGeneratorPort {

  private final SecretKey key;
  private final long expirationMs;

  public JwtService(@Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration:86400000}") long expirationMs) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
    this.expirationMs = expirationMs;
  }

  @Override
  public String generateToken(String userId, String username, String scopes) {
    Date now = new Date();
    List<String> scopeList = scopes != null && !scopes.isBlank()
        ? Arrays.asList(scopes.split(","))
        : List.of();
    return Jwts.builder()
        .subject(userId)
        .claim("username", username)
        .claim("scopes", scopeList)
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expirationMs))
        .signWith(key)
        .compact();
  }

  @Override
  public String extractUserId(String token) {
    return getClaims(token).getSubject();
  }

  @Override
  public boolean validateToken(String token) {
    try {
      getClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String extractUsername(String token) {
    return getClaims(token).get("username", String.class);
  }

  @SuppressWarnings("unchecked")
  public List<String> extractScopes(String token) {
    return getClaims(token).get("scopes", List.class);
  }

  private Claims getClaims(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
