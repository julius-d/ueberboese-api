package com.github.juliusd.ueberboeseapi.spotify;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spotify.api")
public record SpotifyApiUrlProperties(URI baseUrl, URI authBaseUrl) {}
