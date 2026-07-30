package com.amalakaky.aegiscode.infrastructure.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.AuthResponseDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.LoginRequestDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.RegisterRequestDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AuthApiTest {

    // Instancia anónima para evaluar los métodos default directamente
    private final AuthApi defaultApi = new AuthApi() {};

    @Test
    void getDelegate_shouldReturnDefaultAuthApiDelegate() {
        AuthApiDelegate delegate = defaultApi.getDelegate();
        assertThat(delegate).isNotNull();
    }

    @Test
    void defaultMethods_shouldDelegateToDefaultAuthApiDelegate() throws Exception {
        LoginRequestDto loginDto = new LoginRequestDto();
        RegisterRequestDto registerDto = new RegisterRequestDto();

        ResponseEntity<AuthResponseDto> loginResponse = defaultApi.authLogin(loginDto);
        ResponseEntity<UserResponseDto> meResponse = defaultApi.authMe();
        ResponseEntity<UserResponseDto> registerResponse = defaultApi.authRegister(registerDto);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
        assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
    }

    @Test
    void defaultMethods_shouldDelegateToCustomInjectedDelegate() throws Exception {
        AuthApiDelegate mockDelegate = mock(AuthApiDelegate.class);

        AuthApi apiWithCustomDelegate = new AuthApi() {
            @Override
            public AuthApiDelegate getDelegate() {
                return mockDelegate;
            }
        };

        LoginRequestDto loginDto = new LoginRequestDto();
        RegisterRequestDto registerDto = new RegisterRequestDto();

        ResponseEntity<AuthResponseDto> expectedLoginResp = ResponseEntity.ok(new AuthResponseDto());
        ResponseEntity<UserResponseDto> expectedUserResp = ResponseEntity.ok(new UserResponseDto());

        when(mockDelegate.authLogin(loginDto)).thenReturn(expectedLoginResp);
        when(mockDelegate.authMe()).thenReturn(expectedUserResp);
        when(mockDelegate.authRegister(registerDto)).thenReturn(expectedUserResp);

        assertThat(apiWithCustomDelegate.authLogin(loginDto)).isEqualTo(expectedLoginResp);
        assertThat(apiWithCustomDelegate.authMe()).isEqualTo(expectedUserResp);
        assertThat(apiWithCustomDelegate.authRegister(registerDto)).isEqualTo(expectedUserResp);

        verify(mockDelegate).authLogin(loginDto);
        verify(mockDelegate).authMe();
        verify(mockDelegate).authRegister(registerDto);
    }
}