package com.amalakaky.aegiscode.infrastructure.adapter.in.rest.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.amalakaky.aegiscode.application.port.in.AuthPort;
import com.amalakaky.aegiscode.domain.model.JwtToken;
import com.amalakaky.aegiscode.domain.model.User;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.ApiUtil;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.AuthApiDelegate;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.AuthResponseDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.LoginRequestDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.RegisterRequestDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.RegisterRequestDto.ScopesEnum;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.UserResponseDto;
import static org.mockito.ArgumentMatchers.any;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.NativeWebRequest;

@ExtendWith(MockitoExtension.class)
class AuthApiDelegateImplTest {

    @Mock
    private AuthPort authUseCase;

    @Mock
    private Authentication authentication;

    private AuthApiDelegateImpl delegate;

    @BeforeEach
    void setUp() {
        delegate = new AuthApiDelegateImpl(authUseCase);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==========================================
    // TESTS DE authLogin
    // ==========================================

    @Test
    void authLogin_shouldReturnOkWhenCredentialsAreValid() {
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setUsername("admin");
        loginDto.setPassword("secret123");

        JwtToken jwtToken = new JwtToken("token.jwt.val", "admin", "READER,WRITER");
        when(authUseCase.login("admin", "secret123")).thenReturn(jwtToken);

        ResponseEntity<AuthResponseDto> response = delegate.authLogin(loginDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo("token.jwt.val");
        assertThat(response.getBody().getUsername()).isEqualTo("admin");
        assertThat(response.getBody().getScopes()).isEqualTo("READER,WRITER");
    }

    @Test
    void authLogin_shouldReturnUnauthorizedWhenIllegalArgumentExceptionThrown() {
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setUsername("user");
        loginDto.setPassword("badpass");

        when(authUseCase.login("user", "badpass"))
                .thenThrow(new IllegalArgumentException("Credenciales invalidas"));

        ResponseEntity<AuthResponseDto> response = delegate.authLogin(loginDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNull();
    }

    // ==========================================
    // TESTS DE authRegister
    // ==========================================

    @Test
    void authRegister_shouldReturnOkWithMappedScopesWhenScopesIsNotNull() {
        RegisterRequestDto registerDto = new RegisterRequestDto();
        registerDto.setUsername("newuser");
        registerDto.setPassword("password123");
        registerDto.setScopes(List.of(ScopesEnum.READER, ScopesEnum.WRITER));

        String generatedId = UUID.randomUUID().toString();
        User user = User.builder()
                .id(generatedId)
                .username("newuser")
                .scopes("READER,WRITER")
                .build();

        when(authUseCase.register("newuser", "password123", "READER,WRITER")).thenReturn(user);

        ResponseEntity<UserResponseDto> response = delegate.authRegister(registerDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(UUID.fromString(generatedId));
        assertThat(response.getBody().getUsername()).isEqualTo("newuser");
        assertThat(response.getBody().getScopes()).isEqualTo("READER,WRITER");
    }

    @Test
    void authRegister_shouldReturnOkWithDefaultScopesWhenScopesIsNull() {
        RegisterRequestDto registerDto = new RegisterRequestDto();
        registerDto.setUsername("nullscopesuser");
        registerDto.setPassword("password123");
        registerDto.setScopes(null);

        String generatedId = UUID.randomUUID().toString();
        User user = User.builder()
                .id(generatedId)
                .username("nullscopesuser")
                .scopes("READER")
                .build();

        when(authUseCase.register("nullscopesuser", "password123", "READER")).thenReturn(user);

        ResponseEntity<UserResponseDto> response = delegate.authRegister(registerDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(UUID.fromString(generatedId));
        assertThat(response.getBody().getScopes()).isEqualTo("READER");
    }

    @Test
    void authRegister_shouldReturnConflictWhenIllegalArgumentExceptionThrown() {
        RegisterRequestDto registerDto = new RegisterRequestDto();
        registerDto.setUsername("existinguser");
        registerDto.setPassword("password123");

        when(authUseCase.register("existinguser", "password123", "READER"))
                .thenThrow(new IllegalArgumentException("El usuario ya existe"));

        ResponseEntity<UserResponseDto> response = delegate.authRegister(registerDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNull();
    }

    // ==========================================
    // TESTS DE authMe
    // ==========================================

    @Test
    void authMe_shouldReturnUnauthorizedWhenAuthenticationIsNull() {
        SecurityContextHolder.clearContext();

        ResponseEntity<UserResponseDto> response = delegate.authMe();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNull();
    }

    @Test
    void authMe_shouldReturnUnauthorizedWhenNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResponseEntity<UserResponseDto> response = delegate.authMe();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void authMe_shouldReturnUnauthorizedWhenPrincipalIsAnonymousUser() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResponseEntity<UserResponseDto> response = delegate.authMe();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void authMe_shouldReturnOkWhenAuthenticatedWithValidUser() {
        String userIdStr = UUID.randomUUID().toString();
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userIdStr);
        when(authentication.getDetails()).thenReturn("testuser");

        doReturn(List.of(new SimpleGrantedAuthority("READER"), new SimpleGrantedAuthority("WRITER")))
                .when(authentication).getAuthorities();

        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResponseEntity<UserResponseDto> response = delegate.authMe();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(UUID.fromString(userIdStr));
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().getScopes()).isEqualTo("READER,WRITER");
    }


    @Test
    void defaultMethods_shouldSkipExampleWhenMediaTypeIsNotJson() throws Exception {
        NativeWebRequest webRequest = mock(NativeWebRequest.class);
        when(webRequest.getHeader("Accept")).thenReturn("text/plain");

        AuthApiDelegate delegateWithRequest = new AuthApiDelegate() {
            @Override
            public Optional<NativeWebRequest> getRequest() {
                return Optional.of(webRequest);
            }
        };

        ResponseEntity<?> loginResp = delegateWithRequest.authLogin(new LoginRequestDto());
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
    }



    // Instancia anónima para probar los métodos default de la interfaz
    private final AuthApiDelegate defaultDelegate = new AuthApiDelegate() {};

    @Test
    void getRequest_shouldReturnEmptyOptionalByDefault() {
        assertThat(defaultDelegate.getRequest()).isEmpty();
    }

    @Test
    void defaultMethods_shouldReturnNotImplementedWhenNoRequestPresent() throws Exception {
        assertThat(defaultDelegate.authLogin(new LoginRequestDto()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_IMPLEMENTED);
        assertThat(defaultDelegate.authMe().getStatusCode())
                .isEqualTo(HttpStatus.NOT_IMPLEMENTED);
        assertThat(defaultDelegate.authRegister(new RegisterRequestDto()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_IMPLEMENTED);
    }

    @Test
    void defaultMethods_shouldExecuteExampleResponseWhenJsonAcceptHeaderPresent() throws Exception {
        NativeWebRequest webRequest = mock(NativeWebRequest.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);
        PrintWriter printWriter = new PrintWriter(new StringWriter());

        when(webRequest.getHeader("Accept")).thenReturn("application/json");
        when(webRequest.getNativeResponse(HttpServletResponse.class)).thenReturn(httpResponse);
        when(httpResponse.getWriter()).thenReturn(printWriter);

        AuthApiDelegate delegateWithRequest = new AuthApiDelegate() {
            @Override
            public Optional<NativeWebRequest> getRequest() {
                return Optional.of(webRequest);
            }
        };

        ResponseEntity<?> loginResp = delegateWithRequest.authLogin(new LoginRequestDto());
        ResponseEntity<?> meResp = delegateWithRequest.authMe();
        ResponseEntity<?> registerResp = delegateWithRequest.authRegister(new RegisterRequestDto());

        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
        assertThat(meResp.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
        assertThat(registerResp.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
    }

    @Test
    void defaultMethods_shouldSkipExampleResponseWhenNonJsonAcceptHeaderPresent() throws Exception {
        NativeWebRequest webRequest = mock(NativeWebRequest.class);
        when(webRequest.getHeader("Accept")).thenReturn("text/plain");

        AuthApiDelegate delegateWithRequest = new AuthApiDelegate() {
            @Override
            public Optional<NativeWebRequest> getRequest() {
                return Optional.of(webRequest);
            }
        };

        ResponseEntity<?> loginResp = delegateWithRequest.authLogin(new LoginRequestDto());
        ResponseEntity<?> meResp = delegateWithRequest.authMe();
        ResponseEntity<?> registerResp = delegateWithRequest.authRegister(new RegisterRequestDto());

        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
        assertThat(meResp.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
        assertThat(registerResp.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
    }




        @Test
        void defaultMethods_shouldCoverFirstIfBranch() throws Exception {
            NativeWebRequest request = mock(NativeWebRequest.class);
            when(request.getHeader("Accept")).thenReturn("application/json");

            AuthApiDelegate delegateWithRequest = new AuthApiDelegate() {
                @Override
                public Optional<NativeWebRequest> getRequest() {
                    return Optional.of(request);
                }
            };

            try (MockedStatic<ApiUtil> apiUtilMock = mockStatic(ApiUtil.class)) {
                ResponseEntity<?> loginResp = delegateWithRequest.authLogin(new LoginRequestDto());
                ResponseEntity<?> meResp = delegateWithRequest.authMe();
                ResponseEntity<?> registerResp = delegateWithRequest.authRegister(new RegisterRequestDto());

                assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
                assertThat(meResp.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
                assertThat(registerResp.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
            }
        }

        @Test
        void defaultMethods_shouldForceCoverSecondIfBranchInAllMethods() throws Exception {
            NativeWebRequest request = mock(NativeWebRequest.class);
            when(request.getHeader("Accept")).thenReturn("application/json");

            AuthApiDelegate delegateWithRequest = new AuthApiDelegate() {
                @Override
                public Optional<NativeWebRequest> getRequest() {
                    return Optional.of(request);
                }
            };

            MediaType mockMediaType = mock(MediaType.class);

            try (MockedStatic<MediaType> mediaTypeMock = mockStatic(MediaType.class);
                 MockedStatic<ApiUtil> apiUtilMock = mockStatic(ApiUtil.class)) {

                // Interceptamos la creación de la lista MediaTypes y de MediaType.valueOf
                mediaTypeMock.when(() -> MediaType.parseMediaTypes(anyString()))
                        .thenReturn(List.of(mockMediaType));
                mediaTypeMock.when(() -> MediaType.valueOf("application/json"))
                        .thenReturn(MediaType.APPLICATION_JSON);

                // 1. Forzar entrada en el 2º IF de authLogin
                when(mockMediaType.isCompatibleWith(any())).thenReturn(false, true);
                ResponseEntity<?> loginResp = delegateWithRequest.authLogin(new LoginRequestDto());
                assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);

                // 2. Forzar entrada en el 2º IF de authMe
                when(mockMediaType.isCompatibleWith(any())).thenReturn(false, true);
                ResponseEntity<?> meResp = delegateWithRequest.authMe();
                assertThat(meResp.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);

                // 3. Forzar entrada en el 2º IF de authRegister
                when(mockMediaType.isCompatibleWith(any())).thenReturn(false, true);
                ResponseEntity<?> registerResp = delegateWithRequest.authRegister(new RegisterRequestDto());
                assertThat(registerResp.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
            }
        }

}