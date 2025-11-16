package com.github.juliusd.ueberboeseapi;

import com.github.juliusd.ueberboeseapi.generated.OauthApi;
import com.github.juliusd.ueberboeseapi.generated.dtos.OAuthTokenRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.OAuthTokenResponseApiDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "ueberboese.oauth.enabled", havingValue = "true")
public class UeberboeseOauthController implements OauthApi {
  @Override
  public ResponseEntity<OAuthTokenResponseApiDto> refreshOAuthToken(
      String deviceId,
      String providerId,
      String tokenType,
      OAuthTokenRequestApiDto oauthTokenRequestApiDto) {

    // Create the response object with example values from the captured proxy log
    OAuthTokenResponseApiDto response = new OAuthTokenResponseApiDto();
    response.setAccessToken("123fooAccessExampleToken");
    response.setTokenType("Bearer");
    response.setExpiresIn(3600);
    response.setScope(
        "playlist-read-private playlist-read-collaborative streaming user-library-read user-library-modify playlist-modify-private playlist-modify-public user-read-email user-read-private user-top-read");

    return ResponseEntity.ok().header("Content-Type", "application/json").body(response);
  }
}
