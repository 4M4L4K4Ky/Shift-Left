package com.amalakaky.aegiscode.infrastructure.config;

import com.amalakaky.aegiscode.application.port.in.AuthPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationInitSeeder implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(ApplicationInitSeeder.class);
  private final AuthPort authPort;

  public ApplicationInitSeeder(AuthPort authPort) {
    this.authPort = authPort;
  }

  @Override
  public void run(String... args) {
    String[][] users = {
        {"admin", "Admin1234!", "READER,WRITER"},
        {"testuser", "Test1234!", "READER,WRITER"},
        {"readeronly", "Test1234!", "READER"},
    };

    for (String[] u : users) {
      try {
        authPort.register(u[0], u[1], u[2]);
        log.info("Seeded user: {}", u[0]);
      } catch (Exception e) {
        log.debug("User {} already exists: {}", u[0], e.getMessage());
      }
    }
  }
}
