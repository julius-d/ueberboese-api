package com.github.juliusd.ueberboeseapi.spotify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpotifyUriParserTest {

  private SpotifyUriParser spotifyUriParser;

  @BeforeEach
  void setUp() {
    spotifyUriParser = new SpotifyUriParser();
  }

  @Test
  void parseUri_shouldThrowExceptionForNullUri() {
    assertThatThrownBy(() -> spotifyUriParser.parseUri(null))
        .isInstanceOf(InvalidSpotifyUriException.class)
        .hasMessageContaining("URI cannot be null or empty");
  }

  @Test
  void parseUri_shouldThrowExceptionForEmptyUri() {
    assertThatThrownBy(() -> spotifyUriParser.parseUri(""))
        .isInstanceOf(InvalidSpotifyUriException.class)
        .hasMessageContaining("URI cannot be null or empty");
  }

  @Test
  void parseUri_shouldThrowExceptionForBlankUri() {
    assertThatThrownBy(() -> spotifyUriParser.parseUri("   "))
        .isInstanceOf(InvalidSpotifyUriException.class)
        .hasMessageContaining("URI cannot be null or empty");
  }

  @Test
  void parseUri_shouldThrowExceptionForInvalidFormat() {
    assertThatThrownBy(() -> spotifyUriParser.parseUri("invalid:uri"))
        .isInstanceOf(InvalidSpotifyUriException.class)
        .hasMessageContaining("Invalid Spotify URI format");
  }

  @Test
  void parseUri_shouldThrowExceptionForMissingSpotifyPrefix() {
    assertThatThrownBy(() -> spotifyUriParser.parseUri("track:123456"))
        .isInstanceOf(InvalidSpotifyUriException.class)
        .hasMessageContaining("Invalid Spotify URI format");
  }

  @Test
  void parseUri_shouldThrowExceptionForInsufficientParts() {
    assertThatThrownBy(() -> spotifyUriParser.parseUri("spotify:track"))
        .isInstanceOf(InvalidSpotifyUriException.class)
        .hasMessageContaining("Invalid Spotify URI format");
  }

  @Test
  void parseUri_shouldThrowExceptionForTooManyParts() {
    assertThatThrownBy(() -> spotifyUriParser.parseUri("spotify:track:123:extra:parts"))
        .isInstanceOf(InvalidSpotifyUriException.class)
        .hasMessageContaining("Invalid Spotify URI format");
  }

  @Test
  void parseUri_shouldParseTrackUri() {
    SpotifyUriParser.SpotifyUri result = spotifyUriParser.parseUri("spotify:track:123456");

    assertThat(result.type()).isEqualTo("track");
    assertThat(result.id()).isEqualTo("123456");
  }

  @Test
  void parseUri_shouldParseAlbumUri() {
    SpotifyUriParser.SpotifyUri result = spotifyUriParser.parseUri("spotify:album:abcdef");

    assertThat(result.type()).isEqualTo("album");
    assertThat(result.id()).isEqualTo("abcdef");
  }

  @Test
  void parseUri_shouldParseArtistUri() {
    SpotifyUriParser.SpotifyUri result = spotifyUriParser.parseUri("spotify:artist:xyz789");

    assertThat(result.type()).isEqualTo("artist");
    assertThat(result.id()).isEqualTo("xyz789");
  }

  @Test
  void parseUri_shouldParsePlaylistUri() {
    SpotifyUriParser.SpotifyUri result =
        spotifyUriParser.parseUri("spotify:playlist:37i9dQZF1DXcBWIGoYBM5M");

    assertThat(result.type()).isEqualTo("playlist");
    assertThat(result.id()).isEqualTo("37i9dQZF1DXcBWIGoYBM5M");
  }

  @Test
  void parseUri_shouldParseLegacyPlaylistUriFormat() {
    SpotifyUriParser.SpotifyUri result =
        spotifyUriParser.parseUri("spotify:user:johndoe:playlist:37i9dQZF1DXcBWIGoYBM5M");

    assertThat(result.type()).isEqualTo("playlist");
    assertThat(result.id()).isEqualTo("37i9dQZF1DXcBWIGoYBM5M");
  }

  @Test
  void parseUri_shouldHandleUriWithLongId() {
    String longId = "0TnOYISbd1XYRBk9myaseg";
    SpotifyUriParser.SpotifyUri result = spotifyUriParser.parseUri("spotify:track:" + longId);

    assertThat(result.type()).isEqualTo("track");
    assertThat(result.id()).isEqualTo(longId);
  }
}
