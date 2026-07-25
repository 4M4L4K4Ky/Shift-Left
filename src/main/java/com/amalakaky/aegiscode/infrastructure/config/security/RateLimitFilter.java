package com.amalakaky.aegiscode.infrastructure.config.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro de rate limiting que protege el endpoint {@code /api/scans}.
 * 
 * Implementa un token bucket con {@link Bucket4j} permitiendo hasta
 * {@value #MAX_REQUESTS_PER_MINUTE} peticiones por minuto. Cuando se excede
 * el límite, retorna HTTP 429 (Too Many Requests).
 * 
 * Nota: el bucket reside en memoria y no persiste entre reinicios.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

  private static final int MAX_REQUESTS_PER_MINUTE = 5;
  private static final int TOKENS_TO_CONSUME = 1;
  private static final String API_SCANS_URI = "/api/scans";

  private final Bucket bucket;

  public RateLimitFilter() {
    Bandwidth limit = Bandwidth.classic(
        MAX_REQUESTS_PER_MINUTE,
        Refill.greedy(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
    );
    this.bucket = Bucket.builder().addLimit(limit).build();
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                  HttpServletResponse response,
                  FilterChain filterChain) throws ServletException, IOException {

    if (request.getRequestURI().startsWith(API_SCANS_URI)) {
      if (!bucket.tryConsume(TOKENS_TO_CONSUME)) {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().write("Rate limit exceeded. Try again later.");
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}




