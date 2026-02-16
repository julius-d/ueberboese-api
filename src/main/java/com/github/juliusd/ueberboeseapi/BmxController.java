package com.github.juliusd.ueberboeseapi;

import com.github.juliusd.ueberboeseapi.bmx.BmxService;
import com.github.juliusd.ueberboeseapi.generated.BmxApi;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxPlaybackResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxReportRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxReportResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxServicesAvailabilityResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxServicesResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxTokenRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxTokenResponseApiDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller implementing BMX (Bose Music Experience) endpoints for streaming radio services.
 * Handles service discovery, TuneIn playback, custom streams, and token management.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ueberboese.bmx.enabled", havingValue = "true")
public class BmxController implements BmxApi {

  private final BmxService bmxService;

  @Override
  public ResponseEntity<BmxServicesResponseApiDto> getBmxServices() {
    log.info("Getting BMX services registry");

    try {
      BmxServicesResponseApiDto response = bmxService.getBmxServices();
      return ResponseEntity.ok()
          .header("Content-Type", "application/json")
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
          .header(
              "Access-Control-Allow-Headers",
              "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
          .body(response);
    } catch (Exception e) {
      log.error("Failed to get BMX services", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<BmxServicesAvailabilityResponseApiDto> getBmxServicesAvailability() {
    BmxServicesAvailabilityResponseApiDto response = bmxService.getBmxServicesAvailability();
    return ResponseEntity.ok()
        .header("Content-Type", "application/json")
        .header("X-Bmx-Adapter-Version", "master.4.40")
        .body(response);
  }

  @Override
  public ResponseEntity<BmxPlaybackResponseApiDto> getTuneInPlayback(String stationId) {
    log.info("Getting TuneIn playback for station: {}", stationId);

    try {
      BmxPlaybackResponseApiDto response = bmxService.getTuneInPlayback(stationId);
      return ResponseEntity.ok()
          .header("Content-Type", "application/json")
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
          .header(
              "Access-Control-Allow-Headers",
              "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
          .body(response);
    } catch (Exception e) {
      log.error("Failed to get TuneIn playback for station: {}", stationId, e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<BmxPlaybackResponseApiDto> getCustomStreamPlayback(String data) {
    log.info("Getting custom stream playback");

    try {
      if (data == null || data.isEmpty()) {
        return ResponseEntity.badRequest().build();
      }

      BmxPlaybackResponseApiDto response = bmxService.getCustomStreamPlayback(data);
      return ResponseEntity.ok()
          .header("Content-Type", "application/json")
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
          .header(
              "Access-Control-Allow-Headers",
              "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
          .body(response);
    } catch (IllegalArgumentException e) {
      log.warn("Invalid custom stream data: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Failed to get custom stream playback", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<BmxTokenResponseApiDto> refreshTuneInToken(
      BmxTokenRequestApiDto bmxTokenRequestApiDto) {
    log.info("Refreshing TuneIn token");

    try {
      String refreshToken = bmxTokenRequestApiDto.getRefreshToken();
      if (refreshToken == null || refreshToken.isEmpty()) {
        return ResponseEntity.badRequest().build();
      }

      BmxTokenResponseApiDto response = bmxService.refreshTuneInToken(refreshToken);
      return ResponseEntity.ok()
          .header("Content-Type", "application/json")
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
          .header(
              "Access-Control-Allow-Headers",
              "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
          .body(response);
    } catch (Exception e) {
      log.error("Failed to refresh TuneIn token", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<BmxReportResponseApiDto> reportTuneInAnalytics(
      BmxReportRequestApiDto bmxReportRequestApiDto,
      String streamId,
      String guideId,
      String listenId,
      String streamType) {
    log.info(
        "Received TuneIn analytics report: streamId={}, guideId={}, listenId={}, streamType={}, eventType={}",
        streamId,
        guideId,
        listenId,
        streamType,
        bmxReportRequestApiDto != null ? bmxReportRequestApiDto.getEventType() : "null");

    try {
      BmxReportResponseApiDto response = bmxService.reportAnalytics();
      return ResponseEntity.ok()
          .header("Content-Type", "application/json")
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
          .header(
              "Access-Control-Allow-Headers",
              "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
          .body(response);
    } catch (Exception e) {
      log.error("Failed to process analytics report", e);
      return ResponseEntity.internalServerError().build();
    }
  }
}
