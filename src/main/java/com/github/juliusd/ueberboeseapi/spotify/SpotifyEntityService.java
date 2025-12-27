package com.github.juliusd.ueberboeseapi.spotify;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpotifyEntityService {

  private final SpotifyApiUrlProperties spotifyHostProperties;
  private final SpotifyAuthProperties spotifyAuthProperties;
  private final SpotifyAccountService spotifyAccountService;
  private final SpotifyUriParser spotifyUriParser;

  public SpotifyEntityInfo getEntityInfo(String uri) {
    log.info("Getting entity info for URI: {}", uri);

    // Parse URI
    SpotifyUriParser.SpotifyUri spotifyUri = spotifyUriParser.parseUri(uri);
    log.debug("Parsed URI - type: {}, id: {}", spotifyUri.type(), spotifyUri.id());

    // Get authenticated Spotify API
    SpotifyApi spotifyApi = createAuthenticatedSpotifyApi();

    try {
      // Fetch entity based on type
      return switch (spotifyUri.type()) {
        case "track" -> getTrackInfo(spotifyApi, spotifyUri.id());
        case "album" -> getAlbumInfo(spotifyApi, spotifyUri.id());
        case "artist" -> getArtistInfo(spotifyApi, spotifyUri.id());
        case "playlist" -> getPlaylistInfo(spotifyApi, spotifyUri.id());
        default ->
            throw new InvalidSpotifyUriException(
                "Unsupported entity type: "
                    + spotifyUri.type()
                    + ". Supported types: track, album, artist, playlist");
      };
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      log.error("Failed to fetch Spotify entity: {}", e.getMessage(), e);

      // Check if it's a 404 Not Found error
      if (e instanceof SpotifyWebApiException apiException) {
        if (apiException.getMessage() != null && apiException.getMessage().contains("not found")) {
          throw new SpotifyEntityNotFoundException(
              "Spotify entity not found: " + uri, apiException);
        }
      }

      throw new RuntimeException("Failed to fetch Spotify entity information", e);
    }
  }

  private SpotifyApi createAuthenticatedSpotifyApi() {
    try {
      // Get the oldest connected Spotify account
      List<SpotifyAccountService.SpotifyAccount> accounts = spotifyAccountService.listAllAccounts();
      if (accounts.isEmpty()) {
        log.error("No Spotify accounts connected");
        throw new NoSpotifyAccountException(
            "No Spotify accounts connected. Please connect a Spotify account via the management API.");
      }

      // Use the oldest account (last in the list sorted by createdAt descending)
      SpotifyAccountService.SpotifyAccount oldestAccount = accounts.get(accounts.size() - 1);
      log.info(
          "Using Spotify account: {} ({})",
          oldestAccount.displayName(),
          oldestAccount.spotifyUserId());

      // Build SpotifyApi with refresh token
      SpotifyApi spotifyApi =
          new SpotifyApi.Builder()
              .setHost(spotifyHostProperties.host())
              .setScheme(spotifyHostProperties.schema())
              .setPort(spotifyHostProperties.port())
              .setRefreshToken(oldestAccount.refreshToken())
              .setClientId(spotifyAuthProperties.clientId())
              .setClientSecret(spotifyAuthProperties.clientSecret())
              .build();

      // Refresh the access token
      var authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
      var authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();

      // Set the access token
      spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());

      log.debug("Successfully obtained Spotify access token");
      return spotifyApi;

    } catch (IOException | SpotifyWebApiException | ParseException e) {
      log.error("Failed to authenticate with Spotify: {}", e.getMessage());
      throw new SpotifyException(e);
    }
  }

  private SpotifyEntityInfo getTrackInfo(SpotifyApi spotifyApi, String trackId)
      throws IOException, SpotifyWebApiException, ParseException {
    Track track = spotifyApi.getTrack(trackId).build().execute();

    String name = track.getName();
    String imageUrl = null;

    // Tracks get their images from the album
    AlbumSimplified album = track.getAlbum();
    if (album != null && album.getImages() != null && album.getImages().length > 0) {
      imageUrl = selectMediumImage(album.getImages());
    }

    log.info("Found track: {} with image: {}", name, imageUrl);
    return new SpotifyEntityInfo(name, imageUrl);
  }

  private SpotifyEntityInfo getAlbumInfo(SpotifyApi spotifyApi, String albumId)
      throws IOException, SpotifyWebApiException, ParseException {
    Album album = spotifyApi.getAlbum(albumId).build().execute();

    String name = album.getName();
    String imageUrl = null;

    if (album.getImages() != null && album.getImages().length > 0) {
      imageUrl = selectMediumImage(album.getImages());
    }

    log.info("Found album: {} with image: {}", name, imageUrl);
    return new SpotifyEntityInfo(name, imageUrl);
  }

  private SpotifyEntityInfo getArtistInfo(SpotifyApi spotifyApi, String artistId)
      throws IOException, SpotifyWebApiException, ParseException {
    Artist artist = spotifyApi.getArtist(artistId).build().execute();

    String name = artist.getName();
    String imageUrl = null;

    if (artist.getImages() != null && artist.getImages().length > 0) {
      imageUrl = selectMediumImage(artist.getImages());
    }

    log.info("Found artist: {} with image: {}", name, imageUrl);
    return new SpotifyEntityInfo(name, imageUrl);
  }

  private SpotifyEntityInfo getPlaylistInfo(SpotifyApi spotifyApi, String playlistId)
      throws IOException, SpotifyWebApiException, ParseException {
    Playlist playlist = spotifyApi.getPlaylist(playlistId).build().execute();

    String name = playlist.getName();
    String imageUrl = null;

    if (playlist.getImages() != null && playlist.getImages().length > 0) {
      imageUrl = selectMediumImage(playlist.getImages());
    }

    log.info("Found playlist: {} with image: {}", name, imageUrl);
    return new SpotifyEntityInfo(name, imageUrl);
  }

  private String selectMediumImage(Image[] images) {
    if (images == null || images.length == 0) {
      return null;
    }

    // Select the middle image (medium size)
    // Spotify typically provides images in descending size order
    int middleIndex = images.length / 2;
    return images[middleIndex].getUrl();
  }

  public record SpotifyEntityInfo(String name, String imageUrl) {}
}
