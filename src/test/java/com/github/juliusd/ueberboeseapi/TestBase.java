package com.github.juliusd.ueberboeseapi;

import com.github.juliusd.ueberboeseapi.device.DeviceRepository;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyAccountRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "ueberboese.experimental.enabled=true",
      "ueberboese.oauth.enabled=true",
      "ueberboese.data-directory=src/test/resources/test-data",
      "proxy.target-host=http://localhost:8089",
      "proxy.auth-target-host=http://localhost:8090",
      "proxy.software-update-target-host=http://localhost:8091",
      "spotify.auth.client-id=test-client-id",
      "spotify.auth.client-secret=test-client-secret",
      "spotify.mgmt.redirect-uri=ueberboese-login://spotify",
      "spotify.api.auth-base-url=http://localhost:8299",
      "spotify.api.base-url=http://localhost:8299",
      "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
      "ueberboese.mgmt.password=test-password-123",
      "logging.level.org.springframework.jdbc.core=DEBUG",
      "logging.level.org.springframework.data.jdbc=DEBUG"
    })
public class TestBase {

  @LocalServerPort private int port;
  @Autowired private SpotifyAccountRepository spotifyAccountRepository;
  @Autowired protected DeviceRepository deviceRepository;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    spotifyAccountRepository.deleteAll();
    deviceRepository.deleteAll();
  }
}
