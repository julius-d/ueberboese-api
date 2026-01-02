package com.github.juliusd.ueberboeseapi.spotify.client;

import com.github.juliusd.ueberboeseapi.spotify.dto.AuthorizationCodeCredentialsDto;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

/** HTTP service client for Spotify OAuth operations. */
public interface SpotifyOAuthClient {

  /**
   * Exchange authorization code for access and refresh tokens.
   *
   * @param formData Form data containing grant_type, code, redirect_uri, client_id, client_secret
   * @return Token response containing access token, refresh token, and metadata
   */
  @PostExchange(url = "/api/token", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  AuthorizationCodeCredentialsDto exchangeCodeForToken(
      @RequestBody MultiValueMap<String, String> formData);

  /**
   * Refresh an access token using a refresh token.
   *
   * @param formData Form data containing grant_type, refresh_token, client_id, client_secret
   * @return Token response containing new access token and metadata
   */
  @PostExchange(url = "/api/token", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  AuthorizationCodeCredentialsDto refreshAccessToken(
      @RequestBody MultiValueMap<String, String> formData);
}
