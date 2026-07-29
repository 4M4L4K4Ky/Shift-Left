package com.amalakaky.aegiscode.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class User {

  private final String id;
  private final String username;
  private final String passwordHash;
  private final String scopes;
  private final boolean enabled;
}
