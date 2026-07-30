package com.amalakaky.aegiscode.infrastructure.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter(jwtService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldDoNothingWhenHeaderIsNull() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldDoNothingWhenHeaderDoesNotStartWithBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldDoNothingWhenTokenIsInvalid() throws Exception {
        String invalidToken = "invalid-token-123";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtService.validateToken(invalidToken)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldAuthenticateWhenTokenIsValid() throws Exception {
        String validToken = "valid.jwt.token";
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String username = "developer";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(jwtService.extractUserId(validToken)).thenReturn(userId);
        when(jwtService.extractUsername(validToken)).thenReturn(username);
        when(jwtService.extractScopes(validToken)).thenReturn(List.of("READER", "WRITER"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(userId);
        assertThat(auth.getDetails()).isEqualTo(username);
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("READER", "WRITER");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldFilterOutEmptyOrBlankScopes() throws Exception {
        String validToken = "valid.jwt.token";
        String userId = "user-id-456";
        String username = "cleanCodeUser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(jwtService.extractUserId(validToken)).thenReturn(userId);
        when(jwtService.extractUsername(validToken)).thenReturn(username);
        // Incluye elementos vacíos y con espacios en blanco para forzar la lambda .filter(s -> !s.isEmpty())
        when(jwtService.extractScopes(validToken)).thenReturn(List.of("READER", "", "   ", "WRITER"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("READER", "WRITER");

        verify(filterChain).doFilter(request, response);
    }
}