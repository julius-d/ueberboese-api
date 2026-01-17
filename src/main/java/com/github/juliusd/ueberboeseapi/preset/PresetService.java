package com.github.juliusd.ueberboeseapi.preset;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresetService {

  private final PresetRepository presetRepository;

  @Transactional
  public Preset savePreset(Preset preset) {
    // Check if preset exists for this account/device/buttonNumber
    Optional<Preset> existing =
        presetRepository.findByAccountIdAndDeviceIdAndButtonNumber(
            preset.accountId(), preset.deviceId(), preset.buttonNumber());
    var now = OffsetDateTime.now().withNano(0);

    Preset saved;
    if (existing.isPresent()) {
      // Update existing preset
      Preset current = existing.get();
      Preset updated =
          Preset.builder()
              .id(current.id())
              .accountId(preset.accountId())
              .deviceId(preset.deviceId())
              .buttonNumber(preset.buttonNumber())
              .containerArt(preset.containerArt())
              .contentItemType(preset.contentItemType())
              .createdOn(current.createdOn()) // Keep original createdOn
              .location(preset.location())
              .name(preset.name())
              .updatedOn(now)
              .sourceId(preset.sourceId())
              .version(current.version())
              .build();
      saved = presetRepository.save(updated);
      log.info(
          "Updated existing preset id={} for account={}, device={}, buttonNumber={}",
          saved.id(),
          preset.accountId(),
          preset.deviceId(),
          preset.buttonNumber());
    } else {
      // Create new preset
      Preset newPreset =
          Preset.builder()
              .id(null) // Auto-generated
              .accountId(preset.accountId())
              .deviceId(preset.deviceId())
              .buttonNumber(preset.buttonNumber())
              .containerArt(preset.containerArt())
              .contentItemType(preset.contentItemType())
              .createdOn(now)
              .location(preset.location())
              .name(preset.name())
              .updatedOn(now)
              .sourceId(preset.sourceId())
              .version(null) // Let Spring Data JDBC manage the version
              .build();
      saved = presetRepository.save(newPreset);
      log.info(
          "Created new preset id={} for account={}, device={}, buttonNumber={}",
          saved.id(),
          preset.accountId(),
          preset.deviceId(),
          preset.buttonNumber());
    }

    return saved;
  }

  public List<Preset> getPresets(String accountId, String deviceId) {
    return presetRepository.findByAccountIdAndDeviceId(accountId, deviceId);
  }

  public Optional<Preset> getPreset(String accountId, String deviceId, Integer buttonNumber) {
    return presetRepository.findByAccountIdAndDeviceIdAndButtonNumber(
        accountId, deviceId, buttonNumber);
  }
}
