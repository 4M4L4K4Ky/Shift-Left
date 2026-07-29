package com.amalakaky.aegiscode.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class JwtToken {

  private final String token;
  private final String username;
  private final String scopes;
}
