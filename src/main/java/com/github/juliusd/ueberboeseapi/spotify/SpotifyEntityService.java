package com.github.juliusd.ueberboeseapi.spotify;

import com.github.juliusd.ueberboeseapi.spotify.client.SpotifyEntitiesClient;
import com.github.juliusd.ueberboeseapi.spotify.client.SpotifyOAuthClient;
import com.github.juliusd.ueberboeseapi.spotify.dto.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpotifyEntityService {

  private final SpotifyAuthProperties spotifyAuthProperties;
  private final SpotifyAccountService spotifyAccountService;
  private final SpotifyUriParser spotifyUriParser;
  private final SpotifyEntitiesClient spotifyEntitiesClient;
  private final SpotifyOAuthClient spotifyOAuthClient;

  public SpotifyEntityInfo getEntityInfo(String uri) {
    log.info("Getting entity info for URI: {}", uri);

    // Parse URI
    SpotifyUriParser.SpotifyUri spotifyUri = spotifyUriParser.parseUri(uri);
    log.debug("Parsed URI - type: {}, id: {}", spotifyUri.type(), spotifyUri.id());

    // Get access token
    String accessToken = getAccessToken();

    try {
      // Prepare authorization header
      String authHeader = "Bearer " + accessToken;

      // Fetch entity based on type
      return switch (spotifyUri.type()) {
        case "track" -> getTrackInfo(authHeader, spotifyUri.id());
        case "album" -> getAlbumInfo(authHeader, spotifyUri.id());
        case "artist" -> getArtistInfo(authHeader, spotifyUri.id());
        case "playlist" -> getPlaylistInfo(authHeader, spotifyUri.id());
        case "show" -> getShowInfo(authHeader, spotifyUri.id());
        case "episode" -> getEpisodeInfo(authHeader, spotifyUri.id());
        default ->
            throw new InvalidSpotifyUriException(
                "Unsupported entity type: "
                    + spotifyUri.type()
                    + ". Supported types: track, album, artist, playlist, show, episode");
      };
    } catch (InvalidSpotifyUriException | SpotifyEntityNotFoundException e) {
      // Let these exceptions propagate unchanged to the controller
      throw e;
    } catch (Exception e) {
      log.error("Failed to fetch Spotify entity: {}", e.getMessage(), e);

      // Check if it's a 404 Not Found error from Spotify API
      String errorMsg = e.getMessage();
      if (errorMsg != null
          && (errorMsg.toLowerCase().contains("not found") || errorMsg.contains("404"))) {
        throw new SpotifyEntityNotFoundException("Spotify entity not found: " + uri, e);
      }

      throw new RuntimeException("Failed to fetch Spotify entity information", e);
    }
  }

  private String getAccessToken() {
    try {
      // Get the oldest connected Spotify account
      List<SpotifyAccount> accounts = spotifyAccountService.listAllAccounts();
      if (accounts.isEmpty()) {
        log.error("No Spotify accounts connected");
        throw new NoSpotifyAccountException(
            "No Spotify accounts connected. Please connect a Spotify account via the management API.");
      }

      // Use the oldest account (last in the list sorted by createdAt descending)
      SpotifyAccount oldestAccount = accounts.get(accounts.size() - 1);
      log.info(
          "Using Spotify account: {} ({})",
          oldestAccount.displayName(),
          oldestAccount.spotifyUserId());

      // Prepare form data for token refresh
      org.springframework.util.LinkedMultiValueMap<String, String> formData =
          new org.springframework.util.LinkedMultiValueMap<>();
      formData.add("grant_type", "refresh_token");
      formData.add("refresh_token", oldestAccount.refreshToken());
      formData.add("client_id", spotifyAuthProperties.clientId());
      formData.add("client_secret", spotifyAuthProperties.clientSecret());

      // Refresh the access token
      var authorizationCodeCredentials = spotifyOAuthClient.refreshAccessToken(formData);

      log.debug("Successfully obtained Spotify access token");
      return authorizationCodeCredentials.accessToken();

    } catch (Exception e) {
      log.error("Failed to authenticate with Spotify: {}", e.getMessage());
      throw new SpotifyException(e);
    }
  }

  private SpotifyEntityInfo getTrackInfo(String authHeader, String trackId) {
    TrackDto track = spotifyEntitiesClient.getTrack(authHeader, trackId);

    String name = track.name();
    String imageUrl = null;

    // Tracks get their images from the album
    AlbumSimplifiedDto album = track.album();
    if (album != null && album.images() != null && !album.images().isEmpty()) {
      imageUrl = selectMediumImage(album.images());
    }

    log.info("Found track: {} with image: {}", name, imageUrl);
    return new SpotifyEntityInfo(name, imageUrl);
  }

  private SpotifyEntityInfo getAlbumInfo(String authHeader, String albumId) {
    AlbumDto album = spotifyEntitiesClient.getAlbum(authHeader, albumId);

    String name = album.name();
    String imageUrl = null;

    if (album.images() != null && !album.images().isEmpty()) {
      imageUrl = selectMediumImage(album.images());
    }

    log.info("Found album: {} with image: {}", name, imageUrl);
    return new SpotifyEntityInfo(name, imageUrl);
  }

  private SpotifyEntityInfo getArtistInfo(String authHeader, String artistId) {
    ArtistDto artist = spotifyEntitiesClient.getArtist(authHeader, artistId);

    String name = artist.name();
    String imageUrl = null;

    if (artist.images() != null && !artist.images().isEmpty()) {
      imageUrl = selectMediumImage(artist.images());
    }

    log.info("Found artist: {} with image: {}", name, imageUrl);
    return new SpotifyEntityInfo(name, imageUrl);
  }

  private SpotifyEntityInfo getPlaylistInfo(String authHeader, String playlistId) {
    PlaylistDto playlist = spotifyEntitiesClient.getPlaylist(authHeader, playlistId);

    String name = playlist.name();
    String imageUrl = null;

    if (playlist.images() != null && !playlist.images().isEmpty()) {
      imageUrl = selectMediumImage(playlist.images());
    }

    log.info("Found playlist: {} with image: {}", name, imageUrl);
    return new SpotifyEntityInfo(name, imageUrl);
  }

  private SpotifyEntityInfo getShowInfo(String authHeader, String showId) {
    ShowDto show = spotifyEntitiesClient.getShow(authHeader, showId);

    String name = show.name();
    String imageUrl = null;

    if (show.images() != null && !show.images().isEmpty()) {
      imageUrl = selectMediumImage(show.images());
    }

    log.info("Found show: {} with image: {}", name, imageUrl);
    return new SpotifyEntityInfo(name, imageUrl);
  }

  private SpotifyEntityInfo getEpisodeInfo(String authHeader, String episodeId) {
    EpisodeDto episode = spotifyEntitiesClient.getEpisode(authHeader, episodeId);

    String name = episode.name();
    String imageUrl = null;

    if (episode.images() != null && !episode.images().isEmpty()) {
      imageUrl = selectMediumImage(episode.images());
    }

    log.info("Found episode: {} with image: {}", name, imageUrl);
    return new SpotifyEntityInfo(name, imageUrl);
  }

  private String selectMediumImage(List<ImageDto> images) {
    if (images == null || images.isEmpty()) {
      return null;
    }

    // Select the middle image (medium size)
    // Spotify typically provides images in descending size order
    int middleIndex = images.size() / 2;
    return images.get(middleIndex).url();
  }

  public record SpotifyEntityInfo(String name, String imageUrl) {}
}
