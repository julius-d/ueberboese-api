package com.github.juliusd.ueberboeseapi;

import com.github.juliusd.ueberboeseapi.device.Device;
import com.github.juliusd.ueberboeseapi.device.DeviceRepository;
import com.github.juliusd.ueberboeseapi.generated.ExperimentalApi;
import com.github.juliusd.ueberboeseapi.generated.dtos.CredentialApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.CustomerSupportRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.DeviceUpdateRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.DeviceUpdateResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.FullAccountResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetUpdateRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetUpdateResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SoftwareUpdateResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceApiDto;
import com.github.juliusd.ueberboeseapi.preset.Preset;
import com.github.juliusd.ueberboeseapi.preset.PresetService;
import com.github.juliusd.ueberboeseapi.service.AccountDataService;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
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
  private final PresetService presetService;
  private final AccountDataService accountDataService;

  @Override
  public ResponseEntity<PresetUpdateResponseApiDto> updatePreset(
      String accountId,
      String deviceId,
      Integer buttonNumber,
      PresetUpdateRequestApiDto presetUpdateRequestApiDto) {

    log.info("Updating preset {} for account {} and device {}", buttonNumber, accountId, deviceId);

    // Create preset entity and save to database
    Preset preset =
        Preset.builder()
            .accountId(accountId)
            .deviceId(deviceId)
            .buttonNumber(presetUpdateRequestApiDto.getButtonNumber())
            .containerArt(presetUpdateRequestApiDto.getContainerArt())
            .contentItemType(presetUpdateRequestApiDto.getContentItemType())
            .location(presetUpdateRequestApiDto.getLocation())
            .name(presetUpdateRequestApiDto.getName())
            .sourceId(presetUpdateRequestApiDto.getSourceid())
            .build();

    Preset savedPreset = presetService.savePreset(preset);

    // Get the source from account data (if available)
    SourceApiDto source = getSourceFromAccount(accountId, presetUpdateRequestApiDto.getSourceid());

    // Build the response
    PresetUpdateResponseApiDto response = new PresetUpdateResponseApiDto();
    response.setButtonNumber(savedPreset.buttonNumber());
    response.setContainerArt(savedPreset.containerArt());
    response.setContentItemType(savedPreset.contentItemType());
    response.setCreatedOn(savedPreset.createdOn());
    response.setLocation(savedPreset.location());
    response.setName(savedPreset.name());
    response.setSource(source);
    response.setUpdatedOn(savedPreset.updatedOn());
    response.setUsername(source.getUsername() != null ? source.getUsername() : "");

    return ResponseEntity.ok()
        .header(
            "Location",
            "http://streamingqa.bose.com/account/%s/device/%s/preset/%d"
                .formatted(accountId, deviceId, buttonNumber))
        .body(response);
  }

  private SourceApiDto getSourceFromAccount(String accountId, String sourceId) {
    // Try to load account data to get the actual source
    if (accountDataService.hasAccountData(accountId)) {
      try {
        FullAccountResponseApiDto accountData = accountDataService.loadFullAccountData(accountId);
        if (accountData.getSources() != null && accountData.getSources().getSource() != null) {
          List<SourceApiDto> sources = accountData.getSources().getSource();
          return sources.stream()
              .filter(s -> s.getId().equals(sourceId))
              .findFirst()
              .orElseGet(() -> createMockSource(sourceId));
        }
      } catch (IOException e) {
        log.warn("Failed to load account data for source lookup: {}", e.getMessage());
      }
    }
    return createMockSource(sourceId);
  }

  private SourceApiDto createMockSource(String sourceId) {
    CredentialApiDto credential = new CredentialApiDto();
    credential.setType("token_version_3");
    credential.setValue("mockToken456xyz=");

    SourceApiDto source = new SourceApiDto();
    source.setId(sourceId);
    source.setType("Audio");
    source.setCreatedOn(OffsetDateTime.parse("2018-08-11T09:52:31.000+00:00"));
    source.setCredential(credential);
    source.setName("mockuser5zt8py3wuxy123");
    source.setSourceproviderid("15");
    source.setSourcename("user@example.com");
    source.setUpdatedOn(OffsetDateTime.parse("2018-11-26T18:42:27.000+00:00"));
    source.setUsername("mockuser5zt8py3wuxy123");

    return source;
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

  @Override
  public ResponseEntity<Void> customerSupport(
      CustomerSupportRequestApiDto customerSupportRequestApiDto) {
    log.info("Received customer support data");

    return ResponseEntity.ok()
        .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
        .build();
  }
}
