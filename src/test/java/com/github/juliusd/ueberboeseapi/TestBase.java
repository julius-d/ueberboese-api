package com.github.juliusd.ueberboeseapi;

import com.github.juliusd.ueberboeseapi.device.DeviceRepository;
import com.github.juliusd.ueberboeseapi.group.DeviceGroupRepository;
import com.github.juliusd.ueberboeseapi.preset.PresetRepository;
import com.github.juliusd.ueberboeseapi.recent.Recent;
import com.github.juliusd.ueberboeseapi.recent.RecentRepository;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyAccount;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyAccountRepository;
import io.restassured.RestAssured;
import java.time.OffsetDateTime;
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
      "ueberboese.bmx.enabled=true",
      "ueberboese.data-directory=src/test/resources/test-data",
      "proxy.target-host=http://localhost:8089",
      "proxy.auth-target-host=http://localhost:8090",
      "proxy.software-update-target-host=http://localhost:8091",
      "proxy.stats-target-host=http://localhost:8092",
      "proxy.bmx-registry-host=http://localhost:8093",
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
  @Autowired protected SpotifyAccountRepository spotifyAccountRepository;
  @Autowired protected DeviceRepository deviceRepository;
  @Autowired protected RecentRepository recentRepository;
  @Autowired protected PresetRepository presetRepository;
  @Autowired protected DeviceGroupRepository deviceGroupRepository;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    spotifyAccountRepository.deleteAll();
    deviceRepository.deleteAll();
    recentRepository.deleteAll();
    presetRepository.deleteAll();
    deviceGroupRepository.deleteAll();
  }

  protected void givenRecentsInDB() {
    // Add test recents data (ordered by lastPlayedAt DESC - most recent first)
    // Using source IDs that exist in the static XML sources section
    recentRepository.save(
        Recent.builder()
            .id(null)
            .accountId("6921042")
            .name("Ghostsitter 42 - Das Haus im Moor")
            .location("/playback/container/c3BvdGlmeTphbGJ1bTowZ3BGWVZNbVV6VkVxeVAyeUh3cEha")
            .sourceId("19989643") // Spotify source that exists in sources section
            .contentItemType("tracklisturl")
            .deviceId("587A628A4042")
            .lastPlayedAt(OffsetDateTime.parse("2025-12-13T17:14:28.000+00:00"))
            .createdOn(OffsetDateTime.parse("2025-12-13T17:14:28.000+00:00"))
            .updatedOn(OffsetDateTime.now())
            .version(null)
            .build());

    recentRepository.save(
        Recent.builder()
            .id(null)
            .accountId("6921042")
            .name("Radio TEDDY")
            .location("/v1/playback/station/s80044")
            .sourceId("19989342") // TuneIn source that exists in sources section
            .contentItemType("stationurl")
            .deviceId("587A628A4042")
            .lastPlayedAt(OffsetDateTime.parse("2018-11-27T18:20:01.000+00:00"))
            .createdOn(OffsetDateTime.parse("2018-11-27T18:20:01.000+00:00"))
            .updatedOn(OffsetDateTime.now())
            .version(null)
            .build());
  }

  protected void givenSpotifyAccountsInDB() {
    // Add test Spotify accounts for patching tests
    spotifyAccountRepository.save(
        new SpotifyAccount(
            "mockuser123",
            "Mock User 1",
            "mockTokenUser1",
            OffsetDateTime.parse("2019-07-20T17:48:31.000+00:00"),
            OffsetDateTime.now(),
            null));
    spotifyAccountRepository.save(
        new SpotifyAccount(
            "user1namespot",
            "Mock User 2",
            "mockTokenUser2",
            OffsetDateTime.parse("2019-07-20T17:48:31.000+00:00"),
            OffsetDateTime.now(),
            null));
  }
}
