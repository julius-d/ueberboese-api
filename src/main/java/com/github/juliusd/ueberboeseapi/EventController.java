package com.github.juliusd.ueberboeseapi;

import com.github.juliusd.ueberboeseapi.generated.EventsApi;
import com.github.juliusd.ueberboeseapi.generated.dtos.DeviceEventsRequestApiDto;
import com.github.juliusd.ueberboeseapi.service.EventStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EventController implements EventsApi {

  private final EventStorageService eventStorageService;

  @Override
  public ResponseEntity<Void> submitDeviceEvents(
      String deviceId, DeviceEventsRequestApiDto deviceEventsRequestApiDto) {
    eventStorageService.storeEvent(deviceId, deviceEventsRequestApiDto);

    log.info(
        "Stored event for device: {}. Total events for this device: {}",
        deviceId,
        eventStorageService.getEventCount(deviceId));

    return ResponseEntity.ok().build();
  }
}
