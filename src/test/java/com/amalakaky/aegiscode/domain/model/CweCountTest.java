package com.amalakaky.aegiscode.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CweCountTest {

  @Test
  void shouldCreateRecord() {
    var count = new CweCount("CWE-89", 42L);
    assertThat(count.cweId()).isEqualTo("CWE-89");
    assertThat(count.count()).isEqualTo(42L);
  }

  @Test
  void shouldConsiderEqualByValue() {
    var a = new CweCount("CWE-79", 10L);
    var b = new CweCount("CWE-79", 10L);
    assertThat(a).isEqualTo(b);
  }
}
