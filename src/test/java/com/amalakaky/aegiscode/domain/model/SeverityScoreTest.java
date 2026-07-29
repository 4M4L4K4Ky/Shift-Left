package com.amalakaky.aegiscode.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SeverityScoreTest {

  @Test
  void shouldAcceptValidScores() {
    assertThat(new SeverityScore(1).value()).isEqualTo(1);
    assertThat(new SeverityScore(5).value()).isEqualTo(5);
    assertThat(new SeverityScore(10).value()).isEqualTo(10);
  }

  @Test
  void shouldRejectScoreBelowRange() {
    assertThatThrownBy(() -> new SeverityScore(0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldRejectScoreAboveRange() {
    assertThatThrownBy(() -> new SeverityScore(11))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldConsiderEqualByValue() {
    var a = new SeverityScore(7);
    var b = new SeverityScore(7);
    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }
}
