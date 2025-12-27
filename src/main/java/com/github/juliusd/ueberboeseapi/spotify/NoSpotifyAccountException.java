package com.github.juliusd.ueberboeseapi.spotify;

/**
 * Exception thrown when no Spotify accounts have been connected but a Spotify authentication is
 * required.
 */
public class NoSpotifyAccountException extends SpotifyException {
  public NoSpotifyAccountException(String message) {
    super(new RuntimeException(message));
  }
}
