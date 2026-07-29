package com.amalakaky.aegiscode;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class AegiscodeApplicationTest {

  @Test
  void contextLoads() {
  }

  @Test
  void mainMethod_shouldStartApplication() {
    AegiscodeApplication.main(new String[]{"--spring.profiles.active=local"});
  }
}
