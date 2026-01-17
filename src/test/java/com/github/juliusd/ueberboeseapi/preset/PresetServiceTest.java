package com.github.juliusd.ueberboeseapi.preset;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.ueberboeseapi.TestBase;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PresetServiceTest extends TestBase {

  @Autowired private PresetService presetService;

  @Autowired private PresetRepository presetRepository;

  @Test
  void savePreset_shouldCreateNewPreset() {
    // Given
    Preset preset =
        Preset.builder()
            .accountId("test-account")
            .deviceId("test-device")
            .buttonNumber(1)
            .containerArt("https://example.org/art.png")
            .contentItemType("stationurl")
            .location("/v1/playback/station/s12345")
            .name("Test Radio")
            .sourceId("19989621")
            .build();

    // When
    Preset saved = presetService.savePreset(preset);

    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.id()).isNotNull();
    assertThat(saved.accountId()).isEqualTo("test-account");
    assertThat(saved.deviceId()).isEqualTo("test-device");
    assertThat(saved.buttonNumber()).isEqualTo(1);
    assertThat(saved.containerArt()).isEqualTo("https://example.org/art.png");
    assertThat(saved.contentItemType()).isEqualTo("stationurl");
    assertThat(saved.location()).isEqualTo("/v1/playback/station/s12345");
    assertThat(saved.name()).isEqualTo("Test Radio");
    assertThat(saved.sourceId()).isEqualTo("19989621");
    assertThat(saved.createdOn()).isNotNull();
    assertThat(saved.updatedOn()).isNotNull();
  }

  @Test
  void savePreset_shouldUpdateExistingPreset() {
    // Given - create initial preset
    Preset initial =
        Preset.builder()
            .accountId("test-account")
            .deviceId("test-device")
            .buttonNumber(2)
            .containerArt("https://example.org/old-art.png")
            .contentItemType("stationurl")
            .location("/v1/playback/station/s111")
            .name("Old Name")
            .sourceId("source-1")
            .build();

    Preset saved = presetService.savePreset(initial);
    Long initialId = saved.id();

    // When - save preset with same account/device/buttonNumber but different data
    Preset update =
        Preset.builder()
            .accountId("test-account")
            .deviceId("test-device")
            .buttonNumber(2) // Same button number
            .containerArt("https://example.org/new-art.png")
            .contentItemType("tracklisturl")
            .location("/v1/playback/track/t222")
            .name("New Name")
            .sourceId("source-2")
            .build();

    Preset updated = presetService.savePreset(update);

    // Then
    assertThat(updated.id()).isEqualTo(initialId); // Same ID (updated, not inserted)
    assertThat(updated.name()).isEqualTo("New Name");
    assertThat(updated.location()).isEqualTo("/v1/playback/track/t222");
    assertThat(updated.sourceId()).isEqualTo("source-2");
    assertThat(updated.createdOn()).isEqualTo(saved.createdOn()); // Original createdOn preserved
    assertThat(updated.updatedOn()).isAfterOrEqualTo(saved.createdOn()); // updatedOn is updated

    // Verify only one preset exists
    List<Preset> presets = presetService.getPresets("test-account", "test-device");
    assertThat(presets).hasSize(1);
  }

  @Test
  void getPresets_shouldReturnPresetsForDevice() {
    // Given
    String accountId = "test-account-multi";
    String deviceId = "device-1";

    // Add multiple presets
    savePreset(accountId, deviceId, 1, "Preset 1", "source-1");
    savePreset(accountId, deviceId, 2, "Preset 2", "source-2");
    savePreset(accountId, deviceId, 3, "Preset 3", "source-3");

    // Add preset for different device
    savePreset(accountId, "device-2", 1, "Other Device Preset", "source-4");

    // When
    List<Preset> presets = presetService.getPresets(accountId, deviceId);

    // Then
    assertThat(presets).hasSize(3);
    assertThat(presets)
        .extracting(Preset::name)
        .containsExactlyInAnyOrder("Preset 1", "Preset 2", "Preset 3");
  }

  @Test
  void getPresets_shouldReturnEmptyListForUnknownDevice() {
    // When
    List<Preset> presets = presetService.getPresets("unknown-account", "unknown-device");

    // Then
    assertThat(presets).isEmpty();
  }

  @Test
  void getPreset_shouldReturnPresetByButtonNumber() {
    // Given
    String accountId = "test-account-single";
    String deviceId = "device-1";
    savePreset(accountId, deviceId, 1, "Button 1", "source-1");
    savePreset(accountId, deviceId, 2, "Button 2", "source-2");

    // When
    Optional<Preset> preset = presetService.getPreset(accountId, deviceId, 1);

    // Then
    assertThat(preset).isPresent();
    assertThat(preset.get().name()).isEqualTo("Button 1");
    assertThat(preset.get().buttonNumber()).isEqualTo(1);
  }

  @Test
  void getPreset_shouldReturnEmptyForNonExistentButton() {
    // Given
    String accountId = "test-account-missing";
    String deviceId = "device-1";
    savePreset(accountId, deviceId, 1, "Button 1", "source-1");

    // When
    Optional<Preset> preset = presetService.getPreset(accountId, deviceId, 99);

    // Then
    assertThat(preset).isEmpty();
  }

  @Test
  void savePreset_shouldNotAffectDifferentDevices() {
    // Given
    String accountId = "test-account";

    // Save presets for different devices with same button numbers
    savePreset(accountId, "device-1", 1, "Device 1 Button 1", "source-1");
    savePreset(accountId, "device-2", 1, "Device 2 Button 1", "source-2");

    // When
    List<Preset> device1Presets = presetService.getPresets(accountId, "device-1");
    List<Preset> device2Presets = presetService.getPresets(accountId, "device-2");

    // Then
    assertThat(device1Presets).hasSize(1);
    assertThat(device1Presets.get(0).name()).isEqualTo("Device 1 Button 1");

    assertThat(device2Presets).hasSize(1);
    assertThat(device2Presets.get(0).name()).isEqualTo("Device 2 Button 1");
  }

  @Test
  void savePreset_shouldNotAffectDifferentAccounts() {
    // Given
    String deviceId = "shared-device";

    // Save presets for different accounts
    savePreset("account-1", deviceId, 1, "Account 1 Button 1", "source-1");
    savePreset("account-2", deviceId, 1, "Account 2 Button 1", "source-2");

    // When
    List<Preset> account1Presets = presetService.getPresets("account-1", deviceId);
    List<Preset> account2Presets = presetService.getPresets("account-2", deviceId);

    // Then
    assertThat(account1Presets).hasSize(1);
    assertThat(account1Presets.get(0).name()).isEqualTo("Account 1 Button 1");

    assertThat(account2Presets).hasSize(1);
    assertThat(account2Presets.get(0).name()).isEqualTo("Account 2 Button 1");
  }

  private void savePreset(
      String accountId, String deviceId, Integer buttonNumber, String name, String sourceId) {
    Preset preset =
        Preset.builder()
            .accountId(accountId)
            .deviceId(deviceId)
            .buttonNumber(buttonNumber)
            .containerArt("https://example.org/art.png")
            .contentItemType("stationurl")
            .location("/v1/playback/station/s" + buttonNumber)
            .name(name)
            .sourceId(sourceId)
            .build();
    presetService.savePreset(preset);
  }
}
