package com.github.juliusd.ueberboeseapi.preset;

import com.github.juliusd.ueberboeseapi.generated.dtos.CredentialApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetsContainerApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceApiDto;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class PresetMapper {
  public List<PresetApiDto> convertToApiDtos(List<Preset> presets, List<SourceApiDto> sources) {
    return presets.stream().map(preset -> convertToApiDto(preset, sources)).toList();
  }

  private PresetApiDto convertToApiDto(Preset preset, List<SourceApiDto> sources) {

    SourceApiDto source =
        sources.stream()
            .filter(it -> it.getId().equals(preset.sourceId()))
            .findFirst()
            .orElseGet(() -> createMockSource(preset));

    // Create preset item
    PresetApiDto presetItem = new PresetApiDto();
    presetItem.setButtonNumber(preset.buttonNumber());
    presetItem.setContainerArt(preset.containerArt());
    presetItem.setContentItemType(preset.contentItemType());
    presetItem.setCreatedOn(preset.createdOn());
    presetItem.setLocation(preset.location());
    presetItem.setName(preset.name());
    presetItem.setUpdatedOn(preset.updatedOn());
    presetItem.setSource(source);
    presetItem.setUsername(source.getUsername() != null ? source.getUsername() : "");

    return presetItem;
  }

  private static @NonNull SourceApiDto createMockSource(Preset preset) {
    CredentialApiDto credential = new CredentialApiDto();
    SourceApiDto source = new SourceApiDto();
    source.setId(preset.sourceId());
    source.setType("Audio");
    source.setCreatedOn(OffsetDateTime.parse("2018-08-11T08:55:28.000+00:00"));
    source.setUpdatedOn(OffsetDateTime.parse("2019-07-20T17:48:31.000+00:00"));

    // Set source-specific mock data based on sourceId
    if ("19989643".equals(preset.sourceId())) {
      // Spotify source (user1namespot)
      credential.setType("token_version_3");
      credential.setValue("mockTokenUser2");
      source.setCredential(credential);
      source.setName("user1namespot");
      source.setSourceproviderid("15");
      source.setSourcename("user1@example.org");
      source.setUsername("user1namespot");
    } else if ("19989342".equals(preset.sourceId())) {
      // TuneIn source
      credential.setType("token");
      credential.setValue("eyJduTune=");
      source.setCredential(credential);
      source.setName("");
      source.setSourceproviderid("25");
      source.setSourcename("");
      source.setUsername("");
    } else {
      // Default source
      credential.setType("token");
      credential.setValue("eyDu=");
      source.setCredential(credential);
      source.setName("");
      source.setSourceproviderid("25");
      source.setSourcename("");
      source.setUsername("");
    }
    return source;
  }

  /**
   * Merges database presets with XML presets. Database presets take precedence over XML presets for
   * matching button numbers.
   *
   * @param xmlPresets The presets from XML (may be null)
   * @param dbPresets The presets from database
   * @return A merged container with DB presets overriding XML presets by buttonNumber
   */
  public PresetsContainerApiDto mergePresets(
      PresetsContainerApiDto xmlPresets, List<PresetApiDto> dbPresets) {
    PresetsContainerApiDto result = new PresetsContainerApiDto();

    // Build a map of DB presets by buttonNumber
    Map<Integer, PresetApiDto> dbPresetsByButton =
        dbPresets.stream()
            .collect(Collectors.toMap(PresetApiDto::getButtonNumber, preset -> preset));

    // Start with XML presets (if any)
    List<PresetApiDto> allPresets = new ArrayList<>();
    if (xmlPresets != null && xmlPresets.getPreset() != null) {
      for (PresetApiDto xmlPreset : xmlPresets.getPreset()) {
        // Skip XML preset if DB has a preset for the same buttonNumber
        if (!dbPresetsByButton.containsKey(xmlPreset.getButtonNumber())) {
          allPresets.add(xmlPreset);
        }
      }
    }

    // Add all DB presets (they override XML)
    allPresets.addAll(dbPresets);

    // Sort by buttonNumber for consistent ordering
    allPresets.sort((a, b) -> Integer.compare(a.getButtonNumber(), b.getButtonNumber()));

    // Add to container
    allPresets.forEach(result::addPresetItem);

    return result;
  }
}
