package com.github.juliusd.ueberboeseapi.spotify;

import java.io.IOException;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpotifyManagementService {

  // All required scopes from SpotifyTokenService
  private static final String REQUIRED_SCOPES =
      "playlist-read-private playlist-read-collaborative streaming user-library-read user-library-modify playlist-modify-private playlist-modify-public user-read-email user-read-private user-top-read";

  private final SpotifyApiUrlProperties spotifyHostProperties;
  private final SpotifyAuthProperties spotifyAuthProperties;
  private final SpotifyAccountService spotifyAccountService;

  /**
   * Generates the Spotify authorization URL for OAuth flow initialization.
   *
   * @param redirectUri The redirect URI for the OAuth callback
   * @return The Spotify authorization URL
   */
  public String generateAuthorizationUrl(String redirectUri) {
    log.info("Generating Spotify authorization URL with redirectUri: {}", redirectUri);

    try {
      SpotifyApi spotifyApi =
          new SpotifyApi.Builder()
              .setHost(spotifyHostProperties.host())
              .setScheme(spotifyHostProperties.schema())
              .setPort(spotifyHostProperties.port())
              .setClientId(spotifyAuthProperties.clientId())
              .setClientSecret(spotifyAuthProperties.clientSecret())
              .setRedirectUri(SpotifyHttpManager.makeUri(redirectUri))
              .build();

      AuthorizationCodeUriRequest authorizationCodeUriRequest =
          spotifyApi.authorizationCodeUri().scope(REQUIRED_SCOPES).build();

      URI uri = authorizationCodeUriRequest.execute();
      String authUrl = uri.toString();

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
      SpotifyApi spotifyApi =
          new SpotifyApi.Builder()
              .setHost(spotifyHostProperties.host())
              .setScheme(spotifyHostProperties.schema())
              .setPort(spotifyHostProperties.port())
              .setClientId(spotifyAuthProperties.clientId())
              .setClientSecret(spotifyAuthProperties.clientSecret())
              .setRedirectUri(SpotifyHttpManager.makeUri(redirectUri))
              .build();

      // Exchange code for tokens
      AuthorizationCodeRequest authorizationCodeRequest =
          spotifyApi.authorizationCode(code).build();
      AuthorizationCodeCredentials credentials = authorizationCodeRequest.execute();

      log.info(
          "Successfully exchanged code for tokens. Expires in: {} seconds",
          credentials.getExpiresIn());

      // Get user profile to extract user ID and display name
      spotifyApi.setAccessToken(credentials.getAccessToken());
      GetCurrentUsersProfileRequest profileRequest = spotifyApi.getCurrentUsersProfile().build();
      User userProfile = profileRequest.execute();

      String spotifyUserId = userProfile.getId();
      String displayName = userProfile.getDisplayName();
      log.info("Retrieved Spotify user profile for user ID: {} ({})", spotifyUserId, displayName);

      // Save account
      String accountId =
          spotifyAccountService.saveAccount(
              spotifyUserId, displayName, credentials.getRefreshToken());

      log.info("Successfully saved Spotify account with accountId: {}", accountId);
      return accountId;

    } catch (IOException | SpotifyWebApiException | ParseException e) {
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
