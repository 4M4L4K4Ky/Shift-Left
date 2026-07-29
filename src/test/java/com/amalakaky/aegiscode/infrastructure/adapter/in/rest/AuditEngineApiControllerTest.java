package com.amalakaky.aegiscode.infrastructure.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.AuditReportDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.AuditStatisticsResponseDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.GitHubScanRequestDto;
import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.ScanRequestDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuditEngineApiControllerTest {

  @Mock
  private AuditEngineApiDelegate delegate;

  private AuditEngineApiController controller;

  @BeforeEach
  void setUp() {
    controller = new AuditEngineApiController(delegate);
  }

  @Test
  void getDelegate_shouldReturnInjectedDelegate() {
    assertThat(controller.getDelegate()).isSameAs(delegate);
  }

  @Test
  @SneakyThrows
  @SuppressWarnings("unchecked")
  void auditsInlinePost_shouldDelegateToDelegate() {
    var request = new ScanRequestDto();
    ResponseEntity<AuditReportDto> expected =
        (ResponseEntity<AuditReportDto>) (ResponseEntity<?>) ResponseEntity.accepted().build();
    when(delegate.auditsInlinePost(request)).thenReturn(expected);

    ResponseEntity<AuditReportDto> response = controller.auditsInlinePost(request);

    assertThat(response).isSameAs(expected);
    verify(delegate).auditsInlinePost(request);
  }

  @Test
  @SneakyThrows
  @SuppressWarnings("unchecked")
  void auditsRepositoryPost_shouldDelegateToDelegate() {
    var request = new GitHubScanRequestDto();
    ResponseEntity<AuditReportDto> expected =
        (ResponseEntity<AuditReportDto>) (ResponseEntity<?>) ResponseEntity.ok().build();
    when(delegate.auditsRepositoryPost(request)).thenReturn(expected);

    ResponseEntity<AuditReportDto> response = controller.auditsRepositoryPost(request);

    assertThat(response).isSameAs(expected);
    verify(delegate).auditsRepositoryPost(request);
  }

  @Test
  @SneakyThrows
  @SuppressWarnings("unchecked")
  void auditsStatisticsGet_shouldDelegateToDelegate() {
    ResponseEntity<AuditStatisticsResponseDto> expected =
        (ResponseEntity<AuditStatisticsResponseDto>) (ResponseEntity<?>) ResponseEntity.ok().build();
    when(delegate.auditsStatisticsGet()).thenReturn(expected);

    ResponseEntity<AuditStatisticsResponseDto> response = controller.auditsStatisticsGet();

    assertThat(response).isSameAs(expected);
    verify(delegate).auditsStatisticsGet();
  }

  @Test
  @SneakyThrows
  @SuppressWarnings("unchecked")
  void getAuditReport_shouldDelegateToDelegate() {
    ResponseEntity<Resource> expected =
        (ResponseEntity<Resource>) (ResponseEntity<?>) ResponseEntity.ok().build();
    when(delegate.getAuditReport()).thenReturn(expected);

    ResponseEntity<Resource> response = controller.getAuditReport();

    assertThat(response).isSameAs(expected);
    verify(delegate).getAuditReport();
  }

  @Test
  void constructor_withNullDelegate_shouldCreateAnonymousFallback() {
    var ctrl = new AuditEngineApiController(null);
    assertThat(ctrl.getDelegate()).isNotNull();
  }
}
