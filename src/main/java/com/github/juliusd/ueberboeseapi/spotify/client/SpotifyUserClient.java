package com.github.juliusd.ueberboeseapi.spotify.client;

import com.github.juliusd.ueberboeseapi.spotify.dto.UserDto;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;

/** HTTP service client for Spotify user operations. */
public interface SpotifyUserClient {

  /**
   * Get the current user's profile.
   *
   * @param authorization Bearer token for authentication
   * @return User profile details
   */
  @GetExchange("/v1/me")
  UserDto getCurrentUserProfile(@RequestHeader("Authorization") String authorization);
}
