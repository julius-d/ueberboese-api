package com.github.juliusd.ueberboeseapi.spotify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrackDto(String id, String name, AlbumSimplifiedDto album) {}
