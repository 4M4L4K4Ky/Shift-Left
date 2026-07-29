package com.amalakaky.aegiscode.infrastructure.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.NativeWebRequest;

class ApiUtilTest {

  @SneakyThrows
  @Test
  void setExampleResponse_shouldWriteToResponse() {
    var req = mock(NativeWebRequest.class);
    var res = mock(HttpServletResponse.class);
    var stringWriter = new StringWriter();
    var writer = new PrintWriter(stringWriter);
    when(req.getNativeResponse(HttpServletResponse.class)).thenReturn(res);
    when(res.getWriter()).thenReturn(writer);

    ApiUtil.setExampleResponse(req, "application/json", "{\"key\":\"value\"}");

    verify(res).setCharacterEncoding("UTF-8");
    verify(res).addHeader("Content-Type", "application/json");
    assertThat(stringWriter.toString()).isEqualTo("{\"key\":\"value\"}");
  }

  @SneakyThrows
  @Test
  void setExampleResponse_shouldThrowRuntimeExceptionOnIoError() {
    var req = mock(NativeWebRequest.class);
    var res = mock(HttpServletResponse.class);
    when(req.getNativeResponse(HttpServletResponse.class)).thenReturn(res);
    when(res.getWriter()).thenThrow(new IOException("stream closed"));

    assertThatThrownBy(() -> ApiUtil.setExampleResponse(req, "application/json", "{}"))
        .isInstanceOf(RuntimeException.class)
        .hasCauseInstanceOf(IOException.class);
  }

  @SneakyThrows
  @Test
  void privateConstructor_shouldNotBeInstantiated() {
    assertThatThrownBy(() -> {
      Constructor<ApiUtil> constructor = ApiUtil.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
    }).isInstanceOf(InvocationTargetException.class)
        .hasCauseInstanceOf(UnsupportedOperationException.class);
  }
}
