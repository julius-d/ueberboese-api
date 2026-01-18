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
    var now = OffsetDateTime.now().withNano(0);

    // Step 1: Check if this preset content (location + sourceId + contentItemType) exists at a
    // different button
    Optional<Preset> presetWithSameContent =
        presetRepository.findByAccountIdAndDeviceIdAndLocationAndSourceIdAndContentItemType(
            preset.accountId(),
            preset.deviceId(),
            preset.location(),
            preset.sourceId(),
            preset.contentItemType());

    // Step 2: Check if the target button is already occupied
    Optional<Preset> presetAtTargetButton =
        presetRepository.findByAccountIdAndDeviceIdAndButtonNumber(
            preset.accountId(), preset.deviceId(), preset.buttonNumber());

    // Step 3: Handle the different scenarios
    if (presetWithSameContent.isPresent()) {
      Preset existingPreset = presetWithSameContent.get();

      if (existingPreset.buttonNumber().equals(preset.buttonNumber())) {
        // Scenario: Updating preset at the same button (just update metadata: name and
        // containerArt)
        Preset updated =
            existingPreset.toBuilder()
                .containerArt(preset.containerArt())
                .name(preset.name())
                .updatedOn(now)
                .build();
        Preset saved = presetRepository.save(updated);
        log.info(
            "Updated preset metadata id={} at button {} for account={}, device={}",
            saved.id(),
            preset.buttonNumber(),
            preset.accountId(),
            preset.deviceId());
        return saved;
      } else {
        // Scenario: Moving preset to a different button
        // Delete any preset at the target button first
        presetAtTargetButton.ifPresent(
            targetPreset -> {
              presetRepository.delete(targetPreset);
              log.info(
                  "Deleted preset id={} at button {} to make room for move",
                  targetPreset.id(),
                  targetPreset.buttonNumber());
            });

        // Update the existing preset to the new button number (and metadata)
        Preset moved =
            existingPreset.toBuilder()
                .buttonNumber(preset.buttonNumber())
                .containerArt(preset.containerArt())
                .name(preset.name())
                .updatedOn(now)
                .build();
        Preset saved = presetRepository.save(moved);
        log.info(
            "Moved preset id={} from button {} to button {} for account={}, device={}",
            saved.id(),
            existingPreset.buttonNumber(),
            preset.buttonNumber(),
            preset.accountId(),
            preset.deviceId());
        return saved;
      }
    } else if (presetAtTargetButton.isPresent()) {
      // Scenario: Different content at this button - replace it
      Preset current = presetAtTargetButton.get();
      Preset updated =
          current.toBuilder()
              .containerArt(preset.containerArt())
              .contentItemType(preset.contentItemType())
              .location(preset.location())
              .name(preset.name())
              .sourceId(preset.sourceId())
              .updatedOn(now)
              .build();
      Preset saved = presetRepository.save(updated);
      log.info(
          "Replaced preset id={} at button {} for account={}, device={}",
          saved.id(),
          preset.buttonNumber(),
          preset.accountId(),
          preset.deviceId());
      return saved;
    } else {
      // Scenario: New preset at an empty button
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
      Preset saved = presetRepository.save(newPreset);
      log.info(
          "Created new preset id={} at button {} for account={}, device={}",
          saved.id(),
          preset.buttonNumber(),
          preset.accountId(),
          preset.deviceId());
      return saved;
    }
  }

  public List<Preset> getPresets(String accountId, String deviceId) {
    return presetRepository.findByAccountIdAndDeviceId(accountId, deviceId);
  }

  public Optional<Preset> getPreset(String accountId, String deviceId, Integer buttonNumber) {
    return presetRepository.findByAccountIdAndDeviceIdAndButtonNumber(
        accountId, deviceId, buttonNumber);
  }
}
