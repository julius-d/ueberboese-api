package com.github.juliusd.ueberboeseapi.spotify;

public class SpotifyEntityNotFoundException extends RuntimeException {
  public SpotifyEntityNotFoundException(String message) {
    super(message);
  }

  public SpotifyEntityNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
