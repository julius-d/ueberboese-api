package com.github.juliusd.ueberboeseapi.spotify;

import com.github.juliusd.ueberboeseapi.spotify.client.SpotifyOAuthClient;
import com.github.juliusd.ueberboeseapi.spotify.client.SpotifyUserClient;
import com.github.juliusd.ueberboeseapi.spotify.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpotifyManagementService {

  // All required scopes from SpotifyTokenService
  private static final String REQUIRED_SCOPES =
      "playlist-read-private playlist-read-collaborative streaming user-library-read user-library-modify playlist-modify-private playlist-modify-public user-read-email user-read-private user-top-read";

  private final SpotifyAuthProperties spotifyAuthProperties;
  private final SpotifyAccountService spotifyAccountService;
  private final SpotifyOAuthClient spotifyOAuthClient;
  private final SpotifyUserClient spotifyUserClient;

  /**
   * Generates the Spotify authorization URL for OAuth flow initialization.
   *
   * @param redirectUri The redirect URI for the OAuth callback
   * @return The Spotify authorization URL
   */
  public String generateAuthorizationUrl(String redirectUri) {
    log.info("Generating Spotify authorization URL with redirectUri: {}", redirectUri);

    try {
      String authUrl =
          UriComponentsBuilder.fromUriString("https://accounts.spotify.com")
              .path("/authorize")
              .queryParam("client_id", spotifyAuthProperties.clientId())
              .queryParam("response_type", "code")
              .queryParam("redirect_uri", redirectUri)
              .queryParam("scope", REQUIRED_SCOPES)
              .build()
              .encode()
              .toUriString();

      log.info("Successfully generated Spotify authorization URL");
      log.debug("Authorization URL: {}", authUrl);

      return authUrl;
    } catch (Exception e) {
      log.error("Failed to generate Spotify authorization URL: {}", e.getMessage());
      throw new SpotifyManagementException("Failed to generate authorization URL", e);
    }
  }

  /**
   * Exchanges the authorization code for access and refresh tokens, retrieves the user profile, and
   * saves the account.
   *
   * @param code The authorization code from Spotify
   * @param redirectUri The redirect URI used during authorization
   * @return The accountId (Spotify user ID)
   * @throws SpotifyManagementException if the exchange fails or account cannot be saved
   */
  public String exchangeCodeForTokens(String code, String redirectUri) {
    log.info("Exchanging authorization code for tokens");

    try {
      // Prepare form data for token exchange
      LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
      formData.add("grant_type", "authorization_code");
      formData.add("code", code);
      formData.add("redirect_uri", redirectUri);
      formData.add("client_id", spotifyAuthProperties.clientId());
      formData.add("client_secret", spotifyAuthProperties.clientSecret());

      // Exchange code for tokens
      var credentials = spotifyOAuthClient.exchangeCodeForToken(formData);

      log.info(
          "Successfully exchanged code for tokens. Expires in: {} seconds",
          credentials.expiresIn());

      // Get user profile to extract user ID and display name
      String authHeader = "Bearer " + credentials.accessToken();
      UserDto userProfile = spotifyUserClient.getCurrentUserProfile(authHeader);

      String spotifyUserId = userProfile.id();
      String displayName = userProfile.displayName();
      log.info("Retrieved Spotify user profile for user ID: {} ({})", spotifyUserId, displayName);

      // Save account
      String accountId =
          spotifyAccountService.saveAccount(spotifyUserId, displayName, credentials.refreshToken());

      log.info("Successfully saved Spotify account with accountId: {}", accountId);
      return accountId;

    } catch (Exception e) {
      log.error("Failed to exchange code for tokens: {}", e.getMessage());
      throw new SpotifyManagementException("Failed to authenticate with Spotify", e);
    }
  }

  /** Exception thrown when Spotify management operations fail. */
  public static class SpotifyManagementException extends RuntimeException {
    public SpotifyManagementException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
