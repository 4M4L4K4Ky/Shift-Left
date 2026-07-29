package com.amalakaky.aegiscode.infrastructure.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.GitHubScanRequestDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.ScanRequestDto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.NativeWebRequest;

import java.io.PrintWriter;
import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;

class AuditEngineApiDelegateDefaultMethodsTest {

  @Test
  void getRequest_default_shouldReturnEmpty() {
    AuditEngineApiDelegate d = new AuditEngineApiDelegate() {};
    assertEquals(Optional.empty(), d.getRequest());
  }

  @Test
  @SneakyThrows
  void auditsInlinePost_whenNoRequest_shouldReturnNotImplemented() {
    AuditEngineApiDelegate d = new AuditEngineApiDelegate() {};
    var response = d.auditsInlinePost(new ScanRequestDto());
    assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
    void auditsInlinePost_withMatchingAccept_shouldWriteExampleAndReturnNotImplemented()
        throws Exception {
        var nativeWebRequest = mock(NativeWebRequest.class);
        var httpServletResponse = mock(HttpServletResponse.class);
        when(nativeWebRequest.getNativeResponse(HttpServletResponse.class))
            .thenReturn(httpServletResponse);
    when(httpServletResponse.getWriter()).thenReturn(mock(PrintWriter.class));
    when(nativeWebRequest.getHeader("Accept")).thenReturn("application/json");

    AuditEngineApiDelegate delegate = createDelegateWithRequest(nativeWebRequest);
    var response = delegate.auditsInlinePost(new ScanRequestDto());
    assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
  }

  @Test
  void auditsInlinePost_withTextPlainAccept_shouldThrowInvalidMediaType() {
    var nativeWebRequest = mock(NativeWebRequest.class);
    when(nativeWebRequest.getHeader("Accept")).thenReturn("text/plain");

    AuditEngineApiDelegate delegate = createDelegateWithRequest(nativeWebRequest);
        org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.http.InvalidMediaTypeException.class,
            () -> delegate.auditsInlinePost(new ScanRequestDto()));
  }

  @Test
  @SneakyThrows
  void auditsRepositoryPost_whenNoRequest_shouldReturnNotImplemented() {
    AuditEngineApiDelegate d = new AuditEngineApiDelegate() {};
    var response = d.auditsRepositoryPost(new GitHubScanRequestDto());
    assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
  }

  @Test
    void auditsRepositoryPost_withJsonAccept_shouldWriteExampleAndReturnNotImplemented()
        throws Exception {
        var nativeWebRequest = mock(NativeWebRequest.class);
        var httpServletResponse = mock(HttpServletResponse.class);
        when(nativeWebRequest.getNativeResponse(HttpServletResponse.class))
            .thenReturn(httpServletResponse);
    when(httpServletResponse.getWriter()).thenReturn(mock(PrintWriter.class));
    when(nativeWebRequest.getHeader("Accept")).thenReturn("application/json");

    AuditEngineApiDelegate delegate = createDelegateWithRequest(nativeWebRequest);
    var response = delegate.auditsRepositoryPost(new GitHubScanRequestDto());
    assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
  }

  @Test
  @SneakyThrows
  void auditsRepositoryPost_withNonMatchingHeader_shouldSkipApiUtil() {
    var nativeWebRequest = mock(NativeWebRequest.class);
    when(nativeWebRequest.getHeader("Accept")).thenReturn("text/plain");

    AuditEngineApiDelegate delegate = createDelegateWithRequest(nativeWebRequest);
    var response = delegate.auditsRepositoryPost(new GitHubScanRequestDto());
    assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
  }

  @Test
  @SneakyThrows
  void auditsStatisticsGet_whenNoRequest_shouldReturnNotImplemented() {
    AuditEngineApiDelegate d = new AuditEngineApiDelegate() {};
    var response = d.auditsStatisticsGet();
    assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
  }

  @Test
  @SneakyThrows
    void auditsStatisticsGet_withJsonAccept_shouldWriteExampleAndReturnNotImplemented() {
        var nativeWebRequest = mock(NativeWebRequest.class);
        var httpServletResponse = mock(HttpServletResponse.class);
        when(nativeWebRequest.getNativeResponse(HttpServletResponse.class))
            .thenReturn(httpServletResponse);
    when(httpServletResponse.getWriter()).thenReturn(mock(PrintWriter.class));
    when(nativeWebRequest.getHeader("Accept")).thenReturn("application/json");

    AuditEngineApiDelegate delegate = createDelegateWithRequest(nativeWebRequest);
    var response = delegate.auditsStatisticsGet();
    assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
  }

  @Test
  @SneakyThrows
  void auditsStatisticsGet_withNonMatchingAccept_shouldNotWriteExample() {
    var nativeWebRequest = mock(NativeWebRequest.class);
    when(nativeWebRequest.getHeader("Accept")).thenReturn("text/plain");

    AuditEngineApiDelegate delegate = createDelegateWithRequest(nativeWebRequest);
    var response = delegate.auditsStatisticsGet();
    assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getAuditReport_withRequest_shouldReturnNotImplemented() {
    var nativeWebRequest = mock(NativeWebRequest.class);
    AuditEngineApiDelegate delegate = createDelegateWithRequest(nativeWebRequest);
    var response = delegate.getAuditReport();
    assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getAuditReport_whenNoRequest_shouldReturnNotImplemented() {
    AuditEngineApiDelegate d = new AuditEngineApiDelegate() {};
    var response = d.getAuditReport();
    assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
  }

  @Test
  void getDelegate_onAuditEngineApi_shouldCreateAnonymousFallback() {
    AuditEngineApi api = new AuditEngineApi() {};
    AuditEngineApiDelegate d = api.getDelegate();
    assertEquals(Optional.empty(), d.getRequest());
  }

    private static AuditEngineApiDelegate createDelegateWithRequest(
        NativeWebRequest nativeWebRequest) {
    return new AuditEngineApiDelegate() {
      @Override
      public Optional<NativeWebRequest> getRequest() {
        return Optional.of(nativeWebRequest);
      }
    };
  }
}
