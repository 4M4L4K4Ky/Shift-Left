package com.amalakaky.aegiscode.application.usecase;

import com.amalakaky.aegiscode.application.port.in.AuthPort;
import com.amalakaky.aegiscode.application.port.out.db.UserRepositoryPort;
import com.amalakaky.aegiscode.application.port.out.security.TokenGeneratorPort;
import com.amalakaky.aegiscode.domain.model.JwtToken;
import com.amalakaky.aegiscode.domain.model.User;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthUseCaseImpl implements AuthPort {

  private final UserRepositoryPort userRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenGeneratorPort tokenGenerator;

  public AuthUseCaseImpl(UserRepositoryPort userRepository,
      PasswordEncoder passwordEncoder, TokenGeneratorPort tokenGenerator) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenGenerator = tokenGenerator;
  }

  @Override
  public JwtToken login(String username, String password) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Credenciales invalidas"));

    if (!user.isEnabled()) {
      throw new IllegalStateException("Usuario deshabilitado");
    }

    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new IllegalArgumentException("Credenciales invalidas");
    }

    String token = tokenGenerator.generateToken(user.getId(), user.getUsername(), user.getScopes());
    return new JwtToken(token, user.getUsername(), user.getScopes());
  }

  @Override
  public User register(String username, String password, String scopes) {
    if (userRepository.findByUsername(username).isPresent()) {
      throw new IllegalArgumentException("El usuario ya existe");
    }

    String hashedPassword = passwordEncoder.encode(password);
    User user = User.builder()
        .id(UUID.randomUUID().toString())
        .username(username)
        .passwordHash(hashedPassword)
        .scopes(scopes != null && !scopes.isBlank() ? scopes : "READER")
        .enabled(true)
        .build();

    return userRepository.save(user);
  }
}
