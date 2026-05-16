package com.github.juliusd.ueberboeseapi.bmx.report;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.NonNull;

@Builder(toBuilder = true)
public record RadioSession(
    @NonNull String listenId,
    @NonNull String stationId,
    @NonNull String stationName,
    String logoUrl,
    @NonNull OffsetDateTime startedAt) {}
