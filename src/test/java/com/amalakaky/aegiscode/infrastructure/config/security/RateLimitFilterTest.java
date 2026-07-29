package com.amalakaky.aegiscode.infrastructure.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

  private RateLimitFilter filter;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  private final StringWriter writer = new StringWriter();

  @BeforeEach
  void setUp() throws Exception {
    filter = new RateLimitFilter();
  }

  @Test
  void shouldAllowRequestWithinLimit() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/some-other-endpoint");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldRejectWhenExceedingLimit() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/scans");
    when(response.getWriter()).thenReturn(new PrintWriter(writer));

    for (int i = 0; i < 6; i++) {
      filter.doFilterInternal(request, response, filterChain);
    }

    verify(response).setStatus(429);
    verify(filterChain, times(5)).doFilter(request, response);
    assertThat(writer.toString()).contains("Rate limit exceeded");
  }

  @Test
  void shouldNotRateLimitNonScanEndpoints() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/v1/audits/statistics");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(response);
  }
}
