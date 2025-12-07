package com.github.juliusd.ueberboeseapi.spotify;

import com.github.juliusd.ueberboeseapi.generated.dtos.OAuthTokenRequestApiDto;
import java.io.IOException;
import java.util.Set;
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

  private static final Set<String> REQUIRED_SCOPES =
      Set.of(
          "playlist-read-private",
          "playlist-read-collaborative",
          "streaming",
          "user-library-read",
          "user-library-modify",
          "playlist-modify-private",
          "playlist-modify-public",
          "user-read-email",
          "user-read-private",
          "user-top-read");

  private final SpotifyAuthProperties spotifyAuthProperties;

  public AuthorizationCodeCredentials loadSpotifyAuth(
      OAuthTokenRequestApiDto oauthTokenRequestApiDto) {
    try {
      checkProperties(oauthTokenRequestApiDto);
      SpotifyApi spotifyApi =
          new SpotifyApi.Builder()
              .setRefreshToken(spotifyAuthProperties.refreshToken())
              .setClientId(spotifyAuthProperties.clientId())
              .setClientSecret(spotifyAuthProperties.clientSecret())
              .build();

      var authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
      var authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();

      String actualScope = authorizationCodeCredentials.getScope();
      log.info("Spotify auth refresh request successful with scope {}", actualScope);

      // Validate that all required scopes are present
      validateScopes(actualScope);

      return authorizationCodeCredentials;
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      log.warn("Spotify auth failed: {}", e.getMessage());
      throw new SpotifyException(e);
    }
  }

  private void checkProperties(OAuthTokenRequestApiDto oauthTokenRequestApiDto) {
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

    // Check if request refresh token differs from configured token
    String requestRefreshToken = oauthTokenRequestApiDto.getRefreshToken();
    String configuredRefreshToken = spotifyAuthProperties.refreshToken();

    if (requestRefreshToken != null
        && configuredRefreshToken != null
        && !requestRefreshToken.equals(configuredRefreshToken)) {
      String requestTokenPreview = maskToken(requestRefreshToken);
      String configuredTokenPreview = maskToken(configuredRefreshToken);

      log.info(
          "Refresh token mismatch - Request token: {}, Configured token: {}",
          requestTokenPreview,
          configuredTokenPreview);
    }
  }

  private static String maskToken(String token) {
    if (token == null || token.length() < 4) {
      return "****";
    }
    String first2 = token.substring(0, 2);
    String last2 = token.substring(token.length() - 2);
    return first2 + "..." + last2;
  }

  private void validateScopes(String actualScope) {
    if (actualScope == null || actualScope.isBlank()) {
      log.warn("Spotify token has no scopes");
      return;
    }

    // Split the actual scopes (space-separated)
    String[] actualScopes = actualScope.split("\\s+");
    Set<String> actualScopeSet = Set.of(actualScopes);

    // Check for missing scopes
    java.util.List<String> missingScopes = new java.util.ArrayList<>();
    for (String requiredScope : REQUIRED_SCOPES) {
      if (!actualScopeSet.contains(requiredScope)) {
        missingScopes.add(requiredScope);
      }
    }

    if (!missingScopes.isEmpty()) {
      log.warn("Spotify token is missing required scopes: {}", String.join(", ", missingScopes));
    }
  }
}
