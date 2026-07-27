package com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class SourceCodeRequestTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shouldAcceptValidSourceCode() {
    var request = new SourceCodeRequest("public class Test {}");
    var violations = validator.validate(request);
    assertThat(violations).isEmpty();
  }

  @Test
  void shouldRejectBlankSourceCode() {
    var request = new SourceCodeRequest("");
    var violations = validator.validate(request);
    assertThat(violations).isNotEmpty();
  }

  @Test
  void shouldRejectOverMaxSize() {
    var oversized = "a".repeat(50001);
    var request = new SourceCodeRequest(oversized);
    var violations = validator.validate(request);
    assertThat(violations).isNotEmpty();
  }

  @Test
  void shouldAcceptAtMaxSize() {
    var exact = "a".repeat(50000);
    var request = new SourceCodeRequest(exact);
    var violations = validator.validate(request);
    assertThat(violations).isEmpty();
  }
}
