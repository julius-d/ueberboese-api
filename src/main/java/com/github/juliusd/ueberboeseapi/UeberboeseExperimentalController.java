package com.github.juliusd.ueberboeseapi;

import com.github.juliusd.ueberboeseapi.generated.ExperimentalApi;
import com.github.juliusd.ueberboeseapi.generated.dtos.SoftwareUpdateResponseApiDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ueberboese.experimental.enabled", havingValue = "true")
public class UeberboeseExperimentalController implements ExperimentalApi {

  @Override
  public ResponseEntity<SoftwareUpdateResponseApiDto> getSoftwareUpdate(String accountId) {
    log.info("Getting software update for account {}", accountId);

    SoftwareUpdateResponseApiDto response = new SoftwareUpdateResponseApiDto();
    response.setSoftwareUpdateLocation("");

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Void> getProviderSettings(String accountId) {
    log.info("Getting provider settings for account {}", accountId);

    return ResponseEntity.ok()
        .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
        .header("METHOD_NAME", "getProviderSettings")
        .build();
  }

  @Override
  public ResponseEntity<Void> getStreamingToken(String deviceId) {
    log.info("Getting streaming token for device {}", deviceId);

    String mockToken = "mockRefreshedToken123xyz";

    return ResponseEntity.ok().header("Authorization", mockToken).build();
  }
}
