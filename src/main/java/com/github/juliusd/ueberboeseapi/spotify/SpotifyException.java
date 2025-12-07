package com.github.juliusd.ueberboeseapi.spotify;

public class SpotifyException extends RuntimeException {
  public SpotifyException(Exception e) {
    super(e);
  }
}
