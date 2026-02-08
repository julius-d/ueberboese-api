package com.github.juliusd.ueberboeseapi.mgmt;

import com.github.juliusd.ueberboeseapi.generated.mgmt.AccountManagementApi;
import com.github.juliusd.ueberboeseapi.generated.mgmt.EventManagementApi;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.DeviceEventApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.ErrorApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.GetDeviceEvents200ResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.ListSpeakers200ResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.SpeakerApiDto;
import com.github.juliusd.ueberboeseapi.service.DeviceTrackingService;
import com.github.juliusd.ueberboeseapi.service.EventStorageService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MgmtController implements AccountManagementApi, EventManagementApi {

  private final DeviceTrackingService deviceTrackingService;
  private final EventStorageService eventStorageService;

  @Override
  public ResponseEntity<ListSpeakers200ResponseApiDto> listSpeakers(String accountId) {
    log.info("Listing speakers for accountId: {}", accountId);

    List<SpeakerApiDto> speakers = new ArrayList<>();

    for (DeviceTrackingService.DeviceInfo deviceInfo : deviceTrackingService.getAllDevices()) {
      SpeakerApiDto speaker = new SpeakerApiDto();
      speaker.setIpAddress(deviceInfo.getIpAddress());
      speakers.add(speaker);
    }

    ListSpeakers200ResponseApiDto response = new ListSpeakers200ResponseApiDto();
    response.setSpeakers(speakers);

    log.info("Successfully listed {} speaker(s)", speakers.size());
    return ResponseEntity.ok().header("Content-Type", "application/json").body(response);
  }

  @Override
  public ResponseEntity<GetDeviceEvents200ResponseApiDto> getDeviceEvents(String deviceId) {
    log.info("Retrieving events for device: {}", deviceId);

    var storedEvents = eventStorageService.getEventsForDevice(deviceId);
    List<DeviceEventApiDto> allDeviceEvents = new ArrayList<>();

    for (var sourceEvent : storedEvents) {
      DeviceEventApiDto deviceEvent = new DeviceEventApiDto();
      deviceEvent.setData(sourceEvent.getData());
      deviceEvent.setMonoTime(sourceEvent.getMonoTime());
      deviceEvent.setTime(sourceEvent.getTime());
      deviceEvent.setType(sourceEvent.getType());
      allDeviceEvents.add(deviceEvent);
    }

    GetDeviceEvents200ResponseApiDto response = new GetDeviceEvents200ResponseApiDto();
    response.setEvents(allDeviceEvents);

    log.info("Retrieved {} events for device: {}", allDeviceEvents.size(), deviceId);
    return ResponseEntity.ok().header("Content-Type", "application/json").body(response);
  }

  /** Exception handler for RuntimeException - returns 500 Internal Server Error. */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorApiDto> handleRuntimeException(RuntimeException e) {
    log.error("Internal server error: {}", e.getMessage(), e);

    ErrorApiDto error = new ErrorApiDto();
    error.setError("Internal server error");
    error.setMessage("Failed to retrieve speakers");

    return ResponseEntity.status(500).header("Content-Type", "application/json").body(error);
  }
}
