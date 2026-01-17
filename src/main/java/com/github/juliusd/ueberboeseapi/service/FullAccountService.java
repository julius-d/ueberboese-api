package com.github.juliusd.ueberboeseapi.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.juliusd.ueberboeseapi.ProxyService;
import com.github.juliusd.ueberboeseapi.generated.dtos.FullAccountResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetsContainerApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentItemApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentsContainerApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceApiDto;
import com.github.juliusd.ueberboeseapi.preset.Preset;
import com.github.juliusd.ueberboeseapi.preset.PresetMapper;
import com.github.juliusd.ueberboeseapi.preset.PresetService;
import com.github.juliusd.ueberboeseapi.recent.Recent;
import com.github.juliusd.ueberboeseapi.recent.RecentMapper;
import com.github.juliusd.ueberboeseapi.recent.RecentService;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyAccount;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyAccountService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Service for managing full account data operations. Handles caching, proxy forwarding, and XML
 * parsing for account data requests.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FullAccountService {

  private final AccountDataService accountDataService;
  private final ProxyService proxyService;
  private final XmlMapper xmlMapper;
  private final SpotifyAccountService spotifyAccountService;
  private final RecentService recentService;
  private final RecentMapper recentMapper;
  private final PresetService presetService;
  private final PresetMapper presetMapper;

  /**
   * Retrieves full account data for the given account ID. First checks the cache, and if not found,
   * forwards the request to the proxy service.
   *
   * @param accountId The account ID to retrieve data for
   * @param request The HTTP servlet request (needed for proxy forwarding)
   * @return Optional containing the account data if successful, empty otherwise
   */
  public Optional<FullAccountResponseApiDto> getFullAccount(
      String accountId, HttpServletRequest request) {
    log.info("Getting full account data for accountId: {}", accountId);

    // Check if cached data exists
    if (accountDataService.hasAccountData(accountId)) {
      try {
        FullAccountResponseApiDto response = accountDataService.loadFullAccountData(accountId);
        log.info("Successfully loaded account data from cache for accountId: {}", accountId);
        injectRecentsFromDatabase(response, accountId);
        injectPresetsFromDatabase(response, accountId);
        patch(response);
        return Optional.of(response);
      } catch (IOException e) {
        log.error(
            "Failed to load account data from cache for accountId: {}, error: {}",
            accountId,
            e.getMessage());
        return Optional.empty();
      }
    }

    // Cache miss - forward request to proxy
    log.info("Cache miss for accountId: {}, forwarding request to proxy", accountId);
    ResponseEntity<byte[]> proxyResponse = proxyService.forwardRequest(request, null);

    // Check if proxy response is successful
    if (!proxyResponse.getStatusCode().is2xxSuccessful() || proxyResponse.getBody() == null) {
      log.warn(
          "Proxy request failed for accountId: {}, status: {}",
          accountId,
          proxyResponse.getStatusCode());
      return Optional.empty();
    }

    // Try to parse and cache the response
    try {
      String xmlContent = new String(proxyResponse.getBody());
      FullAccountResponseApiDto parsedResponse =
          xmlMapper.readValue(xmlContent, FullAccountResponseApiDto.class);

      // Cache the response for future use
      try {
        accountDataService.saveFullAccountDataRaw(accountId, xmlContent);
        log.info("Successfully cached account data for accountId: {}", accountId);
      } catch (Exception saveException) {
        log.error(
            "Failed to cache account data for accountId: {}, continuing with response. Error: {}",
            accountId,
            saveException.getMessage());
      }

      // Inject database recents and presets before patching
      injectRecentsFromDatabase(parsedResponse, accountId);
      injectPresetsFromDatabase(parsedResponse, accountId);
      // Apply Spotify patches
      patch(parsedResponse);

      return Optional.of(parsedResponse);
    } catch (Exception parseException) {
      log.error(
          "Failed to parse proxy response for accountId: {}. Error: {}",
          accountId,
          parseException.getMessage());
      return Optional.empty();
    }
  }

  private void injectRecentsFromDatabase(FullAccountResponseApiDto response, String accountId) {
    if (response.getDevices() == null || response.getDevices().getDevice() == null) {
      return;
    }

    // Fetch recents from database (shared across all devices)
    List<Recent> recents = recentService.getRecents(accountId);
    List<RecentItemApiDto> recentDtos =
        recentMapper.convertToApiDtos(recents, response.getSources().getSource());

    // Build a map of source ID -> source from the account's sources
    Map<String, SourceApiDto> sourcesById = new HashMap<>();
    if (response.getSources() != null && response.getSources().getSource() != null) {
      for (SourceApiDto source : response.getSources().getSource()) {
        if (source.getId() != null) {
          sourcesById.put(source.getId(), source);
        }
      }
    }

    // Replace mock sources in recents with actual sources from the account
    for (RecentItemApiDto recent : recentDtos) {
      if (recent.getSourceid() != null && sourcesById.containsKey(recent.getSourceid())) {
        // Replace the mock source with the actual source from the account
        recent.setSource(sourcesById.get(recent.getSourceid()));
      }
    }

    // Replace recents in ALL devices with the same list
    RecentsContainerApiDto recentsContainer = new RecentsContainerApiDto();
    recentDtos.forEach(recentsContainer::addRecentItem);

    for (var device : response.getDevices().getDevice()) {
      device.setRecents(recentsContainer);
    }

    log.info("Injected {} recents into full account for accountId: {}", recents.size(), accountId);
  }

  private void injectPresetsFromDatabase(FullAccountResponseApiDto response, String accountId) {
    if (response.getDevices() == null || response.getDevices().getDevice() == null) {
      return;
    }

    // Get sources from account for enrichment
    List<SourceApiDto> sources =
        (response.getSources() != null && response.getSources().getSource() != null)
            ? response.getSources().getSource()
            : List.of();

    // Build a map of source ID -> source from the account's sources
    Map<String, SourceApiDto> sourcesById = new HashMap<>();
    for (SourceApiDto source : sources) {
      if (source.getId() != null) {
        sourcesById.put(source.getId(), source);
      }
    }

    // For each device, inject and merge presets from database
    for (var device : response.getDevices().getDevice()) {
      String deviceId = device.getDeviceid();

      // Fetch presets from database for this device
      List<Preset> dbPresets = presetService.getPresets(accountId, deviceId);
      List<PresetApiDto> dbPresetDtos = presetMapper.convertToApiDtos(dbPresets, sources);

      // Replace mock sources in presets with actual sources from the account
      for (PresetApiDto preset : dbPresetDtos) {
        if (preset.getSource() != null
            && preset.getSource().getId() != null
            && sourcesById.containsKey(preset.getSource().getId())) {
          preset.setSource(sourcesById.get(preset.getSource().getId()));
        }
      }

      // Merge DB presets with XML presets (DB takes precedence by buttonNumber)
      PresetsContainerApiDto mergedPresets =
          presetMapper.mergePresets(device.getPresets(), dbPresetDtos);
      device.setPresets(mergedPresets);

      log.info(
          "Injected {} DB presets into device {} for accountId: {}",
          dbPresets.size(),
          deviceId,
          accountId);
    }

    log.info("Injected presets from database into full account for accountId: {}", accountId);
  }

  private void patch(FullAccountResponseApiDto response) {
    // Get all stored Spotify accounts
    List<SpotifyAccount> spotifyAccounts = spotifyAccountService.listAllAccounts();

    // Create a map for efficient lookup: spotifyUserId -> SpotifyAccount
    Map<String, SpotifyAccount> userIdToAccount =
        spotifyAccounts.stream()
            .collect(Collectors.toMap(SpotifyAccount::spotifyUserId, account -> account));

    int patchedCount = 0;

    // Patch top-level sources
    if (response.getSources() != null && response.getSources().getSource() != null) {
      for (SourceApiDto source : response.getSources().getSource()) {
        if (patchSource(source, userIdToAccount)) {
          patchedCount++;
        }
      }
    }

    // Patch nested sources in device presets and recents
    if (response.getDevices() != null && response.getDevices().getDevice() != null) {
      for (var device : response.getDevices().getDevice()) {
        // Patch preset sources
        if (device.getPresets() != null && device.getPresets().getPreset() != null) {
          for (var preset : device.getPresets().getPreset()) {
            if (preset.getSource() != null && patchSource(preset.getSource(), userIdToAccount)) {
              patchedCount++;
            }
          }
        }

        // Patch recent sources
        if (device.getRecents() != null && device.getRecents().getRecent() != null) {
          for (var recent : device.getRecents().getRecent()) {
            if (recent.getSource() != null && patchSource(recent.getSource(), userIdToAccount)) {
              patchedCount++;
            }
          }
        }
      }
    }

    if (patchedCount > 0) {
      log.info("Patched {} Spotify sources with updated credentials and timestamps", patchedCount);
    }
  }

  /**
   * Patches a single Spotify source with updated credentials and timestamp from stored account.
   *
   * @param source The source to patch
   * @param userIdToAccount Map of Spotify user IDs to accounts
   * @return true if the source was patched, false otherwise
   */
  private boolean patchSource(SourceApiDto source, Map<String, SpotifyAccount> userIdToAccount) {
    // Check if this is a Spotify source (sourceproviderid == "15")
    if (!"15".equals(source.getSourceproviderid())) {
      return false;
    }

    String username = source.getUsername();

    // Check if we have a stored account for this user
    if (username == null || !userIdToAccount.containsKey(username)) {
      return false;
    }

    SpotifyAccount account = userIdToAccount.get(username);

    // Update the credential value
    if (source.getCredential() != null) {
      source.getCredential().setValue(account.refreshToken());
    }

    // Update the updatedOn timestamp
    source.setUpdatedOn(account.updatedAt());

    return true;
  }
}
