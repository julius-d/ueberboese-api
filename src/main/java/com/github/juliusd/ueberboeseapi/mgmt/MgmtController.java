package com.github.juliusd.ueberboeseapi.mgmt;

import com.github.juliusd.ueberboeseapi.bmx.report.RadioReportEvent;
import com.github.juliusd.ueberboeseapi.bmx.report.RadioReportStorageService;
import com.github.juliusd.ueberboeseapi.bmx.report.RadioSession;
import com.github.juliusd.ueberboeseapi.generated.mgmt.AccountManagementApi;
import com.github.juliusd.ueberboeseapi.generated.mgmt.EventManagementApi;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.DeviceEventApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.ErrorApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.GetDeviceEvents200ResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.ListSpeakers200ResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.RadioReportEventApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.RadioReportSessionApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.RadioReportsApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.SpeakerApiDto;
import com.github.juliusd.ueberboeseapi.service.DeviceTrackingService;
import com.github.juliusd.ueberboeseapi.service.EventStorageService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
  private final RadioReportStorageService radioReportStorageService;

  @Override
  public ResponseEntity<ListSpeakers200ResponseApiDto> listSpeakers(String accountId) {
    log.info("Listing speakers for accountId: {}", accountId);

    List<SpeakerApiDto> speakers = new ArrayList<>();

    for (DeviceTrackingService.DeviceInfo deviceInfo : deviceTrackingService.getAllDevices()) {
      SpeakerApiDto speaker = new SpeakerApiDto();
      speaker.setIpAddress(deviceInfo.ipAddress());
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

  @Override
  public ResponseEntity<RadioReportsApiDto> getRadioReports() {
    Map<String, RadioSession> sessions = radioReportStorageService.getSessionsByListenId();
    Map<String, List<RadioReportEvent>> reportsByListenId =
        radioReportStorageService.getAllByListenId();

    var allListenIds = new LinkedHashSet<String>(sessions.keySet());
    allListenIds.addAll(reportsByListenId.keySet());

    List<RadioReportSessionApiDto> result = new ArrayList<>();
    for (String listenId : allListenIds) {
      var session = sessions.get(listenId);
      List<RadioReportEventApiDto> events = new ArrayList<>();
      for (RadioReportEvent r : reportsByListenId.getOrDefault(listenId, List.of())) {
        RadioReportEventApiDto event = new RadioReportEventApiDto();
        event.setTimeStamp(r.timeStamp());
        event.setEventType(r.eventType() != null ? r.eventType().name() : null);
        event.setReason(r.reason());
        event.setReasonSubCode(r.reasonSubCode());
        event.setTimeIntoTrack(r.timeIntoTrack());
        event.setPlaybackDelay(r.playbackDelay());
        events.add(event);
      }
      RadioReportSessionApiDto dto = new RadioReportSessionApiDto();
      dto.setListenId(listenId);
      if (session != null) {
        dto.setStationId(session.stationId());
        dto.setStationName(session.stationName());
        dto.setLogoUrl(session.logoUrl());
        dto.setStartedAt(session.startedAt());
      }
      dto.setEvents(events);
      result.add(dto);
    }

    RadioReportsApiDto response = new RadioReportsApiDto();
    response.setSessions(result);
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
