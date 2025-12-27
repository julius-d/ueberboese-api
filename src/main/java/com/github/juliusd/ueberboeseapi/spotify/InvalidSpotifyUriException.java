package com.github.juliusd.ueberboeseapi.spotify;

public class InvalidSpotifyUriException extends RuntimeException {
  public InvalidSpotifyUriException(String message) {
    super(message);
  }

  public InvalidSpotifyUriException(String message, Throwable cause) {
    super(message, cause);
  }
}
