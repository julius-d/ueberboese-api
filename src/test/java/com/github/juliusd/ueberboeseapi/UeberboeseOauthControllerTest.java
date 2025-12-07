package com.github.juliusd.ueberboeseapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.juliusd.ueberboeseapi.spotify.SpotifyTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

class UeberboeseOauthControllerTest extends TestBase {

  @MockBean private SpotifyTokenService spotifyTokenService;

  @BeforeEach
  void setUpMocks() {
    // Create a mock AuthorizationCodeCredentials response
    AuthorizationCodeCredentials mockCredentials =
        new AuthorizationCodeCredentials.Builder()
            .setAccessToken("123fooAccessExampleToken")
            .setTokenType("Bearer")
            .setExpiresIn(3600)
            .setScope(
                "playlist-read-private playlist-read-collaborative streaming user-library-read user-library-modify playlist-modify-private playlist-modify-public user-read-email user-read-private user-top-read")
            .build();

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
        .body("access_token", equalTo("123fooAccessExampleToken"))
        .body("token_type", equalTo("Bearer"))
        .body("expires_in", equalTo(3600));
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

    // Test different device IDs
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

    // Test different provider IDs
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

    // Test different token types
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
}
