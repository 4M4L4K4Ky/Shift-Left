package com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

  @Id
  @Column(length = 36, nullable = false, updatable = false)
  private String id;

  @Column(nullable = false, unique = true, length = 100)
  private String username;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(name = "role", nullable = false, length = 100)
  private String scopes;

  @Column(nullable = false)
  private boolean enabled;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Builder
  /**
   * Constructor utilizado por Lombok Builder para crear una entidad de usuario.
   */
  public UserEntity(String id, String username, String passwordHash,
      String scopes, boolean enabled) {
    this.id = id;
    this.username = username;
    this.passwordHash = passwordHash;
    this.scopes = scopes;
    this.enabled = enabled;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }
}
