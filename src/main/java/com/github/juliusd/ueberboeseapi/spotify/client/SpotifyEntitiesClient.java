package com.github.juliusd.ueberboeseapi.spotify.client;

import com.github.juliusd.ueberboeseapi.spotify.dto.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;

/**
 * HTTP service client for Spotify entities (tracks, albums, artists, playlists, shows, episodes).
 */
public interface SpotifyEntitiesClient {

  /**
   * Get a track by ID.
   *
   * @param authorization Bearer token for authentication
   * @param id Spotify track ID
   * @return Track details
   */
  @GetExchange("/v1/tracks/{id}")
  TrackDto getTrack(@RequestHeader("Authorization") String authorization, @PathVariable String id);

  /**
   * Get an album by ID.
   *
   * @param authorization Bearer token for authentication
   * @param id Spotify album ID
   * @return Album details
   */
  @GetExchange("/v1/albums/{id}")
  AlbumDto getAlbum(@RequestHeader("Authorization") String authorization, @PathVariable String id);

  /**
   * Get an artist by ID.
   *
   * @param authorization Bearer token for authentication
   * @param id Spotify artist ID
   * @return Artist details
   */
  @GetExchange("/v1/artists/{id}")
  ArtistDto getArtist(
      @RequestHeader("Authorization") String authorization, @PathVariable String id);

  /**
   * Get a playlist by ID.
   *
   * @param authorization Bearer token for authentication
   * @param id Spotify playlist ID
   * @return Playlist details
   */
  @GetExchange("/v1/playlists/{id}")
  PlaylistDto getPlaylist(
      @RequestHeader("Authorization") String authorization, @PathVariable String id);

  /**
   * Get a show by ID.
   *
   * @param authorization Bearer token for authentication
   * @param id Spotify show ID
   * @return Show details
   */
  @GetExchange("/v1/shows/{id}")
  ShowDto getShow(@RequestHeader("Authorization") String authorization, @PathVariable String id);

  /**
   * Get an episode by ID.
   *
   * @param authorization Bearer token for authentication
   * @param id Spotify episode ID
   * @return Episode details
   */
  @GetExchange("/v1/episodes/{id}")
  EpisodeDto getEpisode(
      @RequestHeader("Authorization") String authorization, @PathVariable String id);
}
