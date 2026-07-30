package com.amalakaky.aegiscode.infrastructure.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class AuthApiControllerTest {

    @Test
    void getDelegate_shouldReturnInjectedDelegate() {
        AuthApiDelegate mockDelegate = mock(AuthApiDelegate.class);
        AuthApiController controller = new AuthApiController(mockDelegate);

        assertThat(controller.getDelegate()).isSameAs(mockDelegate);
    }

    @Test
    void getDelegate_shouldReturnDefaultDelegateWhenNullInjected() {
        AuthApiController controller = new AuthApiController(null);

        assertThat(controller.getDelegate()).isNotNull();
    }
}