package com.github.juliusd.ueberboeseapi.preset;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.ueberboeseapi.TestBase;
import java.util.List;
import java.util.Optional;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PresetServiceTest extends TestBase {

  private static final String ACCOUNT_ID = "test-account";
  private static final String DEVICE_ID = "test-device";

  @Autowired private PresetService presetService;

  @Test
  void savePreset_shouldCreateNewPreset() {
    // Given
    Preset preset =
        Preset.builder()
            .accountId(ACCOUNT_ID)
            .deviceId(DEVICE_ID)
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
    assertThat(saved.accountId()).isEqualTo(ACCOUNT_ID);
    assertThat(saved.deviceId()).isEqualTo(DEVICE_ID);
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
  void savePreset_shouldUpdateMetadataWhenContentStaysSame() {
    // Given - create initial preset
    Preset initial =
        Preset.builder()
            .accountId(ACCOUNT_ID)
            .deviceId(DEVICE_ID)
            .buttonNumber(1)
            .containerArt("https://example.org/old-art.png")
            .contentItemType("stationurl")
            .location("/v1/playback/station/s111")
            .name("Old Name")
            .sourceId("source-1")
            .build();

    Preset saved = presetService.savePreset(initial);
    Long initialId = saved.id();

    // When - update metadata (name, containerArt) but keep same content identity
    Preset update =
        Preset.builder()
            .accountId(ACCOUNT_ID)
            .deviceId(DEVICE_ID)
            .buttonNumber(1) // Same button
            .containerArt("https://example.org/new-art.png") // Changed metadata
            .contentItemType("stationurl") // Same contentItemType (part of content identity)
            .location("/v1/playback/station/s111") // Same location
            .name("New Name") // Changed metadata
            .sourceId("source-1") // Same sourceId
            .build();

    Preset updated = presetService.savePreset(update);

    // Then - should update metadata but keep same ID, createdOn, and content identity
    assertThat(updated.id()).isEqualTo(initialId); // Same ID
    assertThat(updated.buttonNumber()).isEqualTo(1); // Same button
    assertThat(updated.location()).isEqualTo("/v1/playback/station/s111"); // Same location
    assertThat(updated.sourceId()).isEqualTo("source-1"); // Same sourceId
    assertThat(updated.contentItemType()).isEqualTo("stationurl"); // Same contentItemType
    assertThat(updated.name()).isEqualTo("New Name"); // Updated metadata
    assertThat(updated.containerArt())
        .isEqualTo("https://example.org/new-art.png"); // Updated metadata
    assertThat(updated.createdOn()).isEqualTo(saved.createdOn()); // Original createdOn preserved
    assertThat(updated.updatedOn()).isAfterOrEqualTo(saved.updatedOn()); // updatedOn is updated

    // Verify only one preset exists
    List<Preset> presets = presetService.getPresets(ACCOUNT_ID, DEVICE_ID);
    assertThat(presets).hasSize(1);
  }

  @Test
  void savePreset_shouldReplaceWhenContentItemTypeChanges() {
    // Given - create initial preset with stationurl
    Preset initial =
        Preset.builder()
            .accountId(ACCOUNT_ID)
            .deviceId(DEVICE_ID)
            .buttonNumber(1)
            .containerArt("https://example.org/art.png")
            .contentItemType("stationurl")
            .location("/v1/playback/station/s111")
            .name("Radio Station")
            .sourceId("source-1")
            .build();

    Preset saved = presetService.savePreset(initial);
    Long initialId = saved.id();

    // When - save different contentItemType at same button (different content)
    Preset update =
        Preset.builder()
            .accountId(ACCOUNT_ID)
            .deviceId(DEVICE_ID)
            .buttonNumber(1) // Same button
            .containerArt("https://example.org/playlist-art.png")
            .contentItemType("tracklisturl") // Different contentItemType = different content
            .location("/v1/playback/station/s111") // Same location
            .name("Playlist")
            .sourceId("source-1") // Same sourceId
            .build();

    Preset updated = presetService.savePreset(update);

    // Then - should replace the content (same ID but all content fields changed)
    assertThat(updated.id()).isEqualTo(initialId); // Same ID (replaced, not new record)
    assertThat(updated.buttonNumber()).isEqualTo(1); // Same button
    assertThat(updated.location()).isEqualTo("/v1/playback/station/s111"); // Same location
    assertThat(updated.sourceId()).isEqualTo("source-1"); // Same sourceId
    assertThat(updated.contentItemType()).isEqualTo("tracklisturl"); // Changed - new content
    assertThat(updated.name()).isEqualTo("Playlist"); // Changed
    assertThat(updated.containerArt()).isEqualTo("https://example.org/playlist-art.png"); // Changed
    assertThat(updated.createdOn()).isEqualTo(saved.createdOn()); // Original createdOn preserved

    // Verify only one preset exists
    List<Preset> presets = presetService.getPresets(ACCOUNT_ID, DEVICE_ID);
    assertThat(presets).hasSize(1);
  }

  @Test
  void savePreset_shouldUpdateExistingPreset() {
    // Given - create initial preset
    Preset initial =
        Preset.builder()
            .accountId(ACCOUNT_ID)
            .deviceId(DEVICE_ID)
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
            .accountId(ACCOUNT_ID)
            .deviceId(DEVICE_ID)
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
    List<Preset> presets = presetService.getPresets(ACCOUNT_ID, DEVICE_ID);
    assertThat(presets).hasSize(1);
  }

  @Test
  void prestCanBeMoved() {
    var initial1 =
        Preset.builder()
            .accountId(ACCOUNT_ID)
            .deviceId(DEVICE_ID)
            .buttonNumber(1)
            .containerArt("https://example.org/art.png")
            .contentItemType("stationurl")
            .location("/v1/playback/station/s111")
            .name("My Name")
            .sourceId("source-1")
            .build();
    presetService.savePreset(initial1);

    var initial1NowAt2 =
        Preset.builder()
            .accountId(ACCOUNT_ID)
            .deviceId(DEVICE_ID)
            .buttonNumber(2)
            .containerArt("https://example.org/art.png")
            .contentItemType("stationurl")
            .location("/v1/playback/station/s111")
            .name("My Name")
            .sourceId("source-1")
            .build();
    presetService.savePreset(initial1NowAt2);

    List<Preset> presets = presetService.getPresets(ACCOUNT_ID, DEVICE_ID);
    assertThat(presets).hasSize(1).extracting(Preset::buttonNumber).containsExactly(2);
  }

  @Test
  void prestCanBeMovedToAlreadyUsedPlace() {
    var initialOne =
        Preset.builder()
            .accountId(ACCOUNT_ID)
            .deviceId(DEVICE_ID)
            .buttonNumber(1)
            .containerArt("https://example.org/art.png")
            .contentItemType("stationurl")
            .location("/v1/playback/station/s111")
            .name("My Name")
            .sourceId("source-1")
            .build();
    presetService.savePreset(initialOne);

    var initialTwo =
        Preset.builder()
            .accountId(ACCOUNT_ID)
            .deviceId(DEVICE_ID)
            .buttonNumber(2)
            .containerArt("https://example.org/art2.png")
            .contentItemType("stationurl")
            .location("/v1/playback/station/s222")
            .name("My Name 2")
            .sourceId("source-2")
            .build();
    presetService.savePreset(initialTwo);

    var initialOneNowAt2 =
        Preset.builder()
            .accountId(ACCOUNT_ID)
            .deviceId(DEVICE_ID)
            .buttonNumber(2)
            .containerArt("https://example.org/art.png")
            .contentItemType("stationurl")
            .location("/v1/playback/station/s111")
            .name("My Name")
            .sourceId("source-1")
            .build();
    presetService.savePreset(initialOneNowAt2);

    List<Preset> presets = presetService.getPresets(ACCOUNT_ID, DEVICE_ID);
    assertThat(presets)
        .hasSize(1)
        .extracting(Preset::buttonNumber, Preset::name)
        .containsExactly(Tuple.tuple(2, "My Name"));
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
    savePreset(ACCOUNT_ID, DEVICE_ID, 1, "Button 1", "source-1");
    savePreset(ACCOUNT_ID, DEVICE_ID, 2, "Button 2", "source-2");

    // When
    Optional<Preset> preset = presetService.getPreset(ACCOUNT_ID, DEVICE_ID, 1);

    // Then
    assertThat(preset).isPresent();
    assertThat(preset.get().name()).isEqualTo("Button 1");
    assertThat(preset.get().buttonNumber()).isEqualTo(1);
  }

  @Test
  void getPreset_shouldReturnEmptyForNonExistentButton() {
    // Given
    String accountId = "test-account-missing";
    savePreset(accountId, DEVICE_ID, 1, "Button 1", "source-1");

    // When
    Optional<Preset> preset = presetService.getPreset(accountId, DEVICE_ID, 99);

    // Then
    assertThat(preset).isEmpty();
  }

  @Test
  void savePreset_shouldNotAffectDifferentDevices() {
    // Given
    // Save presets for different devices with same button numbers
    savePreset(ACCOUNT_ID, "device-1", 1, "Device 1 Button 1", "source-1");
    savePreset(ACCOUNT_ID, "device-2", 1, "Device 2 Button 1", "source-2");

    // When
    List<Preset> device1Presets = presetService.getPresets(ACCOUNT_ID, "device-1");
    List<Preset> device2Presets = presetService.getPresets(ACCOUNT_ID, "device-2");

    // Then
    assertThat(device1Presets).hasSize(1);
    assertThat(device1Presets.getFirst().name()).isEqualTo("Device 1 Button 1");

    assertThat(device2Presets).hasSize(1);
    assertThat(device2Presets.getFirst().name()).isEqualTo("Device 2 Button 1");
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
    assertThat(account1Presets.getFirst().name()).isEqualTo("Account 1 Button 1");

    assertThat(account2Presets).hasSize(1);
    assertThat(account2Presets.getFirst().name()).isEqualTo("Account 2 Button 1");
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
