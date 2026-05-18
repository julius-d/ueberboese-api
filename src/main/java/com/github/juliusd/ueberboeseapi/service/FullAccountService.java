package com.github.juliusd.ueberboeseapi.service;

import static java.util.stream.Collectors.toUnmodifiableSet;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.juliusd.ueberboeseapi.ProxyService;
import com.github.juliusd.ueberboeseapi.SourceProvider;
import com.github.juliusd.ueberboeseapi.device.Device;
import com.github.juliusd.ueberboeseapi.device.DeviceRepository;
import com.github.juliusd.ueberboeseapi.generated.dtos.AttachedProductApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.CredentialApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.DeviceApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.DevicesContainerApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.FullAccountResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetsContainerApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentItemApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentsContainerApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourcesContainerApiDto;
import com.github.juliusd.ueberboeseapi.preset.Preset;
import com.github.juliusd.ueberboeseapi.preset.PresetMapper;
import com.github.juliusd.ueberboeseapi.preset.PresetService;
import com.github.juliusd.ueberboeseapi.recent.Recent;
import com.github.juliusd.ueberboeseapi.recent.RecentMapper;
import com.github.juliusd.ueberboeseapi.recent.RecentService;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyAccount;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyAccountService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for managing full account data operations. Handles caching, proxy forwarding, and XML
 * parsing for account data requests.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FullAccountService {

  private static final String SPOTIFY_PROVIDER_ID = String.valueOf(SourceProvider.SPOTIFY.getId());

  private final AccountDataService accountDataService;
  private final ProxyService proxyService;
  private final XmlMapper xmlMapper;
  private final SpotifyAccountService spotifyAccountService;
  private final RecentService recentService;
  private final RecentMapper recentMapper;
  private final PresetService presetService;
  private final PresetMapper presetMapper;
  private final DeviceRepository deviceRepository;

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

    String clientIp = request.getHeader("X-Forwarded-For");
    if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getRemoteAddr();
    }
    if (clientIp != null && clientIp.contains(",")) {
      clientIp = clientIp.split(",")[0].trim();
    }

    log.info("Processing full account for accountId: {} linked to IP: {}", accountId, clientIp);

    var minimal = buildMinimalAccount(accountId);

    injectDevicesFromDatabase(minimal, accountId, clientIp);

    injectSpotifySources(minimal);
    injectRecentsFromDatabase(minimal, accountId);
    injectPresetsFromDatabase(minimal, accountId);
    patch(minimal);

    return Optional.of(minimal);
  }

  private void injectDevicesFromDatabase(
      FullAccountResponseApiDto response, String accountId, String clientIp) {
    if (response.getDevices() == null) {
      response.setDevices(new DevicesContainerApiDto());
    }

    Set<String> existingDeviceIds;
    if (response.getDevices().getDevice() != null) {
      existingDeviceIds =
          response.getDevices().getDevice().stream()
              .map(DeviceApiDto::getDeviceid)
              .filter(Objects::nonNull)
              .collect(toUnmodifiableSet());
    } else {
      existingDeviceIds = Set.of();
    }

    List<Device> dbDevices = deviceRepository.findAllByMargeAccountId(accountId);

    boolean ipExistsInDb =
        dbDevices.stream().anyMatch(d -> d.ipAddress() != null && d.ipAddress().equals(clientIp));

    if (!ipExistsInDb) {
      log.warn(
          "Client IP {} not found in database for account {}. Falling back to returning all devices.",
          clientIp,
          accountId);
    }

    for (Device device : dbDevices) {
      if (ipExistsInDb && (device.ipAddress() == null || !device.ipAddress().equals(clientIp))) {
        continue;
      }

      if (!existingDeviceIds.contains(device.deviceId())) {
        var deviceDto = new DeviceApiDto();
        deviceDto.setDeviceid(device.deviceId());
        deviceDto.setName(device.name());
        deviceDto.setIpaddress(device.ipAddress());
        deviceDto.setCreatedOn(device.firstSeen());
        deviceDto.setUpdatedOn(device.updatedOn());
        deviceDto.setFirmwareVersion(device.firmwareVersion());
        deviceDto.setSerialNumber(device.deviceSerialNumber());

        if (device.productCode() != null || device.productSerialNumber() != null) {
          var attachedProduct = new AttachedProductApiDto();
          attachedProduct.setProductCode(device.productCode());
          attachedProduct.setSerialnumber(device.productSerialNumber());
          deviceDto.setAttachedProduct(attachedProduct);
        }

        deviceDto.setPresets(new PresetsContainerApiDto());
        deviceDto.setRecents(new RecentsContainerApiDto());
        response.getDevices().addDeviceItem(deviceDto);

        log.info(
            "Injected device {} (IP: {}) into full account for accountId: {}",
            device.deviceId(),
            device.ipAddress(),
            accountId);
      }
    }
  }

  private void injectRecentsFromDatabase(FullAccountResponseApiDto response, String accountId) {
    if (response.getDevices() == null || response.getDevices().getDevice() == null) {
      return;
    }

    // Fetch recents from database (shared across all devices)
    List<Recent> recents = recentService.getRecents(accountId);
    List<SourceApiDto> sources =
        (response.getSources() != null && response.getSources().getSource() != null)
            ? response.getSources().getSource()
            : List.of();
    List<RecentItemApiDto> recentDtos = recentMapper.convertToApiDtos(recents, sources);

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
      log.warn(
          "Aborting preset injection: devices container or device list is NULL for accountId: {}",
          accountId);
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

  private void injectSpotifySources(FullAccountResponseApiDto response) {
    if (response.getSources() == null) {
      response.setSources(new SourcesContainerApiDto());
    }
    List<SpotifyAccount> spotifyAccounts = spotifyAccountService.listAllAccounts();
    addMissingSpotifySources(response, spotifyAccounts);
  }

  private void patch(FullAccountResponseApiDto response) {
    List<SpotifyAccount> spotifyAccounts = spotifyAccountService.listAllAccounts();

    Map<String, SpotifyAccount> userIdToAccount =
        spotifyAccounts.stream()
            .collect(Collectors.toMap(SpotifyAccount::spotifyUserId, account -> account));

    int patchedCount = 0;

    if (response.getSources() != null && response.getSources().getSource() != null) {
      for (SourceApiDto source : response.getSources().getSource()) {
        if (patchSource(source, userIdToAccount)) {
          patchedCount++;
        }
      }
    }

    if (response.getDevices() != null && response.getDevices().getDevice() != null) {
      for (var device : response.getDevices().getDevice()) {
        if (device.getPresets() != null && device.getPresets().getPreset() != null) {
          for (var preset : device.getPresets().getPreset()) {
            if (preset.getSource() != null && patchSource(preset.getSource(), userIdToAccount)) {
              patchedCount++;
            }
          }
        }

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

  private void addMissingSpotifySources(
      FullAccountResponseApiDto response, List<SpotifyAccount> spotifyAccounts) {
    Set<String> existingSpotifyUsernames =
        response.getSources().getSource() != null
            ? response.getSources().getSource().stream()
                .filter(s -> SPOTIFY_PROVIDER_ID.equals(s.getSourceproviderid()))
                .map(SourceApiDto::getUsername)
                .filter(Objects::nonNull)
                .collect(toUnmodifiableSet())
            : Set.of();

    int nextId = 10;
    for (SpotifyAccount account : spotifyAccounts) {
      if (existingSpotifyUsernames.contains(account.spotifyUserId())) {
        continue;
      }
      response
          .getSources()
          .addSourceItem(
              new SourceApiDto()
                  .id(String.valueOf(nextId++))
                  .type("Audio")
                  .sourceproviderid(SPOTIFY_PROVIDER_ID)
                  .username(account.spotifyUserId())
                  .sourcename(account.displayName())
                  .createdOn(account.createdAt())
                  .updatedOn(account.updatedAt())
                  .credential(new CredentialApiDto("token", account.refreshToken())));
      log.info("Added Spotify source for user: {}", account.spotifyUserId());
    }
  }

  private FullAccountResponseApiDto buildMinimalAccount(String accountId) {
    var response = new FullAccountResponseApiDto();
    response.setId(accountId);
    response.setAccountStatus("ACTIVE");
    response.setMode("global");
    response.setPreferredLanguage("en");
    response.setDevices(new DevicesContainerApiDto());
    var sources = new SourcesContainerApiDto();
    sources.addSourceItem(
        new SourceApiDto()
            .id("1")
            .type("Audio")
            .createdOn(OffsetDateTime.parse("2018-08-11T08:55:41+00:00"))
            .updatedOn(OffsetDateTime.parse("2019-07-20T17:48:31+00:00"))
            .sourceproviderid(String.valueOf(SourceProvider.TUNEIN.getId()))
            .credential(new CredentialApiDto("token", "eyJduTune=")));
    response.setSources(sources);
    return response;
  }

  /**
   * Patches a single Spotify source with updated credentials and timestamp from stored account.
   *
   * @param source The source to patch
   * @param userIdToAccount Map of Spotify user IDs to accounts
   * @return true if the source was patched, false otherwise
   */
  private boolean patchSource(SourceApiDto source, Map<String, SpotifyAccount> userIdToAccount) {
    if (!SPOTIFY_PROVIDER_ID.equals(source.getSourceproviderid())) {
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
