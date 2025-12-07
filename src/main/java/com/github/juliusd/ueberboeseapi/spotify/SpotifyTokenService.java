package com.github.juliusd.ueberboeseapi.spotify;

import com.github.juliusd.ueberboeseapi.generated.dtos.OAuthTokenRequestApiDto;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

@Component
@Slf4j
@RequiredArgsConstructor
public class SpotifyTokenService {

  private final SpotifyAuthProperties spotifyAuthProperties;

  public AuthorizationCodeCredentials loadSpotifyAuth(
      OAuthTokenRequestApiDto oauthTokenRequestApiDto) {
    try {
      checkProperties();
      SpotifyApi spotifyApi =
          new SpotifyApi.Builder()
              .setRefreshToken(spotifyAuthProperties.refreshToken())
              .setClientId(spotifyAuthProperties.clientId())
              .setClientSecret(spotifyAuthProperties.clientSecret())
              .build();

      var authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
      var authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();
      log.info(
          "Spotify auth refresh request successful with scope {}",
          authorizationCodeCredentials.getScope());
      return authorizationCodeCredentials;
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      log.warn("Spotify auth failed: {}", e.getMessage());
      throw new SpotifyException(e);
    }
  }

  private void checkProperties() {
    if (spotifyAuthProperties.clientId() == null || spotifyAuthProperties.clientId().isBlank()) {
      log.warn("Spotify client ID is empty or not configured");
    }
    if (spotifyAuthProperties.clientSecret() == null
        || spotifyAuthProperties.clientSecret().isBlank()) {
      log.warn("Spotify client secret is empty or not configured");
    }
    if (spotifyAuthProperties.refreshToken() == null
        || spotifyAuthProperties.refreshToken().isBlank()) {
      log.warn("Spotify refresh token is empty or not configured");
    }
  }
}
