package com.github.juliusd.ueberboeseapi.spotify;

import java.time.OffsetDateTime;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

@Table("SPOTIFY_ACCOUNT")
public record SpotifyAccount(
    @Id Long id,
    @NonNull String spotifyUserId,
    String displayName,
    String refreshToken,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    @Version Long version) {}
