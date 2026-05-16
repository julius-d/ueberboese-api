package com.github.juliusd.ueberboeseapi.bmx.report;

import java.time.OffsetDateTime;

public record RadioReportEvent(
    OffsetDateTime timeStamp,
    EventType eventType,
    String reason,
    String reasonSubCode,
    Integer timeIntoTrack,
    Integer playbackDelay) {

  public enum EventType {
    START,
    STOP,
    PAUSE,
    RESUME,
    TIMED
  }
}
