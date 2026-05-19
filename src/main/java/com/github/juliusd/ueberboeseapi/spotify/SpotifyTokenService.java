package com.github.juliusd.ueberboeseapi.spotify;

import com.github.juliusd.ueberboeseapi.generated.dtos.OAuthTokenRequestApiDto;
import com.github.juliusd.ueberboeseapi.spotify.client.SpotifyOAuthClient;
import com.github.juliusd.ueberboeseapi.spotify.dto.AuthorizationCodeCredentialsDto;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
  private final SpotifyAccountService spotifyAccountService;
  private final SpotifyOAuthClient spotifyOAuthClient;

  public AuthorizationCodeCredentialsDto loadSpotifyAuth(
      OAuthTokenRequestApiDto oauthTokenRequestApiDto) {
    checkProperties();

    var accounts = spotifyAccountService.listAllAccounts();
    if (accounts.isEmpty()) {
      log.error("No Spotify accounts connected");
      throw new NoSpotifyAccountException(
          "No Spotify accounts connected. Please connect a Spotify account via the management API.");
    }

    // Determine which account to use based on the incoming request refresh token
    SpotifyAccount targetAccount = null;
    String incomingRefreshToken = oauthTokenRequestApiDto.getRefreshToken();

    if (incomingRefreshToken != null && !incomingRefreshToken.isBlank()) {
      // Try to find the exact matching account in our database
      for (SpotifyAccount account : accounts) {
        if (incomingRefreshToken.equals(account.refreshToken())) {
          targetAccount = account;
          log.debug(
              "Matched incoming refresh token to Spotify account: {} ({})",
              account.displayName(),
              account.spotifyUserId());
          break;
        }
      }
    }

    // Fallback if no explicit match was found
    if (targetAccount == null) {
      targetAccount = accounts.getLast();
      log.info(
          "No exact refresh token match found. Falling back to oldest account: {} ({})",
          targetAccount.displayName(),
          targetAccount.spotifyUserId());
    }

    try {
      // Prepare form data for token refresh using the resolved account
      org.springframework.util.LinkedMultiValueMap<String, String> formData =
          new org.springframework.util.LinkedMultiValueMap<>();
      formData.add("grant_type", "refresh_token");
      formData.add("refresh_token", targetAccount.refreshToken());
      formData.add("client_id", spotifyAuthProperties.clientId());
      formData.add("client_secret", spotifyAuthProperties.clientSecret());

      // Call Spotify API to refresh the access token
      var authorizationCodeCredentials = spotifyOAuthClient.refreshAccessToken(formData);

      String actualScope = authorizationCodeCredentials.scope();
      log.debug("Spotify auth refresh request successful with scope {}", actualScope);

      // Validate that all required scopes are present
      validateScopes(actualScope);

      // Check if Spotify returned a brand new or rotated refresh token
      String latestRefreshToken = authorizationCodeCredentials.refreshToken();
      if (latestRefreshToken == null || latestRefreshToken.isBlank()) {
        // If Spotify does not issue a new token, retain the current active one
        log.debug("No refresh token issued by Spotify, retain the current active one");
        latestRefreshToken = targetAccount.refreshToken();
      }

      // Persist the (potentially rotated) token and refresh the 'updatedAt' timestamp
      spotifyAccountService.updateRefreshToken(targetAccount.spotifyUserId(), latestRefreshToken);

      return authorizationCodeCredentials;
    } catch (RuntimeException e) {
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
