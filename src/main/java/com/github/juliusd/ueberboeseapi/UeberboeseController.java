package com.github.juliusd.ueberboeseapi;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.juliusd.ueberboeseapi.generated.DefaultApi;
import com.github.juliusd.ueberboeseapi.generated.dtos.CredentialApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.FullAccountResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetsContainerApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentItemApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentItemRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentItemResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentsContainerApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceProviderApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceProvidersResponseApiDto;
import com.github.juliusd.ueberboeseapi.service.AccountDataService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.concurrent.ThreadLocalRandom;
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

  @Autowired private HttpServletRequest request;

  @Override
  public ResponseEntity<RecentItemResponseApiDto> addRecentItem(
      String accountId, String deviceId, RecentItemRequestApiDto recentItemRequestApiDto) {

    // Generate a unique ID for the recent item (simulating the real API behavior)
    String recentItemId =
        String.valueOf(ThreadLocalRandom.current().nextLong(1000000000L, 9999999999L));

    SourceApiDto source = buildSourceApiDto(recentItemRequestApiDto);

    // Create the response object
    RecentItemResponseApiDto response = new RecentItemResponseApiDto();
    response.setId(recentItemId);
    response.setContentItemType(recentItemRequestApiDto.getContentItemType());
    response.setCreatedOn(OffsetDateTime.parse("2018-11-27T18:20:01.000+00:00"));
    response.setLastplayedat(recentItemRequestApiDto.getLastplayedat());
    response.setLocation(recentItemRequestApiDto.getLocation());
    response.setName(recentItemRequestApiDto.getName());
    response.setSource(source);
    response.setSourceid(recentItemRequestApiDto.getSourceid());
    response.setUpdatedOn(OffsetDateTime.now());

    // Build the Location header
    String locationHeader =
        "http://streamingqa.bose.com/account/%s/device/%s/recent/%s"
            .formatted(accountId, deviceId, recentItemId);

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
    credential.setType("token");
    credential.setValue("eyDu=");

    SourceApiDto source = new SourceApiDto();
    source.setId(recentItemRequestApiDto.getSourceid());
    source.setType("Audio");
    source.setCreatedOn(OffsetDateTime.parse("2018-08-11T08:55:41.000+00:00"));
    source.setCredential(credential);
    source.setName("");
    source.setSourceproviderid("25");
    source.setSourcename("");
    source.setUpdatedOn(OffsetDateTime.parse("2019-07-20T17:48:31.000+00:00"));
    source.setUsername("");
    return source;
  }

  @Override
  public ResponseEntity<SourceProvidersResponseApiDto> getSourceProviders() {

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
    log.info("Getting full account data for accountId: {}", accountId);

    // Check if cached data exists
    if (accountDataService.hasAccountData(accountId)) {
      try {
        FullAccountResponseApiDto response = accountDataService.loadFullAccountData(accountId);
        log.info("Successfully loaded account data from cache for accountId: {}", accountId);

        return ResponseEntity.ok()
            .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
            .header("METHOD_NAME", "getFullAccount")
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            .header(
                "Access-Control-Allow-Headers",
                "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
            .header("Access-Control-Expose-Headers", "Authorization")
            .body(response);
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
    log.info("Cache miss for accountId: {}, forwarding request to proxy", accountId);
    ResponseEntity<byte[]> proxyResponse = proxyService.forwardRequest(request, null);

    // Check if proxy response is successful
    if (!proxyResponse.getStatusCode().is2xxSuccessful() || proxyResponse.getBody() == null) {
      log.warn(
          "Proxy request failed for accountId: {}, status: {}",
          accountId,
          proxyResponse.getStatusCode());
      return ResponseEntity.status(502)
          .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
          .build();
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

      return ResponseEntity.status(proxyResponse.getStatusCode())
          .headers(proxyResponse.getHeaders())
          .body(parsedResponse);
    } catch (Exception parseException) {
      log.error(
          "Failed to parse proxy response for accountId: {}. Error: {}",
          accountId,
          parseException.getMessage());
      return ResponseEntity.status(502)
          .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
          .build();
    }
  }

  @Override
  public ResponseEntity<RecentsContainerApiDto> getRecents(String accountId, String deviceId) {

    RecentsContainerApiDto response = new RecentsContainerApiDto();

    // Create mock recent items
    response.addRecentItem(
        createMockRecentItem(
            "tracklisturl",
            "Ghostsitter 23 - Das Haus im Moor",
            "/playback/container/c3BvdGlmeTphbGJ1bTowZ3BGWVZNbVV6VkVxeVAyeUh3cEha",
            "19989621",
            "15",
            "token_version_3",
            "mockToken123=",
            "mockuser123",
            "mock@example.com",
            OffsetDateTime.parse("2025-12-13T17:14:28.000+00:00")));

    response.addRecentItem(
        createMockRecentItem(
            "stationurl",
            "Radio TEDDY",
            "/v1/playback/station/s80044",
            "19989313",
            "25",
            "token",
            "eyJmock=",
            "",
            "",
            OffsetDateTime.parse("2018-11-27T18:20:01.000+00:00")));

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

              // Return the presets for this device
              PresetsContainerApiDto presets =
                  device.getPresets() != null ? device.getPresets() : new PresetsContainerApiDto();

              return ResponseEntity.ok()
                  .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
                  .header("Access-Control-Allow-Origin", "*")
                  .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                  .header(
                      "Access-Control-Allow-Headers",
                      "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
                  .header("Access-Control-Expose-Headers", "Authorization")
                  .body(presets);
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

  private static RecentItemApiDto createMockRecentItem(
      String contentItemType,
      String name,
      String location,
      String sourceId,
      String sourceProviderId,
      String credentialType,
      String credentialValue,
      String username,
      String sourcename,
      OffsetDateTime createdOn) {

    // Generate unique ID
    String recentItemId =
        String.valueOf(ThreadLocalRandom.current().nextLong(1000000000L, 9999999999L));

    // Create credential
    CredentialApiDto credential = new CredentialApiDto();
    credential.setType(credentialType);
    credential.setValue(credentialValue);

    // Create source
    SourceApiDto source = new SourceApiDto();
    source.setId(sourceId);
    source.setType("Audio");
    source.setCreatedOn(OffsetDateTime.parse("2018-08-11T08:55:41.000+00:00"));
    source.setCredential(credential);
    source.setName(username);
    source.setSourceproviderid(sourceProviderId);
    source.setSourcename(sourcename);
    source.setUpdatedOn(OffsetDateTime.parse("2019-07-20T17:48:31.000+00:00"));
    source.setUsername(username);

    // Create recent item
    RecentItemApiDto recentItem = new RecentItemApiDto();
    recentItem.setId(recentItemId);
    recentItem.setContentItemType(contentItemType);
    recentItem.setCreatedOn(createdOn);
    recentItem.setLastplayedat(OffsetDateTime.now());
    recentItem.setLocation(location);
    recentItem.setName(name);
    recentItem.setSource(source);
    recentItem.setSourceid(sourceId);
    recentItem.setUpdatedOn(OffsetDateTime.now());

    return recentItem;
  }

  private static SourceProviderApiDto createSourceProvider(SourceProvider sourceProvider) {
    SourceProviderApiDto provider = new SourceProviderApiDto();
    provider.setId(sourceProvider.getId());
    provider.setCreatedOn(sourceProvider.getCreatedOn());
    provider.setName(sourceProvider.getName());
    provider.setUpdatedOn(sourceProvider.getUpdatedOn());
    return provider;
  }
}
