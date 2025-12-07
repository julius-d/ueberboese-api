package com.github.juliusd.ueberboeseapi;

import com.github.juliusd.ueberboeseapi.generated.OauthApi;
import com.github.juliusd.ueberboeseapi.generated.dtos.OAuthTokenRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.OAuthTokenResponseApiDto;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "ueberboese.oauth.enabled", havingValue = "true")
@RequiredArgsConstructor
public class UeberboeseOauthController implements OauthApi {

  private final SpotifyTokenService spotifyTokenService;

  @Override
  public ResponseEntity<OAuthTokenResponseApiDto> refreshOAuthToken(
      String deviceId,
      String providerId,
      String tokenType,
      OAuthTokenRequestApiDto oauthTokenRequestApiDto) {

    var authorizationCodeCredentials = spotifyTokenService.loadSpotifyAuth(oauthTokenRequestApiDto);

    // Create the response object with example values from the captured proxy log
    OAuthTokenResponseApiDto response = new OAuthTokenResponseApiDto();
    response.setAccessToken(authorizationCodeCredentials.getAccessToken());
    response.setTokenType(authorizationCodeCredentials.getTokenType());
    response.setExpiresIn(authorizationCodeCredentials.getExpiresIn());
    response.setScope(authorizationCodeCredentials.getScope());

    return ResponseEntity.ok().header("Content-Type", "application/json").body(response);
  }
}
