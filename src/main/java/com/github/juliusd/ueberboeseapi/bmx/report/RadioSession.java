package com.github.juliusd.ueberboeseapi.bmx.report;

import java.time.OffsetDateTime;

public record RadioSession(
    String listenId,
    String stationId,
    String stationName,
    String logoUrl,
    OffsetDateTime startedAt) {}
