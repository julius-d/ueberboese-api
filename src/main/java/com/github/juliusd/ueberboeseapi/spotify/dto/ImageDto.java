package com.github.juliusd.ueberboeseapi.spotify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ImageDto(String url, Integer height, Integer width) {}
