package com.amalakaky.aegiscode.infrastructure.adapter.out.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amalakaky.aegiscode.domain.model.User;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity.UserEntity;
import com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository.UserJpaRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private UserJpaRepository jpaRepository;

    private UserRepositoryAdapter repositoryAdapter;

    @BeforeEach
    void setUp() {
        repositoryAdapter = new UserRepositoryAdapter(jpaRepository);
    }

    @Test
    void findByUsername_shouldReturnUserWhenFound() {
        // Given
        String username = "admin";
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UserEntity entity = new UserEntity(userId, username, "hash123", "ROLE_ADMIN", true);
        when(jpaRepository.findByUsername(username)).thenReturn(Optional.of(entity));

        // When
        Optional<User> result = repositoryAdapter.findByUsername(username);

        // Then
        assertThat(result).isPresent();
        User user = result.get();
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getPasswordHash()).isEqualTo("hash123");
        assertThat(user.getScopes()).isEqualTo("ROLE_ADMIN");
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void findByUsername_shouldReturnEmptyWhenNotFound() {
        // Given
        String username = "nonexistent";
        when(jpaRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        Optional<User> result = repositoryAdapter.findByUsername(username);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void save_shouldSaveEntityAndReturnDomainUser() {
        // Given
        String userId = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11";
        User domainUser = new User(userId, "developer", "secretHash",
                "ROLE_USER", true);
        UserEntity savedEntity = new UserEntity(userId, "developer",
                "secretHash", "ROLE_USER", true);

        when(jpaRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        // When
        User result = repositoryAdapter.save(domainUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("developer");
        assertThat(result.getPasswordHash()).isEqualTo("secretHash");
        assertThat(result.getScopes()).isEqualTo("ROLE_USER");
        assertThat(result.isEnabled()).isTrue();

        verify(jpaRepository).save(any(UserEntity.class));
    }
}