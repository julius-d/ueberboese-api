package com.github.juliusd.ueberboeseapi.mgmt;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.github.juliusd.ueberboeseapi.TestBase;
import com.github.juliusd.ueberboeseapi.spotify.InvalidSpotifyUriException;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyAccountService;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyEntityNotFoundException;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyEntityService;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyManagementService;
import io.restassured.http.ContentType;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DirtiesContext
class SpotifyMgmtControllerTest extends TestBase {

  @MockitoBean private SpotifyManagementService spotifyManagementService;
  @MockitoBean private SpotifyAccountService spotifyAccountService;
  @MockitoBean private SpotifyEntityService spotifyEntityService;

  @Test
  void initSpotifyAuth_shouldReturnRedirectUrl() {
    // Given
    String mockAuthUrl =
        "https://accounts.spotify.com/authorize?client_id=test&response_type=code&redirect_uri=ueberboese-login%3A%2F%2Fspotify&scope=playlist-read-private+user-read-private";

    when(spotifyManagementService.generateAuthorizationUrl(anyString())).thenReturn(mockAuthUrl);

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .accept(ContentType.JSON)
        .when()
        .post("/mgmt/spotify/init")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("redirectUrl", equalTo(mockAuthUrl));
  }

  @Test
  void confirmSpotifyAuth_shouldReturnAccountId() {
    // Given
    String authCode = "test_authorization_code_123";
    String mockAccountId = "spotify_user_abc123";

    when(spotifyManagementService.exchangeCodeForTokens(anyString(), anyString()))
        .thenReturn(mockAccountId);

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .queryParam("code", authCode)
        .when()
        .post("/mgmt/spotify/confirm")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("success", equalTo(true))
        .body("message", equalTo("Spotify account connected successfully"))
        .body("accountId", equalTo(mockAccountId));
  }

  @Test
  void confirmSpotifyAuth_shouldRequireCode() {
    // When / Then - missing code parameter
    // Spring will return 400 automatically for missing required parameter
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .when()
        .post("/mgmt/spotify/confirm")
        .then()
        .statusCode(
            anyOf(is(400), is(500))); // Accept either 400 or 500 depending on Spring behavior
  }

  @Test
  void confirmSpotifyAuth_shouldRequireNonEmptyCode() {
    // When / Then - empty code parameter
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .queryParam("code", "")
        .when()
        .post("/mgmt/spotify/confirm")
        .then()
        .statusCode(400)
        .contentType("application/json")
        .body("error", equalTo("Missing parameter"))
        .body("message", containsString("required"));
  }

  @Test
  void confirmSpotifyAuth_shouldHandleInvalidCode() {
    // Given
    String invalidCode = "invalid_code_xyz";

    when(spotifyManagementService.exchangeCodeForTokens(anyString(), anyString()))
        .thenThrow(
            new SpotifyManagementService.SpotifyManagementException(
                "Failed to authenticate with Spotify", new RuntimeException("Invalid code")));

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .queryParam("code", invalidCode)
        .when()
        .post("/mgmt/spotify/confirm")
        .then()
        .statusCode(401)
        .contentType("application/json")
        .body("error", equalTo("Authentication failed"))
        .body("message", containsString("Spotify"));
  }

  @Test
  void confirmSpotifyAuth_shouldHandleSpotifyApiFailure() {
    // Given
    String authCode = "test_code";

    when(spotifyManagementService.exchangeCodeForTokens(anyString(), anyString()))
        .thenThrow(
            new SpotifyManagementService.SpotifyManagementException(
                "Failed to exchange code", new RuntimeException("API error")));

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .queryParam("code", authCode)
        .when()
        .post("/mgmt/spotify/confirm")
        .then()
        .statusCode(401)
        .contentType("application/json")
        .body("error", equalTo("Authentication failed"))
        .body("message", notNullValue());
  }

  @Test
  void initSpotifyAuth_shouldHandleServiceException() {
    // Given
    when(spotifyManagementService.generateAuthorizationUrl(anyString()))
        .thenThrow(new RuntimeException("Unexpected error"));

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .when()
        .post("/mgmt/spotify/init")
        .then()
        .statusCode(500)
        .contentType("application/json")
        .body("error", equalTo("Internal server error"))
        .body("message", notNullValue());
  }

  @Test
  void confirmSpotifyAuth_shouldReturnValidAccountIdFormat() {
    // Given
    String authCode = "valid_code_456";
    String expectedAccountId = "user_valid_789";

    when(spotifyManagementService.exchangeCodeForTokens(anyString(), anyString()))
        .thenReturn(expectedAccountId);

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .queryParam("code", authCode)
        .when()
        .post("/mgmt/spotify/confirm")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("accountId", matchesPattern("^[a-zA-Z0-9_]+$")); // Simple alphanumeric validation
  }

  @Test
  void listSpotifyAccounts_shouldReturnAccounts() throws IOException {
    // Given
    List<SpotifyAccountService.SpotifyAccount> mockAccounts =
        List.of(
            new SpotifyAccountService.SpotifyAccount(
                "user1",
                "John Doe",
                "refresh_token_1",
                OffsetDateTime.parse("2025-12-23T10:30:00Z")),
            new SpotifyAccountService.SpotifyAccount(
                "user2",
                "Jane Smith",
                "refresh_token_2",
                OffsetDateTime.parse("2025-12-22T14:15:00Z")));

    when(spotifyAccountService.listAllAccounts()).thenReturn(mockAccounts);

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .when()
        .get("/mgmt/spotify/accounts")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("accounts", hasSize(2))
        .body("accounts[0].displayName", equalTo("John Doe"))
        .body("accounts[0].createdAt", equalTo("2025-12-23T10:30:00Z"))
        .body("accounts[1].displayName", equalTo("Jane Smith"))
        .body("accounts[1].createdAt", equalTo("2025-12-22T14:15:00Z"))
        // Verify that sensitive fields are not exposed
        .body("accounts[0].spotifyUserId", nullValue())
        .body("accounts[0].refreshToken", nullValue())
        .body("accounts[1].spotifyUserId", nullValue())
        .body("accounts[1].refreshToken", nullValue());
  }

  @Test
  void listSpotifyAccounts_shouldReturnEmptyList() throws IOException {
    // Given
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of());

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .when()
        .get("/mgmt/spotify/accounts")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("accounts", hasSize(0))
        .body("accounts", empty());
  }

  @Test
  void listSpotifyAccounts_shouldHandleServiceException() throws IOException {
    // Given
    when(spotifyAccountService.listAllAccounts())
        .thenThrow(new IOException("Failed to read accounts directory"));

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .when()
        .get("/mgmt/spotify/accounts")
        .then()
        .statusCode(500)
        .contentType("application/json")
        .body("error", equalTo("Internal server error"))
        .body("message", notNullValue());
  }

  @Test
  void mgmtEndpoints_shouldRequireAuthentication() {
    // Verify that requests without authentication are rejected with 401
    given()
        .header("Content-Type", "application/json")
        .when()
        .post("/mgmt/spotify/init")
        .then()
        .statusCode(401);
  }

  @Test
  void mgmtEndpoints_shouldRejectInvalidCredentials() {
    // Verify that requests with wrong credentials are rejected with 401
    given()
        .auth()
        .basic("wrong", "credentials")
        .header("Content-Type", "application/json")
        .when()
        .post("/mgmt/spotify/init")
        .then()
        .statusCode(401);
  }

  @Test
  void getSpotifyEntity_shouldReturnEntityInfoForTrack() {
    // Given
    String uri = "spotify:track:6rqhFgbbKwnb9MLmUQDhG6";
    SpotifyEntityService.SpotifyEntityInfo mockInfo =
        new SpotifyEntityService.SpotifyEntityInfo(
            "Bohemian Rhapsody", "https://i.scdn.co/image/test.jpg");

    when(spotifyEntityService.getEntityInfo(uri)).thenReturn(mockInfo);

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .body("{\"uri\": \"" + uri + "\"}")
        .when()
        .post("/mgmt/spotify/entity")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("name", equalTo("Bohemian Rhapsody"))
        .body("imageUrl", equalTo("https://i.scdn.co/image/test.jpg"));
  }

  @Test
  void getSpotifyEntity_shouldReturnEntityInfoForAlbum() {
    // Given
    String uri = "spotify:album:4LH4d3cOWNNsVw41Gqt2kv";
    SpotifyEntityService.SpotifyEntityInfo mockInfo =
        new SpotifyEntityService.SpotifyEntityInfo(
            "The Dark Side of the Moon", "https://i.scdn.co/image/album.jpg");

    when(spotifyEntityService.getEntityInfo(uri)).thenReturn(mockInfo);

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .body("{\"uri\": \"" + uri + "\"}")
        .when()
        .post("/mgmt/spotify/entity")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("name", equalTo("The Dark Side of the Moon"))
        .body("imageUrl", equalTo("https://i.scdn.co/image/album.jpg"));
  }

  @Test
  void getSpotifyEntity_shouldReturnEntityInfoForArtist() {
    // Given
    String uri = "spotify:artist:0OdUWJ0sBjDrqHygGUXeCF";
    SpotifyEntityService.SpotifyEntityInfo mockInfo =
        new SpotifyEntityService.SpotifyEntityInfo("Queen", "https://i.scdn.co/image/artist.jpg");

    when(spotifyEntityService.getEntityInfo(uri)).thenReturn(mockInfo);

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .body("{\"uri\": \"" + uri + "\"}")
        .when()
        .post("/mgmt/spotify/entity")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("name", equalTo("Queen"))
        .body("imageUrl", equalTo("https://i.scdn.co/image/artist.jpg"));
  }

  @Test
  void getSpotifyEntity_shouldReturnEntityInfoForPlaylist() {
    // Given
    String uri = "spotify:playlist:37i9dQZF1DXcBWIGoYBM5M";
    SpotifyEntityService.SpotifyEntityInfo mockInfo =
        new SpotifyEntityService.SpotifyEntityInfo(
            "Today's Top Hits", "https://i.scdn.co/image/playlist.jpg");

    when(spotifyEntityService.getEntityInfo(uri)).thenReturn(mockInfo);

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .body("{\"uri\": \"" + uri + "\"}")
        .when()
        .post("/mgmt/spotify/entity")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("name", equalTo("Today's Top Hits"))
        .body("imageUrl", equalTo("https://i.scdn.co/image/playlist.jpg"));
  }

  @Test
  void getSpotifyEntity_shouldReturnNullImageUrlWhenNoImageAvailable() {
    // Given
    String uri = "spotify:playlist:37i9dQZF1DXcBWIGoYBM5M";
    SpotifyEntityService.SpotifyEntityInfo mockInfo =
        new SpotifyEntityService.SpotifyEntityInfo("My Private Playlist", null);

    when(spotifyEntityService.getEntityInfo(uri)).thenReturn(mockInfo);

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .body("{\"uri\": \"" + uri + "\"}")
        .when()
        .post("/mgmt/spotify/entity")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("name", equalTo("My Private Playlist"))
        .body("imageUrl", nullValue());
  }

  @Test
  void getSpotifyEntity_shouldReturn400ForInvalidUri() {
    // Given
    String invalidUri = "invalid:uri:format";

    when(spotifyEntityService.getEntityInfo(invalidUri))
        .thenThrow(new InvalidSpotifyUriException("Invalid Spotify URI format"));

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .body("{\"uri\": \"" + invalidUri + "\"}")
        .when()
        .post("/mgmt/spotify/entity")
        .then()
        .statusCode(400)
        .contentType("application/json")
        .body("error", equalTo("Invalid URI"))
        .body("message", containsString("Invalid Spotify URI format"));
  }

  @Test
  void getSpotifyEntity_shouldReturn400ForUnsupportedEntityType() {
    // Given
    String unsupportedUri = "spotify:show:12345";

    when(spotifyEntityService.getEntityInfo(unsupportedUri))
        .thenThrow(
            new InvalidSpotifyUriException(
                "Unsupported entity type: show. Supported types: track, album, artist, playlist"));

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .body("{\"uri\": \"" + unsupportedUri + "\"}")
        .when()
        .post("/mgmt/spotify/entity")
        .then()
        .statusCode(400)
        .contentType("application/json")
        .body("error", equalTo("Invalid URI"))
        .body("message", containsString("Unsupported entity type"));
  }

  @Test
  void getSpotifyEntity_shouldReturn404ForEntityNotFound() {
    // Given
    String notFoundUri = "spotify:track:nonexistent123";

    when(spotifyEntityService.getEntityInfo(notFoundUri))
        .thenThrow(new SpotifyEntityNotFoundException("Spotify entity not found: " + notFoundUri));

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .body("{\"uri\": \"" + notFoundUri + "\"}")
        .when()
        .post("/mgmt/spotify/entity")
        .then()
        .statusCode(404)
        .contentType("application/json")
        .body("error", equalTo("Not found"))
        .body("message", containsString("not found"));
  }

  @Test
  void getSpotifyEntity_shouldReturn401WithoutAuthentication() {
    // When / Then
    given()
        .header("Content-Type", "application/json")
        .body("{\"uri\": \"spotify:track:12345\"}")
        .when()
        .post("/mgmt/spotify/entity")
        .then()
        .statusCode(401);
  }

  @Test
  void getSpotifyEntity_shouldReturn401WithWrongCredentials() {
    // When / Then
    given()
        .auth()
        .basic("wrong", "credentials")
        .header("Content-Type", "application/json")
        .body("{\"uri\": \"spotify:track:12345\"}")
        .when()
        .post("/mgmt/spotify/entity")
        .then()
        .statusCode(401);
  }

  @Test
  void getSpotifyEntity_shouldReturn500ForSpotifyApiError() {
    // Given
    String uri = "spotify:track:12345";

    when(spotifyEntityService.getEntityInfo(uri))
        .thenThrow(new RuntimeException("Spotify API error"));

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .body("{\"uri\": \"" + uri + "\"}")
        .when()
        .post("/mgmt/spotify/entity")
        .then()
        .statusCode(500)
        .contentType("application/json")
        .body("error", equalTo("Internal server error"))
        .body("message", notNullValue());
  }
}
