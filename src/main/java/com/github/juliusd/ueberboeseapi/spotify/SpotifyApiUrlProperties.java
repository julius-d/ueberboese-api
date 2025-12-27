package com.github.juliusd.ueberboeseapi.spotify;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spotify.api.url")
public record SpotifyApiUrlProperties(String schema, String host, Integer port) {}
