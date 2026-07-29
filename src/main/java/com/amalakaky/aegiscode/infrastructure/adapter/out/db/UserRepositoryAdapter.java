package com.amalakaky.aegiscode.infrastructure.adapter.out.db;

import com.amalakaky.aegiscode.application.port.out.db.UserRepositoryPort;
import com.amalakaky.aegiscode.domain.model.User;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity.UserEntity;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository.UserJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

  private final UserJpaRepository jpaRepository;

  public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return jpaRepository.findByUsername(username)
        .map(this::toDomain);
  }

  @Override
  public User save(User user) {
    UserEntity entity = new UserEntity(user.getId(), user.getUsername(),
        user.getPasswordHash(), user.getScopes(), user.isEnabled());
    UserEntity saved = jpaRepository.save(entity);
    return toDomain(saved);
  }

  private User toDomain(UserEntity entity) {
    return new User(entity.getId(), entity.getUsername(),
        entity.getPasswordHash(), entity.getScopes(), entity.isEnabled());
  }
}
