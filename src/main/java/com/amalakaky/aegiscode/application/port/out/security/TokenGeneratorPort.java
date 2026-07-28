package com.amalakaky.aegiscode.application.port.out.security;

public interface TokenGeneratorPort {

  String generateToken(String userId, String username, String scopes);

  String extractUserId(String token);

  boolean validateToken(String token);
}
