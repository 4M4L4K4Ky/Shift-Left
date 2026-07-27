package com.amalakaky.aegiscode;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AegiscodeApplicationTest {

  @Test
  void contextLoads() {
  }

  @Test
  void mainMethod_shouldStartApplication() {
    AegiscodeApplication.main(new String[]{});
  }
}
