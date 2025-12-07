package com.github.juliusd.ueberboeseapi.spotify;

import com.github.juliusd.ueberboeseapi.generated.dtos.OAuthTokenRequestApiDto;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

@Component
@Slf4j
public class SpotifyTokenService {

  private final SpotifyAuthProperties spotifyAuthProperties;

  public SpotifyTokenService(SpotifyAuthProperties spotifyAuthProperties) {
    this.spotifyAuthProperties = spotifyAuthProperties;
  }

  public AuthorizationCodeCredentials loadSpotifyAuth(
      OAuthTokenRequestApiDto oauthTokenRequestApiDto) {
    try {
      SpotifyApi spotifyApi =
          new SpotifyApi.Builder()
              .setRefreshToken(spotifyAuthProperties.refreshToken())
              .setClientId(spotifyAuthProperties.clientId())
              .setClientSecret(spotifyAuthProperties.clientSecret())
              .build();

      var authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
      var authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();
      return authorizationCodeCredentials;
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      log.warn("Spotify auth failed: {}", e.getMessage());
      throw new SpotifyException(e);
    }
  }
}
