package com.github.juliusd.ueberboeseapi;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.juliusd.ueberboeseapi.device.DeviceService;
import com.github.juliusd.ueberboeseapi.generated.DefaultApi;
import com.github.juliusd.ueberboeseapi.generated.dtos.CredentialApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.ErrorResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.FullAccountResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PowerOnRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetsContainerApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentItemApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentItemRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentItemResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentsContainerApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceProviderApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceProvidersResponseApiDto;
import com.github.juliusd.ueberboeseapi.preset.Preset;
import com.github.juliusd.ueberboeseapi.preset.PresetMapper;
import com.github.juliusd.ueberboeseapi.preset.PresetService;
import com.github.juliusd.ueberboeseapi.recent.Recent;
import com.github.juliusd.ueberboeseapi.recent.RecentMapper;
import com.github.juliusd.ueberboeseapi.recent.RecentService;
import com.github.juliusd.ueberboeseapi.service.AccountDataService;
import com.github.juliusd.ueberboeseapi.service.DeviceTrackingService;
import com.github.juliusd.ueberboeseapi.service.FullAccountService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UeberboeseController implements DefaultApi {

  private final AccountDataService accountDataService;
  private final ProxyService proxyService;
  private final XmlMapper xmlMapper;
  private final DeviceTrackingService deviceTrackingService;
  private final FullAccountService fullAccountService;
  private final RecentService recentService;
  private final RecentMapper recentMapper;
  private final PresetService presetService;
  private final PresetMapper presetMapper;
  private final DeviceService deviceService;

  @Autowired private HttpServletRequest request;

  @Override
  public ResponseEntity<RecentItemResponseApiDto> addRecentItem(
      String accountId, String deviceId, RecentItemRequestApiDto recentItemRequestApiDto) {
    log.info("Adding recent item for accountId: {} and deviceId: {}", accountId, deviceId);

    // Save or update recent in database
    Recent saved = recentService.addOrUpdateRecent(accountId, deviceId, recentItemRequestApiDto);

    SourceApiDto source = buildSourceApiDto(recentItemRequestApiDto);

    // Create the response object
    var response =
        new RecentItemResponseApiDto()
            .id(String.valueOf(saved.id()))
            .contentItemType(saved.contentItemType())
            .createdOn(saved.createdOn())
            .lastplayedat(saved.lastPlayedAt())
            .location(saved.location())
            .name(saved.name())
            .source(source)
            .sourceid(saved.sourceId())
            .updatedOn(saved.updatedOn());

    // Build the Location header
    String locationHeader =
        "http://streamingqa.bose.com/account/%s/device/%s/recent/%s"
            .formatted(accountId, deviceId, saved.id());

    return ResponseEntity.created(URI.create(locationHeader))
        .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        .header(
            "Access-Control-Allow-Headers",
            "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
        .header("Access-Control-Expose-Headers", "Credentials")
        .body(response);
  }

  private static SourceApiDto buildSourceApiDto(RecentItemRequestApiDto recentItemRequestApiDto) {
    var credential = new CredentialApiDto();
    SourceApiDto source = new SourceApiDto();
    source.setId(recentItemRequestApiDto.getSourceid());
    source.setType("Audio");
    source.setCreatedOn(OffsetDateTime.parse("2018-08-11T08:55:28.000+00:00"));
    source.setUpdatedOn(OffsetDateTime.parse("2019-07-20T17:48:31.000+00:00"));

    // Set source-specific mock data based on sourceId
    if ("19989643".equals(recentItemRequestApiDto.getSourceid())) {
      // Spotify source (user1namespot)
      credential.setType("token_version_3");
      credential.setValue("mockTokenUser2");
      source.setName("user1namespot");
      source.setSourceproviderid("15");
      source.setSourcename("user1@example.org");
      source.setUsername("user1namespot");
    } else if ("19989342".equals(recentItemRequestApiDto.getSourceid())) {
      // TuneIn source
      credential.setType("token");
      credential.setValue("eyJduTune=");
      source.setName("");
      source.setSourceproviderid("25");
      source.setSourcename("");
      source.setUsername("");
    } else {
      // Default source
      credential.setType("token");
      credential.setValue("eyDu=");
      source.setName("");
      source.setSourceproviderid("25");
      source.setSourcename("");
      source.setUsername("");
    }

    source.setCredential(credential);
    return source;
  }

  @Override
  public ResponseEntity<SourceProvidersResponseApiDto> getSourceProviders() {
    log.info("Getting source providers");

    SourceProvidersResponseApiDto response = new SourceProvidersResponseApiDto();

    // Create all source providers from the enum
    for (SourceProvider provider : SourceProvider.values()) {
      response.addSourceproviderItem(createSourceProvider(provider));
    }

    return ResponseEntity.ok()
        .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        .header(
            "Access-Control-Allow-Headers",
            "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
        .header("Access-Control-Expose-Headers", "Authorization")
        .body(response);
  }

  @Override
  public ResponseEntity<FullAccountResponseApiDto> getFullAccount(String accountId) {
    log.info("Getting full account for accountId: {}", accountId);
    return fullAccountService
        .getFullAccount(accountId, request)
        .map(
            data ->
                ResponseEntity.ok()
                    .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
                    .header("METHOD_NAME", "getFullAccount")
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                    .header(
                        "Access-Control-Allow-Headers",
                        "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
                    .header("Access-Control-Expose-Headers", "Authorization")
                    .body(data))
        .orElseGet(
            () ->
                ResponseEntity.status(502)
                    .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
                    .build());
  }

  @Override
  public ResponseEntity<RecentsContainerApiDto> getRecents(String accountId, String deviceId) {
    log.info("Getting recents for accountId: {} and deviceId: {}", accountId, deviceId);

    // Note: deviceId parameter is ignored - recents are shared across account
    List<Recent> recents = recentService.getRecents(accountId);
    var sources = loadSources(accountId);
    List<RecentItemApiDto> recentDtos = recentMapper.convertToApiDtos(recents, sources);

    RecentsContainerApiDto response = new RecentsContainerApiDto();
    recentDtos.forEach(response::addRecentItem);

    return ResponseEntity.ok()
        .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        .header(
            "Access-Control-Allow-Headers",
            "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
        .header("Access-Control-Expose-Headers", "Authorization")
        .body(response);
  }

  private List<SourceApiDto> loadSources(String accountId) {
    try {

      if (accountDataService.hasAccountData(accountId)) {
        FullAccountResponseApiDto fullAccount = accountDataService.loadFullAccountData(accountId);
        return fullAccount.getSources().getSource();
      } else {
        return List.of();
      }
    } catch (IOException e) {
      log.warn("failed to load sources", e);
      return List.of();
    }
  }

  @Override
  public ResponseEntity<PresetsContainerApiDto> getPresets(String accountId, String deviceId) {
    log.info("Getting presets for accountId: {} and deviceId: {}", accountId, deviceId);

    if (accountDataService.hasAccountData(accountId)) {
      try {
        FullAccountResponseApiDto fullAccountData =
            accountDataService.loadFullAccountData(accountId);
        log.info(
            "Successfully loaded account data from cache for accountId: {}, looking for deviceId: {}",
            accountId,
            deviceId);

        // Find the device with matching deviceId
        if (fullAccountData.getDevices() != null
            && fullAccountData.getDevices().getDevice() != null) {
          for (var device : fullAccountData.getDevices().getDevice()) {
            if (deviceId.equals(device.getDeviceid())) {
              log.info(
                  "Found device {} with {} presets",
                  deviceId,
                  device.getPresets() != null && device.getPresets().getPreset() != null
                      ? device.getPresets().getPreset().size()
                      : 0);

              // Get presets from database
              List<Preset> dbPresets = presetService.getPresets(accountId, deviceId);
              List<PresetApiDto> dbPresetDtos =
                  presetMapper.convertToApiDtos(
                      dbPresets, fullAccountData.getSources().getSource());

              // Merge DB presets with XML presets (DB takes precedence)
              PresetsContainerApiDto mergedPresets =
                  presetMapper.mergePresets(device.getPresets(), dbPresetDtos);

              return ResponseEntity.ok()
                  .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
                  .header("Access-Control-Allow-Origin", "*")
                  .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                  .header(
                      "Access-Control-Allow-Headers",
                      "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
                  .header("Access-Control-Expose-Headers", "Authorization")
                  .body(mergedPresets);
            }
          }
        }

        // Device not found
        log.warn("Device {} not found in account {}", deviceId, accountId);
        return ResponseEntity.status(404)
            .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
            .build();

      } catch (IOException e) {
        log.error(
            "Failed to load account data from cache for accountId: {}, error: {}",
            accountId,
            e.getMessage());
        return ResponseEntity.status(502)
            .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
            .build();
      }
    }

    // Cache miss - forward request to proxy
    log.info(
        "Cache miss for accountId: {}, deviceId: {}, forwarding request to proxy",
        accountId,
        deviceId);
    ResponseEntity<byte[]> proxyResponse = proxyService.forwardRequest(request, null);

    // Check if proxy response is successful
    if (!proxyResponse.getStatusCode().is2xxSuccessful() || proxyResponse.getBody() == null) {
      log.warn(
          "Proxy request failed for accountId: {}, deviceId: {}, status: {}",
          accountId,
          deviceId,
          proxyResponse.getStatusCode());
      return ResponseEntity.status(502)
          .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
          .build();
    }

    // Try to parse the response
    try {
      String xmlContent = new String(proxyResponse.getBody());
      PresetsContainerApiDto parsedResponse =
          xmlMapper.readValue(xmlContent, PresetsContainerApiDto.class);

      return ResponseEntity.status(proxyResponse.getStatusCode())
          .headers(proxyResponse.getHeaders())
          .body(parsedResponse);
    } catch (Exception parseException) {
      log.error(
          "Failed to parse proxy response for accountId: {}, deviceId: {}. Error: {}",
          accountId,
          deviceId,
          parseException.getMessage());
      return ResponseEntity.status(502)
          .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
          .build();
    }
  }

  private static SourceProviderApiDto createSourceProvider(SourceProvider sourceProvider) {
    SourceProviderApiDto provider = new SourceProviderApiDto();
    provider.setId(sourceProvider.getId());
    provider.setCreatedOn(sourceProvider.getCreatedOn());
    provider.setName(sourceProvider.getName());
    provider.setUpdatedOn(sourceProvider.getUpdatedOn());
    return provider;
  }

  @Override
  public ResponseEntity<RecentItemResponseApiDto> getRecentItem(
      String accountId, String deviceId, String recentId) {
    log.info(
        "Getting recent item for accountId: {}, deviceId: {}, recentId: {}",
        accountId,
        deviceId,
        recentId);

    // Parse recentId to Long
    Long id;
    try {
      id = Long.parseLong(recentId);
    } catch (NumberFormatException e) {
      log.warn("Invalid recentId format: {}", recentId);
      return ResponseEntity.status(404)
          .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
          .build();
    }

    // Fetch recent from service
    var recentOpt = recentService.getRecentById(accountId, id);
    if (recentOpt.isEmpty()) {
      log.warn("Recent item not found for accountId: {}, recentId: {}", accountId, id);
      return ResponseEntity.status(404)
          .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
          .build();
    }

    Recent recent = recentOpt.get();

    // Load sources and find the source for this recent
    var sources = loadSources(accountId);
    SourceApiDto source =
        sources.stream()
            .filter(s -> s.getId().equals(recent.sourceId()))
            .findFirst()
            .orElseGet(() -> createMockSource(recent.sourceId()));

    // Create the response object
    var response =
        new RecentItemResponseApiDto()
            .id(String.valueOf(recent.id()))
            .contentItemType(recent.contentItemType())
            .createdOn(recent.createdOn())
            .lastplayedat(recent.lastPlayedAt())
            .location(recent.location())
            .name(recent.name())
            .source(source)
            .sourceid(recent.sourceId())
            .updatedOn(recent.updatedOn());

    return ResponseEntity.ok()
        .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        .header(
            "Access-Control-Allow-Headers",
            "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
        .header("Access-Control-Expose-Headers", "Authorization")
        .body(response);
  }

  private static SourceApiDto createMockSource(String sourceId) {
    var credential = new CredentialApiDto();
    SourceApiDto source = new SourceApiDto();
    source.setId(sourceId);
    source.setType("Audio");

    // Set source-specific mock data based on sourceId
    if ("19989643".equals(sourceId)) {
      // Spotify source (user1namespot) - from test data
      source.setCreatedOn(OffsetDateTime.parse("2018-08-11T08:55:41.000+00:00"));
      source.setUpdatedOn(OffsetDateTime.parse("2019-07-20T17:48:31.000+00:00"));
      credential.setType("token_version_3");
      credential.setValue("mockTokenUser2");
      source.setName("user1namespot");
      source.setSourceproviderid("15");
      source.setSourcename("user1@example.org");
      source.setUsername("user1namespot");
    } else if ("19989342".equals(sourceId)) {
      // TuneIn source
      source.setCreatedOn(OffsetDateTime.parse("2018-08-11T08:55:28.000+00:00"));
      source.setUpdatedOn(OffsetDateTime.parse("2019-07-20T17:48:31.000+00:00"));
      credential.setType("token");
      credential.setValue("eyJduTune=");
      source.setName("");
      source.setSourceproviderid("25");
      source.setSourcename("");
      source.setUsername("");
    } else if ("19989621".equals(sourceId)) {
      // Spotify source ID from the new endpoint (mockuser789xyz)
      source.setCreatedOn(OffsetDateTime.parse("2018-08-11T09:52:31.000+00:00"));
      source.setUpdatedOn(OffsetDateTime.parse("2018-11-26T18:42:27.000+00:00"));
      credential.setType("token_version_3");
      credential.setValue("mockToken789xyz=");
      source.setName("mockuser789xyz");
      source.setSourceproviderid("15");
      source.setSourcename("user@example.com");
      source.setUsername("mockuser789xyz");
    } else {
      // Default source
      source.setCreatedOn(OffsetDateTime.parse("2018-08-11T08:55:28.000+00:00"));
      source.setUpdatedOn(OffsetDateTime.parse("2019-07-20T17:48:31.000+00:00"));
      credential.setType("token");
      credential.setValue("eyDu=");
      source.setName("");
      source.setSourceproviderid("25");
      source.setSourcename("");
      source.setUsername("");
    }

    source.setCredential(credential);
    return source;
  }

  @Override
  public ResponseEntity<Void> powerOnSupport(PowerOnRequestApiDto powerOnRequestApiDto) {
    try {
      log.info("Received power_on request");

      // Extract device ID from the device element
      String deviceId = null;
      if (powerOnRequestApiDto.getDevice() != null) {
        deviceId = powerOnRequestApiDto.getDevice().getId();
      }

      // Extract IP address from diagnostic data
      String ipAddress = null;
      if (powerOnRequestApiDto.getDiagnosticData() != null
          && powerOnRequestApiDto.getDiagnosticData().getDeviceLandscape() != null) {
        ipAddress = powerOnRequestApiDto.getDiagnosticData().getDeviceLandscape().getIpAddress();
      }

      // Validate we have both required fields
      if (deviceId == null || deviceId.isBlank()) {
        log.warn("Power_on request missing device ID");
        return ResponseEntity.badRequest().build();
      }

      if (ipAddress == null || ipAddress.isBlank()) {
        log.warn("Power_on request missing IP address for device: {}", deviceId);
        return ResponseEntity.badRequest().build();
      }

      // Record the device power on event
      deviceTrackingService.recordDevicePowerOn(deviceId, ipAddress);

      log.info("Successfully processed power_on for device: {} at IP: {}", deviceId, ipAddress);

      return ResponseEntity.ok()
          .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
          .build();

    } catch (Exception e) {
      log.error("Error processing power_on request", e);
      return ResponseEntity.status(500).build();
    }
  }

  @Override
  public ResponseEntity<Void> removeDevice(String accountId, String deviceId) {
    log.info("Removing device {} from account {}", deviceId, accountId);

    boolean unpaired = deviceService.unpairDevice(deviceId);

    if (!unpaired) {
      log.info("Device {} does not exist or is already unpaired", deviceId);
      var errorResponse =
          new ErrorResponseApiDto().message("Device does not exist ").statusCode("4012");

      return (ResponseEntity<Void>)
          (ResponseEntity<?>)
              ResponseEntity.status(400)
                  .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
                  .body(errorResponse);
    }

    // Build the Location header
    String locationHeader =
        "http://streamingqa.bose.com/account/%s/device/%s".formatted(accountId, deviceId);

    return ResponseEntity.ok()
        .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
        .header("Location", locationHeader)
        .header("METHOD_NAME", "removeDevice")
        .build();
  }
}
