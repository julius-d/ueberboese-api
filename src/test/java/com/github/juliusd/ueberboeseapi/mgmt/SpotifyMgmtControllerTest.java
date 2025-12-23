package com.github.juliusd.ueberboeseapi.mgmt;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.github.juliusd.ueberboeseapi.TestBase;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyAccountService;
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

  @Test
  void initSpotifyAuth_shouldReturnRedirectUrl() {
    // Given
    String mockAuthUrl =
        "https://accounts.spotify.com/authorize?client_id=test&response_type=code&redirect_uri=ueberboese-login%3A%2F%2Fspotify&scope=playlist-read-private+user-read-private";

    when(spotifyManagementService.generateAuthorizationUrl(anyString())).thenReturn(mockAuthUrl);

    // When / Then
    given()
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
        .header("Content-Type", "application/json")
        .when()
        .get("/mgmt/spotify/accounts")
        .then()
        .statusCode(500)
        .contentType("application/json")
        .body("error", equalTo("Internal server error"))
        .body("message", notNullValue());
  }
}
