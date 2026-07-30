package com.amalakaky.aegiscode.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amalakaky.aegiscode.application.port.out.db.UserRepositoryPort;
import com.amalakaky.aegiscode.application.port.out.security.TokenGeneratorPort;
import com.amalakaky.aegiscode.domain.model.JwtToken;
import com.amalakaky.aegiscode.domain.model.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenGeneratorPort tokenGenerator;

    private AuthUseCaseImpl authUseCase;

    @BeforeEach
    void setUp() {
        authUseCase = new AuthUseCaseImpl(userRepository, passwordEncoder, tokenGenerator);
    }

    // ==========================================
    // TESTS DE LOGIN
    // ==========================================

    @Test
    void login_shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authUseCase.login("nonexistent",
                "password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Credenciales invalidas");
    }

    @Test
    void login_shouldThrowExceptionWhenUserIsDisabled() {
        User disabledUser = User.builder()
                .id("user-id-1")
                .username("disabledUser")
                .passwordHash("hashedPassword")
                .scopes("READER")
                .enabled(false)
                .build();

        when(userRepository.findByUsername("disabledUser")).thenReturn(Optional.of(disabledUser));

        assertThatThrownBy(() -> authUseCase.login("disabledUser",
                "password"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Usuario deshabilitado");
    }

    @Test
    void login_shouldThrowExceptionWhenPasswordDoesNotMatch() {
        User user = User.builder()
                .id("user-id-1")
                .username("john")
                .passwordHash("hashedPassword")
                .scopes("READER")
                .enabled(true)
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword",
                "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authUseCase.login("john",
                "wrongPassword"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Credenciales invalidas");
    }

    @Test
    void login_shouldReturnJwtTokenWhenCredentialsAreValid() {
        User user = User.builder()
                .id("user-id-123")
                .username("john")
                .passwordHash("hashedPassword")
                .scopes("READER,WRITER")
                .enabled(true)
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("validPassword",
                "hashedPassword")).thenReturn(true);
        when(tokenGenerator.generateToken("user-id-123", "john", "READER,WRITER"))
                .thenReturn("mocked.jwt.token");

        JwtToken token = authUseCase.login("john", "validPassword");

        assertThat(token).isNotNull();
        assertThat(token.getToken()).isEqualTo("mocked.jwt.token");
        assertThat(token.getUsername()).isEqualTo("john");
        assertThat(token.getScopes()).isEqualTo("READER,WRITER");
    }

    // ==========================================
    // TESTS DE REGISTER
    // ==========================================

    @Test
    void register_shouldThrowExceptionWhenUserAlreadyExists() {
        User existingUser = User.builder().username("existingUser").build();
        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authUseCase.register("existingUser",
                "password", "READER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El usuario ya existe");
    }

    @Test
    void register_shouldSaveUserWithGivenScopesWhenScopesProvided() {
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawPassword")).thenReturn("hashedPassword");

        User savedUser = User.builder()
                .id("gen-uuid")
                .username("newUser")
                .passwordHash("hashedPassword")
                .scopes("ADMIN")
                .enabled(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = authUseCase.register("newUser", "rawPassword", "ADMIN");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newUser");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo("newUser");
        assertThat(capturedUser.getPasswordHash()).isEqualTo("hashedPassword");
        assertThat(capturedUser.getScopes()).isEqualTo("ADMIN");
        assertThat(capturedUser.isEnabled()).isTrue();
        assertThat(capturedUser.getId()).isNotBlank();
    }

    @Test
    void register_shouldSaveUserWithDefaultReaderScopeWhenScopesIsNull() {
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawPassword")).thenReturn("hashedPassword");

        User savedUser = User.builder().username("newUser").scopes("READER").build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authUseCase.register("newUser", "rawPassword", null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getScopes()).isEqualTo("READER");
    }

    @Test
    void register_shouldSaveUserWithDefaultReaderScopeWhenScopesIsBlank() {
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawPassword")).thenReturn("hashedPassword");

        User savedUser = User.builder().username("newUser").scopes("READER").build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authUseCase.register("newUser", "rawPassword", "   ");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getScopes()).isEqualTo("READER");
    }
}