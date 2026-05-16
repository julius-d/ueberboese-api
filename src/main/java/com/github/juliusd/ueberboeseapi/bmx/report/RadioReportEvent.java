package com.github.juliusd.ueberboeseapi.bmx.report;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.NonNull;

@Builder(toBuilder = true)
public record RadioReportEvent(
    @NonNull OffsetDateTime timeStamp,
    @NonNull EventType eventType,
    @NonNull String reason,
    String reasonSubCode,
    @NonNull Integer timeIntoTrack,
    @NonNull Integer playbackDelay) {

  public enum EventType {
    START,
    STOP,
    PAUSE,
    RESUME,
    TIMED
  }
}
