package com.github.juliusd.ueberboeseapi.service;

import com.github.juliusd.ueberboeseapi.generated.dtos.DeviceEventsRequestApiDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for storing and retrieving device events in-memory.
 *
 * <p>This service maintains an in-memory storage of events received from Bose SoundTouch devices,
 * organized by device ID. Events are limited to a configurable maximum per device, with oldest
 * events being removed when the limit is exceeded.
 */
@Service
@Slf4j
public class EventStorageService {

  // In-memory storage of events by device ID
  private final Map<String, List<DeviceEventsRequestApiDto>> eventsByDevice =
      new ConcurrentHashMap<>();

  @Value("${ueberboese.events.max-events-per-device}")
  private int maxEventsPerDevice;

  /**
   * Store an event for a specific device.
   *
   * <p>If storing this event would exceed the maximum events per device limit, the oldest event(s)
   * will be automatically removed to maintain the limit.
   *
   * @param deviceId The device ID
   * @param event The event data to store
   */
  public void storeEvent(String deviceId, DeviceEventsRequestApiDto event) {
    List<DeviceEventsRequestApiDto> events =
        eventsByDevice.computeIfAbsent(deviceId, k -> new ArrayList<>());

    // Synchronize on the list to ensure thread-safe operations
    synchronized (events) {
      events.add(event);

      // Remove oldest events if limit exceeded
      while (events.size() > maxEventsPerDevice) {
        events.removeFirst();
      }
    }
  }

  /**
   * Get all events for a specific device.
   *
   * @param deviceId The device ID
   * @return Copy of the list of events for the device (empty list if no events found)
   */
  public List<DeviceEventsRequestApiDto> getEventsForDevice(String deviceId) {
    List<DeviceEventsRequestApiDto> events = eventsByDevice.get(deviceId);
    if (events == null) {
      return new ArrayList<>();
    }

    // Return a copy to avoid concurrent modification issues
    synchronized (events) {
      return new ArrayList<>(events);
    }
  }

  /**
   * Get the total number of events stored for a specific device.
   *
   * @param deviceId The device ID
   * @return Number of events stored
   */
  public int getEventCount(String deviceId) {
    List<DeviceEventsRequestApiDto> events = eventsByDevice.get(deviceId);
    if (events == null) {
      return 0;
    }

    synchronized (events) {
      return events.size();
    }
  }

  /** Clear all stored events. Used for testing. */
  public void clearAllEvents() {
    eventsByDevice.clear();
  }
}
