package com.github.juliusd.ueberboeseapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.juliusd.ueberboeseapi.spotify.NoSpotifyAccountException;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyTokenService;
import com.github.juliusd.ueberboeseapi.spotify.dto.AuthorizationCodeCredentialsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DirtiesContext
class UeberboeseOauthControllerTest extends TestBase {

  @MockitoBean private SpotifyTokenService spotifyTokenService;
  @MockitoBean private ProxyService proxyService;

  @BeforeEach
  void setUpMocks() {
    // Create a mock AuthorizationCodeCredentials response
    AuthorizationCodeCredentialsDto mockCredentials =
        new AuthorizationCodeCredentialsDto(
            "123fooAccessExampleToken",
            "Bearer",
            3600,
            null,
            "playlist-read-private playlist-read-collaborative streaming user-library-read user-library-modify playlist-modify-private playlist-modify-public user-read-email user-read-private user-top-read");

    // Mock the service to return our mock credentials
    when(spotifyTokenService.loadSpotifyAuth(any())).thenReturn(mockCredentials);
  }

  @Test
  void refreshOAuthToken_shouldRefreshToken() {
    // language=JSON
    String requestJson =
        """
       {
         "code": "",
         "grant_type": "refresh_token",
         "redirect_uri": "",
         "refresh_token": "AQC-x0O-W2aVur-OIA"
       }""";

    given()
        .header("Accept", "*/*")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "foo/bat+123+312+312+123")
        .header("Content-type", "application/json")
        .body(requestJson)
        .when()
        .post("/oauth/device/587A628A4042/music/musicprovider/15/token/cs3")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("access_token", equalTo("123fooAccessExampleToken"))
        .body("token_type", equalTo("Bearer"))
        .body("expires_in", equalTo(3600))
        .body(
            "scope",
            equalTo(
                "playlist-read-private playlist-read-collaborative streaming user-library-read user-library-modify playlist-modify-private playlist-modify-public user-read-email user-read-private user-top-read"));
  }

  @Test
  void refreshOAuthToken_shouldHandleDifferentProviders() {
    // language=JSON
    String requestJson =
        """
       {
         "code": "",
         "grant_type": "refresh_token",
         "redirect_uri": "",
         "refresh_token": "different-refresh-token"
       }""";

    // Mock proxy response for non-Spotify provider (ID 20)
    String proxyResponseJson =
        """
        {
          "access_token": "proxied-access-token",
          "token_type": "Bearer",
          "expires_in": 7200,
          "scope": "custom-scope"
        }""";

    when(proxyService.forwardRequest(any(), anyString()))
        .thenReturn(
            ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(proxyResponseJson.getBytes()));

    given()
        .header("Accept", "*/*")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "test-auth-token")
        .header("Content-type", "application/json")
        .body(requestJson)
        .when()
        .post("/oauth/device/TESTDEVICE123/music/musicprovider/20/token/other")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("access_token", equalTo("proxied-access-token"))
        .body("token_type", equalTo("Bearer"))
        .body("expires_in", equalTo(7200));

    // Verify proxy was called
    verify(proxyService).forwardRequest(any(), anyString());
    // Verify Spotify service was NOT called
    verify(spotifyTokenService, never()).loadSpotifyAuth(any());
  }

  @Test
  void refreshOAuthToken_shouldValidateRequestBody() {
    // Test with invalid JSON (malformed JSON body)
    String invalidRequestJson = "invalid json content";

    given()
        .header("Accept", "*/*")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "test-token")
        .header("Content-type", "application/json")
        .body(invalidRequestJson)
        .when()
        .post("/oauth/device/587A628A4042/music/musicprovider/15/token/cs3")
        .then()
        .statusCode(400);
  }

  @Test
  void refreshOAuthToken_shouldRequireGrantType() {
    // Test with missing required grant_type field
    String requestJson =
        """
       {
         "code": "",
         "redirect_uri": "",
         "refresh_token": "AQC-x0O-W2aVur-OIA"
       }""";

    given()
        .header("Accept", "*/*")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "test-token")
        .header("Content-type", "application/json")
        .body(requestJson)
        .when()
        .post("/oauth/device/587A628A4042/music/musicprovider/15/token/cs3")
        .then()
        .statusCode(400);
  }

  @Test
  void refreshOAuthToken_shouldRequireRefreshToken() {
    // Test with missing required refresh_token field
    String requestJson =
        """
       {
         "code": "",
         "grant_type": "refresh_token",
         "redirect_uri": ""
       }""";

    given()
        .header("Accept", "*/*")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "test-token")
        .header("Content-type", "application/json")
        .body(requestJson)
        .when()
        .post("/oauth/device/587A628A4042/music/musicprovider/15/token/cs3")
        .then()
        .statusCode(400);
  }

  @Test
  void refreshOAuthToken_shouldUseSpotifyForProvider15() {
    // language=JSON
    String requestJson =
        """
       {
         "code": "",
         "grant_type": "refresh_token",
         "redirect_uri": "",
         "refresh_token": "spotify-refresh-token"
       }""";

    given()
        .header("Accept", "*/*")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "test-token")
        .header("Content-type", "application/json")
        .body(requestJson)
        .when()
        .post("/oauth/device/DEVICE123/music/musicprovider/15/token/cs3")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("access_token", equalTo("123fooAccessExampleToken"));

    // Verify Spotify service was called
    verify(spotifyTokenService).loadSpotifyAuth(any());
    // Verify proxy was NOT called
    verify(proxyService, never()).forwardRequest(any(), anyString());
  }

  @Test
  void refreshOAuthToken_shouldProxyNonSpotifyProviders() {
    // language=JSON
    String requestJson =
        """
       {
         "code": "",
         "grant_type": "refresh_token",
         "redirect_uri": "",
         "refresh_token": "non-spotify-token"
       }""";

    // Mock proxy response
    String proxyResponseJson =
        """
        {
          "access_token": "non-spotify-proxied-token",
          "token_type": "Bearer",
          "expires_in": 7200
        }""";

    when(proxyService.forwardRequest(any(), anyString()))
        .thenReturn(
            ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(proxyResponseJson.getBytes()));

    given()
        .header("Accept", "*/*")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "test-token")
        .header("Content-type", "application/json")
        .body(requestJson)
        .when()
        .post("/oauth/device/TESTDEVICE456/music/musicprovider/25/token/other")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("access_token", equalTo("non-spotify-proxied-token"))
        .body("token_type", equalTo("Bearer"))
        .body("expires_in", equalTo(7200));

    // Verify proxy was called
    verify(proxyService).forwardRequest(any(), anyString());
    // Verify Spotify service was NOT called
    verify(spotifyTokenService, never()).loadSpotifyAuth(any());
  }

  @Test
  void refreshOAuthToken_shouldValidatePathParameters() {
    // Test various path parameter combinations
    String requestJson =
        """
       {
         "code": "",
         "grant_type": "refresh_token",
         "redirect_uri": "",
         "refresh_token": "AQC-x0O-W2aVur-OIA"
       }""";

    // Mock proxy response for non-Spotify providers
    String proxyResponseJson =
        """
        {
          "access_token": "test-token",
          "token_type": "Bearer",
          "expires_in": 3600
        }""";

    when(proxyService.forwardRequest(any(), anyString()))
        .thenReturn(
            ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(proxyResponseJson.getBytes()));

    // Test different device IDs with Spotify (provider 15)
    given()
        .header("Accept", "*/*")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "test-token")
        .header("Content-type", "application/json")
        .body(requestJson)
        .when()
        .post("/oauth/device/DIFFERENT_DEVICE/music/musicprovider/15/token/cs3")
        .then()
        .statusCode(200);

    // Test different provider IDs (25 = non-Spotify, will be proxied)
    given()
        .header("Accept", "*/*")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "test-token")
        .header("Content-type", "application/json")
        .body(requestJson)
        .when()
        .post("/oauth/device/587A628A4042/music/musicprovider/25/token/cs3")
        .then()
        .statusCode(200);

    // Test different token types with Spotify (provider 15)
    given()
        .header("Accept", "*/*")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "test-token")
        .header("Content-type", "application/json")
        .body(requestJson)
        .when()
        .post("/oauth/device/587A628A4042/music/musicprovider/15/token/different")
        .then()
        .statusCode(200);
  }

  @Test
  void refreshOAuthToken_shouldThrowErrorWhenNoAccountsConnected() {
    // Mock SpotifyTokenService to throw NoSpotifyAccountException
    when(spotifyTokenService.loadSpotifyAuth(any()))
        .thenThrow(
            new NoSpotifyAccountException(
                "No Spotify accounts connected. Please connect a Spotify account via the management API."));

    // language=JSON
    String requestJson =
        """
       {
         "code": "",
         "grant_type": "refresh_token",
         "redirect_uri": "",
         "refresh_token": "some-refresh-token"
       }""";

    given()
        .header("Accept", "*/*")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "test-token")
        .header("Content-type", "application/json")
        .body(requestJson)
        .when()
        .post("/oauth/device/587A628A4042/music/musicprovider/15/token/cs3")
        .then()
        .statusCode(500); // SpotifyException results in 500 error

    // Verify Spotify service was called
    verify(spotifyTokenService).loadSpotifyAuth(any());
  }
}
