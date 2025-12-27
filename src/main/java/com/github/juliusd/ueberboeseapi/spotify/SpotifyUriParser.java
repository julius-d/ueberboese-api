package com.github.juliusd.ueberboeseapi.spotify;

import org.springframework.stereotype.Component;

@Component
public class SpotifyUriParser {

  /**
   * Parses a Spotify URI into its type and ID components.
   *
   * @param uri the Spotify URI to parse (e.g., "spotify:track:123" or
   *     "spotify:user:userId:playlist:playlistId")
   * @return a SpotifyUri record containing the type and ID
   * @throws InvalidSpotifyUriException if the URI is invalid
   */
  public SpotifyUri parseUri(String uri) {
    if (uri == null || uri.isBlank()) {
      throw new InvalidSpotifyUriException("URI cannot be null or empty");
    }

    // Expected format: spotify:{type}:{id}
    // Or legacy playlist format: spotify:user:{userId}:playlist:{playlistId}
    String[] parts = uri.split(":");

    if (parts.length < 3 || !parts[0].equals("spotify")) {
      throw new InvalidSpotifyUriException("Invalid Spotify URI format. Expected: spotify:type:id");
    }

    // Handle legacy playlist format: spotify:user:{userId}:playlist:{playlistId}
    if (parts.length == 5 && parts[1].equals("user") && parts[3].equals("playlist")) {
      return new SpotifyUri("playlist", parts[4]);
    }

    // Standard format: spotify:{type}:{id}
    if (parts.length == 3) {
      return new SpotifyUri(parts[1], parts[2]);
    }

    throw new InvalidSpotifyUriException(
        "Invalid Spotify URI format. Expected: spotify:type:id or spotify:user:userId:playlist:playlistId");
  }

  public record SpotifyUri(String type, String id) {}
}
