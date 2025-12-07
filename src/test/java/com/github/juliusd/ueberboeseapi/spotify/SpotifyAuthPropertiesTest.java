package com.github.juliusd.ueberboeseapi.spotify;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.ueberboeseapi.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SpotifyAuthPropertiesTest extends TestBase {

  @Autowired private SpotifyAuthProperties spotifyAuthProperties;

  @Test
  void shouldLoadSpotifyAuthProperties() {
    assertThat(spotifyAuthProperties).isNotNull();
    assertThat(spotifyAuthProperties.clientId()).isEqualTo("test-client-id");
    assertThat(spotifyAuthProperties.clientSecret()).isEqualTo("test-client-secret");
    assertThat(spotifyAuthProperties.refreshToken()).isEqualTo("test-refresh-token");
  }
}
