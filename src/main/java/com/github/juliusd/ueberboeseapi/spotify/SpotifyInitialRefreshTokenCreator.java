package com.github.juliusd.ueberboeseapi.spotify;

import java.io.IOException;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

public class SpotifyInitialRefreshTokenCreator {
  private static final String CLIENT_ID = "TODO";
  private static final String CLIENT_SECRET = "TODO";
  private static final String CODE_FROM_URL = "TODO";

  private final SpotifyApi spotifyApi;

  public SpotifyInitialRefreshTokenCreator(SpotifyApi spotifyApi) {
    this.spotifyApi = spotifyApi;
  }

  public void authorizationCodeUri() {
    AuthorizationCodeUriRequest authorizationCodeUriRequest =
        spotifyApi
            .authorizationCodeUri()
            .scope(
                "playlist-read-private playlist-read-collaborative streaming user-library-read user-library-modify playlist-modify-private playlist-modify-public user-read-email user-read-private user-top-read")
            .build();
    var uri = authorizationCodeUriRequest.execute();

    System.out.println("URI: " + uri.toString());
  }

  public void createAccessToken(String code)
      throws IOException, ParseException, SpotifyWebApiException {
    var authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
    var authorizationCodeCredentials = authorizationCodeRequest.execute();
    System.out.println("RefreshToken: " + authorizationCodeCredentials.getRefreshToken());
  }

  public static void main(String[] args)
      throws IOException, ParseException, SpotifyWebApiException {
    var spotifyApi =
        new SpotifyApi.Builder()
            .setClientId(CLIENT_ID)
            .setClientSecret(CLIENT_SECRET)
            .setRedirectUri(SpotifyHttpManager.makeUri("http://127.0.0.1:8080"))
            .build();

    var spotifyAuthorization = new SpotifyInitialRefreshTokenCreator(spotifyApi);
    // First this
    //    spotifyAuthorization.authorizationCodeUri();
    // Second run this:
    spotifyAuthorization.createAccessToken(CODE_FROM_URL);
  }
}
