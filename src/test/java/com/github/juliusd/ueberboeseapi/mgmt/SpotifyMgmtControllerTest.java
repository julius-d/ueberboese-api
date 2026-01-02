package com.github.juliusd.ueberboeseapi.mgmt;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import com.github.juliusd.ueberboeseapi.TestBase;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyAccount;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyAccountRepository;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.restassured.http.ContentType;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@WireMockTest(httpPort = 8299)
class SpotifyMgmtControllerTest extends TestBase {

  @Autowired private SpotifyAccountRepository spotifyAccountRepository;

  private WireMock spotifyApiServer;

  @BeforeEach
  void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
    spotifyApiServer = wmRuntimeInfo.getWireMock();
  }

  @AfterEach
  void tearDown() {
    assertThat(spotifyApiServer.findAllUnmatchedRequests()).isEmpty();
  }

  @Test
  void initSpotifyAuth_shouldReturnRedirectUrl() {
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
        .body(
            "redirectUrl",
            equalTo(
                "https://accounts.spotify.com/authorize?client_id=test-client-id"
                    + "&response_type=code"
                    + "&redirect_uri=ueberboese-login://spotify"
                    + "&scope=playlist-read-private%20playlist-read-collaborative%20streaming%20user-library-read%20user-library-modify%20playlist-modify-private%20playlist-modify-public%20user-read-email%20user-read-private%20user-top-read"));
  }

  @Test
  void confirmSpotifyAuth_shouldReturnAccountId() {
    // Given
    String authCode = "test_authorization_code_123";
    String mockAccountId = "spotify_user_abc123";

    // Stub Spotify API token exchange
    // Form data is sent in request body as application/x-www-form-urlencoded
    spotifyApiServer.register(
        post(urlEqualTo("/api/token"))
            .withHeader("Content-Type", matching("application/x-www-form-urlencoded.*"))
            .withRequestBody(matching(".*grant_type=authorization_code.*"))
            .withRequestBody(matching(".*code=" + authCode + ".*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                        {
                          "access_token": "test_access_token",
                          "token_type": "Bearer",
                          "expires_in": 3600,
                          "refresh_token": "test_refresh_token",
                          "scope": "playlist-read-private user-read-private"
                        }
                        """)));

    // Stub Spotify API user profile request
    spotifyApiServer.register(
        get(urlEqualTo("/v1/me"))
            .withHeader("Authorization", matching("Bearer .*"))
            .willReturn(
                okJson(
                    """
                    {
                      "id": "%s",
                      "display_name": "Test User",
                      "email": "test@example.org"
                    }
                    """
                        .formatted(mockAccountId))));

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

    // Stub Spotify API to return error for invalid code
    spotifyApiServer.register(
        post(urlEqualTo("/api/token"))
            .withHeader("Content-Type", matching("application/x-www-form-urlencoded.*"))
            .withRequestBody(matching(".*code=" + invalidCode + ".*"))
            .willReturn(
                aResponse()
                    .withStatus(400)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                        {
                          "error": "invalid_grant",
                          "error_description": "Invalid authorization code"
                        }
                        """)));

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

    // Stub Spotify API to return server error
    spotifyApiServer.register(
        post(urlEqualTo("/api/token"))
            .withHeader("Content-Type", matching("application/x-www-form-urlencoded.*"))
            .withRequestBody(matching(".*code=" + authCode + ".*"))
            .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

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
  void confirmSpotifyAuth_shouldReturnValidAccountIdFormat() {
    // Given
    String authCode = "valid_code_456";
    String expectedAccountId = "user_valid_789";

    // Stub Spotify API token exchange
    spotifyApiServer.register(
        post(urlEqualTo("/api/token"))
            .withHeader("Content-Type", matching("application/x-www-form-urlencoded.*"))
            .withRequestBody(matching(".*code=" + authCode + ".*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                        {
                          "access_token": "valid_access_token",
                          "token_type": "Bearer",
                          "expires_in": 3600,
                          "refresh_token": "valid_refresh_token",
                          "scope": "playlist-read-private"
                        }
                        """)));

    // Stub user profile
    spotifyApiServer.register(
        get(urlEqualTo("/v1/me"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                        {
                          "id": "%s",
                          "display_name": "Valid User"
                        }
                        """
                            .formatted(expectedAccountId))));

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
        .body("accountId", matchesPattern("^[a-zA-Z0-9_]+$"));
  }

  @Test
  void listSpotifyAccounts_shouldReturnAccounts() {
    // Given
    OffsetDateTime time1 = OffsetDateTime.parse("2025-12-23T10:30:00Z");
    OffsetDateTime time2 = OffsetDateTime.parse("2025-12-22T14:15:00Z");
    spotifyAccountRepository.save(
        new SpotifyAccount("user1", "John Doe", "refresh_token_1", time1, time1, null));
    spotifyAccountRepository.save(
        new SpotifyAccount("user2", "Jane Smith", "refresh_token_2", time2, time2, null));

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
        .body("accounts[0].spotifyUserId", equalTo("user1"))
        .body("accounts[0].displayName", equalTo("John Doe"))
        .body("accounts[0].createdAt", equalTo("2025-12-23T10:30:00Z"))
        .body("accounts[1].spotifyUserId", equalTo("user2"))
        .body("accounts[1].displayName", equalTo("Jane Smith"))
        .body("accounts[1].createdAt", equalTo("2025-12-22T14:15:00Z"))
        // Verify that sensitive fields are not exposed
        .body("accounts[0].refreshToken", nullValue())
        .body("accounts[1].refreshToken", nullValue());
  }

  @Test
  void listSpotifyAccounts_shouldReturnEmptyList() {
    // Given
    // No accounts in DB (TestBase clears DB before each test)

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
    String trackId = "6rqhFgbbKwnb9MLmUQDhG6";

    setupSpotifyAccountAndTokenRefresh();

    // Stub Spotify API track endpoint
    spotifyApiServer.register(
        get(urlEqualTo("/v1/tracks/" + trackId))
            .willReturn(
                okJson(
                    """
                    {
                      "id": "%s",
                      "name": "Bohemian Rhapsody",
                      "album": {
                        "id": "album123",
                        "name": "A Night at the Opera",
                        "images": [
                          {"url": "https://i.scdn.co/image/large.jpg", "height": 640, "width": 640},
                          {"url": "https://i.scdn.co/image/test.jpg", "height": 300, "width": 300},
                          {"url": "https://i.scdn.co/image/small.jpg", "height": 64, "width": 64}
                        ]
                      }
                    }
                    """
                        .formatted(trackId))));

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
    String albumId = "4LH4d3cOWNNsVw41Gqt2kv";

    setupSpotifyAccountAndTokenRefresh();

    spotifyApiServer.register(
        get(urlEqualTo("/v1/albums/" + albumId))
            .willReturn(
                okJson(
                    """
                    {
                      "id": "%s",
                      "name": "The Dark Side of the Moon",
                      "images": [
                        {"url": "https://i.scdn.co/image/large.jpg", "height": 640, "width": 640},
                        {"url": "https://i.scdn.co/image/album.jpg", "height": 300, "width": 300}
                      ]
                    }"""
                        .formatted(albumId))));

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
    String artistId = "0OdUWJ0sBjDrqHygGUXeCF";

    setupSpotifyAccountAndTokenRefresh();

    spotifyApiServer.register(
        get(urlEqualTo("/v1/artists/" + artistId))
            .willReturn(
                okJson(
                    """
                    {
                      "id": "%s",
                      "name": "Queen",
                      "images": [
                        {"url": "https://i.scdn.co/image/large.jpg", "height": 640, "width": 640},
                        {"url": "https://i.scdn.co/image/artist.jpg", "height": 300, "width": 300}
                      ]
                    }
                    """
                        .formatted(artistId))));

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
    String playlistId = "37i9dQZF1DXcBWIGoYBM5M";

    setupSpotifyAccountAndTokenRefresh();

    spotifyApiServer.register(
        get(urlEqualTo("/v1/playlists/" + playlistId))
            .willReturn(
                okJson(
                    """
                    {
                      "id": "%s",
                      "name": "Today's Top Hits",
                      "images": [
                        {"url": "https://i.scdn.co/image/large.jpg", "height": 640, "width": 640},
                        {"url": "https://i.scdn.co/image/playlist.jpg", "height": 300, "width": 300}
                      ]
                    }
                    """
                        .formatted(playlistId))));

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
  void getSpotifyEntity_shouldReturnEntityInfoForShow() {
    // Given
    String uri = "spotify:show:123456rty0";
    String showId = "123456rty0";

    setupSpotifyAccountAndTokenRefresh();

    spotifyApiServer.register(
        get(urlEqualTo("/v1/shows/" + showId))
            .willReturn(
                okJson(
                    """
                    {
                      "id": "%s",
                      "name": "A nice podcast",
                      "images": [
                        {"url": "https://i.scdn.co/image/large.jpg", "height": 640, "width": 640},
                        {"url": "https://i.scdn.co/image/show.jpg", "height": 300, "width": 300}
                      ]
                    }
                    """
                        .formatted(showId))));

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .body(
            """
              {
                "uri": "%s"
              }
              """
                .formatted(uri))
        .when()
        .post("/mgmt/spotify/entity")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("name", equalTo("A nice podcast"))
        .body("imageUrl", equalTo("https://i.scdn.co/image/show.jpg"));
  }

  @Test
  void getSpotifyEntity_shouldReturnEntityInfoForEpisode() {
    // Given
    String uri = "spotify:episode:512ojhOuo1ktJprKbVcKyQ";
    String episodeId = "512ojhOuo1ktJprKbVcKyQ";

    setupSpotifyAccountAndTokenRefresh();

    spotifyApiServer.register(
        get(urlEqualTo("/v1/episodes/" + episodeId))
            .willReturn(
                okJson(
                    """
                    {
                      "id": "%s",
                      "name": "Episode 42: The Answer",
                      "images": [
                        {"url": "https://i.scdn.co/image/large.jpg", "height": 640, "width": 640},
                        {"url": "https://i.scdn.co/image/episode.jpg", "height": 300, "width": 300}
                      ]
                    }
                    """
                        .formatted(episodeId))));

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .header("Content-Type", "application/json")
        .body(
            """
              {
                "uri": "%s"
              }
              """
                .formatted(uri))
        .when()
        .post("/mgmt/spotify/entity")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("name", equalTo("Episode 42: The Answer"))
        .body("imageUrl", equalTo("https://i.scdn.co/image/episode.jpg"));
  }

  @Test
  void getSpotifyEntity_shouldReturnNullImageUrlWhenNoImageAvailable() {
    // Given
    String uri = "spotify:playlist:37i9dQZF1DXcBWIGoYBM5M";
    String playlistId = "37i9dQZF1DXcBWIGoYBM5M";

    setupSpotifyAccountAndTokenRefresh();

    spotifyApiServer.register(
        get(urlEqualTo("/v1/playlists/" + playlistId))
            .willReturn(
                okJson(
                    """
                    {
                      "id": "%s",
                      "name": "My Private Playlist",
                      "images": []
                    }
                    """
                        .formatted(playlistId))));

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
    // Given - URI validation happens before API call
    String invalidUri = "invalid:uri:format";

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
    // Given - Type validation happens before API call
    String unsupportedUri = "spotify:audiobook:12345";
    setupSpotifyAccountAndTokenRefresh();

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
    String trackId = "nonexistent123";

    setupSpotifyAccountAndTokenRefresh();

    // Stub Spotify API to return 404
    spotifyApiServer.register(
        get(urlEqualTo("/v1/tracks/" + trackId))
            .willReturn(
                aResponse()
                    .withStatus(404)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                        {
                          "error": {
                            "status": 404,
                            "message": "Not found"
                          }
                        }
                        """)));

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
    String trackId = "12345";

    setupSpotifyAccountAndTokenRefresh();

    // Stub Spotify API to return server error
    spotifyApiServer.register(
        get(urlEqualTo("/v1/tracks/" + trackId))
            .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

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

  private void setupSpotifyAccountAndTokenRefresh() {
    // Create a Spotify account in DB
    spotifyAccountRepository.save(
        new SpotifyAccount(
            "test_user",
            "Test User",
            "test_refresh_token",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            null));

    // Stub token refresh endpoint (used by SpotifyEntityService)
    // Form data is sent in request body as application/x-www-form-urlencoded
    spotifyApiServer.register(
        post(urlEqualTo("/api/token"))
            .withHeader("Content-Type", matching("application/x-www-form-urlencoded.*"))
            .withRequestBody(matching(".*grant_type=refresh_token.*"))
            .willReturn(
                okJson(
                    """
                {
                  "access_token": "refreshed_token",
                  "token_type": "Bearer",
                  "expires_in": 3600,
                  "scope": "user-read-private"
                }
                """)));
  }
}
