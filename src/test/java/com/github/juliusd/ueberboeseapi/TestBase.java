package com.github.juliusd.ueberboeseapi;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "ueberboese.experimental.enabled=true",
      "ueberboese.oauth.enabled=true",
      "ueberboese.data-directory=src/test/resources/test-data",
      "proxy.target-host=http://localhost:8089",
      "spotify.auth.client-id=test-client-id",
      "spotify.auth.client-secret=test-client-secret",
      "spotify.mgmt.redirect-uri=ueberboese-login://spotify",
      "ueberboese.mgmt.password=test-password-123"
    })
public class TestBase {

  @LocalServerPort private int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }
}
