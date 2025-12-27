package com.github.juliusd.ueberboeseapi.spotify;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Spotify API authentication.
 *
 * <p>These properties are required for authenticating with the Spotify Web API to refresh user
 * access tokens. The client ID and secret are obtained from the Spotify Developer Dashboard.
 *
 * <p>Example configuration in application.properties:
 *
 * <pre>
 * spotify.auth.client-id=your-spotify-client-id
 * spotify.auth.client-secret=your-spotify-client-secret
 * </pre>
 *
 * <p>Example environment variable configuration:
 *
 * <pre>
 * SPOTIFY_AUTH_CLIENT_ID=your-spotify-client-id
 * SPOTIFY_AUTH_CLIENT_SECRET=your-spotify-client-secret
 * </pre>
 */
@ConfigurationProperties(prefix = "spotify.auth")
public record SpotifyAuthProperties(
    /**
     * The Spotify client ID obtained from the Spotify Developer Dashboard. This identifies your
     * application to the Spotify Web API.
     */
    String clientId,
    /**
     * The Spotify client secret obtained from the Spotify Developer Dashboard. This is used along
     * with the client ID to authenticate your application.
     */
    String clientSecret) {}
