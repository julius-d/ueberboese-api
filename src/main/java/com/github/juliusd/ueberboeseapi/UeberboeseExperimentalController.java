package com.github.juliusd.ueberboeseapi;

import com.github.juliusd.ueberboeseapi.device.Device;
import com.github.juliusd.ueberboeseapi.device.DeviceRepository;
import com.github.juliusd.ueberboeseapi.generated.ExperimentalApi;
import com.github.juliusd.ueberboeseapi.generated.dtos.CredentialApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.DeviceUpdateRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.DeviceUpdateResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetUpdateRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetUpdateResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SoftwareUpdateResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceApiDto;
import java.time.OffsetDateTime;
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

  private final DeviceRepository deviceRepository;

  @Override
  public ResponseEntity<PresetUpdateResponseApiDto> updatePreset(
      String accountId,
      String deviceId,
      Integer presetNumber,
      PresetUpdateRequestApiDto presetUpdateRequestApiDto) {

    log.info("Updating preset {} for account {} and device {}", presetNumber, accountId, deviceId);

    // Build the credential
    CredentialApiDto credential = new CredentialApiDto();
    credential.setType("token_version_3");
    credential.setValue("mockToken456xyz=");

    // Build the source object from the request
    SourceApiDto source = new SourceApiDto();
    source.setId(presetUpdateRequestApiDto.getSourceid());
    source.setType("Audio");
    source.setCreatedOn(OffsetDateTime.parse("2018-08-11T09:52:31.000+00:00"));
    source.setCredential(credential);
    source.setName("mockuser5zt8py3wuxy123");
    source.setSourceproviderid("15");
    source.setSourcename("user@example.com");
    source.setUpdatedOn(OffsetDateTime.parse("2018-11-26T18:42:27.000+00:00"));
    source.setUsername("mockuser5zt8py3wuxy123");

    // Build the response
    PresetUpdateResponseApiDto response = new PresetUpdateResponseApiDto();
    response.setButtonNumber(presetUpdateRequestApiDto.getButtonNumber());
    response.setContainerArt(presetUpdateRequestApiDto.getContainerArt());
    response.setContentItemType(presetUpdateRequestApiDto.getContentItemType());
    response.setCreatedOn(OffsetDateTime.parse("2018-11-14T18:27:39.000+00:00"));
    response.setLocation(presetUpdateRequestApiDto.getLocation());
    response.setName(presetUpdateRequestApiDto.getName());
    response.setSource(source);
    response.setUpdatedOn(OffsetDateTime.parse("2025-12-28T16:38:41.000+00:00"));
    response.setUsername(presetUpdateRequestApiDto.getUsername());

    return ResponseEntity.ok()
        .header(
            "Location",
            "http://streamingqa.bose.com/account/%s/device/%s/preset/%d"
                .formatted(accountId, deviceId, presetNumber))
        .body(response);
  }

  @Override
  public ResponseEntity<SoftwareUpdateResponseApiDto> getSoftwareUpdate(String accountId) {
    log.info("Getting software update for account {}", accountId);

    SoftwareUpdateResponseApiDto response = new SoftwareUpdateResponseApiDto();
    response.setSoftwareUpdateLocation("");

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<DeviceUpdateResponseApiDto> updateDevice(
      String accountId, String deviceId, DeviceUpdateRequestApiDto deviceUpdateRequestApiDto) {
    log.info("Updating device {} for account {}", deviceId, accountId);

    // Get or create device using builder pattern
    var now = OffsetDateTime.now();
    Device device =
        deviceRepository
            .findById(deviceId)
            .map(
                existingDevice ->
                    existingDevice.toBuilder()
                        .name(deviceUpdateRequestApiDto.getName()) // Update name
                        .lastSeen(now) // Update lastSeen
                        .build())
            .orElseGet(
                () ->
                    Device.builder()
                        .deviceId(deviceId)
                        .name(deviceUpdateRequestApiDto.getName())
                        .ipAddress(null) // No IP address for new devices
                        .firstSeen(now)
                        .lastSeen(now)
                        .version(null)
                        .build());

    device = deviceRepository.save(device);

    // Build response
    DeviceUpdateResponseApiDto response = new DeviceUpdateResponseApiDto();
    response.setDeviceid(device.deviceId());
    response.setCreatedOn(device.firstSeen());
    response.setIpaddress(device.ipAddress());
    response.setName(device.name());
    response.setUpdatedOn(device.lastSeen());

    return ResponseEntity.ok()
        .header(
            "Location",
            "http://streamingqa.bose.com/account/%s/device/%s".formatted(accountId, deviceId))
        .header("METHOD_NAME", "updateDevice")
        .body(response);
  }
}
