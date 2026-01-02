package com.github.juliusd.ueberboeseapi.spotify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EpisodeDto(String id, String name, List<ImageDto> images) {}
