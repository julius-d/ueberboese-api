package com.github.juliusd.ueberboeseapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.juliusd.ueberboeseapi.generated.OauthApi;
import com.github.juliusd.ueberboeseapi.generated.dtos.OAuthTokenRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.OAuthTokenResponseApiDto;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "ueberboese.oauth.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class UeberboeseOauthController implements OauthApi {

  private static final String SPOTIFY_PROVIDER_ID = String.valueOf(SourceProvider.SPOTIFY.getId());

  private final SpotifyTokenService spotifyTokenService;
  private final ProxyService proxyService;
  private final HttpServletRequest request;
  private final ObjectMapper objectMapper;

  @Override
  public ResponseEntity<OAuthTokenResponseApiDto> refreshOAuthToken(
      String deviceId,
      String providerId,
      String tokenType,
      OAuthTokenRequestApiDto oauthTokenRequestApiDto) {
    log.info(
        "refreshOAuthToken request: deviceId={}, providerId={}, tokenType={}",
        deviceId,
        providerId,
        tokenType);

    // Route to Spotify if provider ID is 15
    if (SPOTIFY_PROVIDER_ID.equals(providerId)) {
      log.debug("Routing to Spotify authentication for providerId: {}", providerId);
      return handleSpotifyAuth(oauthTokenRequestApiDto);
    }

    // For all other providers, proxy to auth target
    log.debug("Routing to proxy service for providerId: {}", providerId);
    return handleProxyAuth(oauthTokenRequestApiDto);
  }

  private ResponseEntity<OAuthTokenResponseApiDto> handleSpotifyAuth(
      OAuthTokenRequestApiDto oauthTokenRequestApiDto) {
    var authorizationCodeCredentials = spotifyTokenService.loadSpotifyAuth(oauthTokenRequestApiDto);

    OAuthTokenResponseApiDto response = new OAuthTokenResponseApiDto();
    response.setAccessToken(authorizationCodeCredentials.getAccessToken());
    response.setTokenType(authorizationCodeCredentials.getTokenType());
    response.setExpiresIn(authorizationCodeCredentials.getExpiresIn());
    response.setScope(authorizationCodeCredentials.getScope());

    return ResponseEntity.ok().header("Content-Type", "application/json").body(response);
  }

  private ResponseEntity<OAuthTokenResponseApiDto> handleProxyAuth(
      OAuthTokenRequestApiDto oauthTokenRequestApiDto) {
    // Convert DTO to JSON string for proxy
    String requestBodyJson;
    try {
      requestBodyJson = objectMapper.writeValueAsString(oauthTokenRequestApiDto);
    } catch (Exception e) {
      log.error("Failed to serialize request body for proxying", e);
      return ResponseEntity.status(500).build();
    }

    // Forward the request to the auth target host via ProxyService
    ResponseEntity<byte[]> proxyResponse = proxyService.forwardRequest(request, requestBodyJson);

    // Check if proxy succeeded
    if (!proxyResponse.getStatusCode().is2xxSuccessful()) {
      log.warn("Proxy request failed with status: {}", proxyResponse.getStatusCode().value());
      return ResponseEntity.status(proxyResponse.getStatusCode()).build();
    }

    // Try to parse the proxy response as OAuthTokenResponseApiDto
    try {
      byte[] responseBody = proxyResponse.getBody();
      if (responseBody != null) {
        OAuthTokenResponseApiDto response =
            objectMapper.readValue(responseBody, OAuthTokenResponseApiDto.class);
        return ResponseEntity.status(proxyResponse.getStatusCode())
            .headers(proxyResponse.getHeaders())
            .body(response);
      }
    } catch (Exception e) {
      log.error("Failed to parse proxy response for non-Spotify provider", e);
      return ResponseEntity.status(502).build();
    }

    log.error("Proxy response body was null");
    return ResponseEntity.status(502).build();
  }
}
