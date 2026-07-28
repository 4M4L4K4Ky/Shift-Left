package com.amalakaky.aegiscode.infrastructure.adapter.in.rest.delegate;

import com.amalakaky.aegiscode.application.port.in.AuthPort;
import com.amalakaky.aegiscode.domain.model.JwtToken;
import com.amalakaky.aegiscode.domain.model.User;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.AuthApiDelegate;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.AuthResponseDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.LoginRequestDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.RegisterRequestDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.UserResponseDto;
import java.util.stream.Collectors;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthApiDelegateImpl implements AuthApiDelegate {

  private final AuthPort authUseCase;

  public AuthApiDelegateImpl(AuthPort authUseCase) {
    this.authUseCase = authUseCase;
  }

  @Override
  public ResponseEntity<AuthResponseDto> authLogin(LoginRequestDto loginRequestDto) {
    try {
      JwtToken token = authUseCase.login(
          loginRequestDto.getUsername(), loginRequestDto.getPassword());

      AuthResponseDto dto = new AuthResponseDto();
      dto.setToken(token.getToken());
      dto.setUsername(token.getUsername());
      dto.setScopes(token.getScopes());
      return ResponseEntity.ok(dto);

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new AuthResponseDto());
    }
  }

  @Override
  public ResponseEntity<UserResponseDto> authRegister(RegisterRequestDto registerRequestDto) {
    try {
      String scopes = registerRequestDto.getScopes() != null
          ? registerRequestDto.getScopes().stream()
              .map(Enum::name)
              .collect(Collectors.joining(","))
          : "READER";

      User user = authUseCase.register(
          registerRequestDto.getUsername(), registerRequestDto.getPassword(), scopes);

      UserResponseDto dto = new UserResponseDto();
      dto.setId(UUID.fromString(user.getId()));
      dto.setUsername(user.getUsername());
      dto.setScopes(scopes);
      return ResponseEntity.ok(dto);

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new UserResponseDto());
    }
  }

  @Override
  public ResponseEntity<UserResponseDto> authMe() {
    var auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !auth.isAuthenticated()
        || "anonymousUser".equals(auth.getPrincipal())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new UserResponseDto());
    }

    UserResponseDto dto = new UserResponseDto();
    dto.setId(UUID.fromString((String) auth.getPrincipal()));
    dto.setUsername((String) auth.getDetails());
    dto.setScopes(auth.getAuthorities().stream()
        .map(Object::toString)
        .collect(Collectors.joining(",")));
    return ResponseEntity.ok(dto);
  }
}
