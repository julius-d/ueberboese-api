package com.github.juliusd.ueberboeseapi.bmx;

import static tools.jackson.databind.json.JsonMapper.builder;

import com.github.juliusd.ueberboeseapi.generated.dtos.BmxAudioApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxLinkApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxLinkWithClientApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxLinksApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxPlaybackResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxReportResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxServiceApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxServiceAssetsApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxServiceAvailabilityApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxServiceIconsApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxServiceIdApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxServicesAvailabilityResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxServicesResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxStreamApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxTokenResponseApiDto;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/** Service for handling BMX operations including TuneIn integration and custom streams. */
@Service
@Slf4j
@RequiredArgsConstructor
public class BmxService {

  private final TuneInClient tuneInClient;
  private final UeberboeseApiUrlProperties urlProperties;

  private final JsonMapper jsonMapper = builder().findAndAddModules().build();

  /**
   * Creates and returns the BMX services registry.
   *
   * @return BMX services response with all service definitions
   */
  public BmxServicesResponseApiDto getBmxServices() {
    log.info("Building BMX services registry");

    // Create top-level links
    BmxLinksApiDto topLinks = new BmxLinksApiDto();
    BmxLinkApiDto availabilityLink = new BmxLinkApiDto("../servicesAvailability");
    topLinks.setBmxServicesAvailability(availabilityLink);

    // Create services list
    List<BmxServiceApiDto> services =
        List.of(
            createTuneInService(),
            createLocalInternetRadioService(),
            createSiriusXmService(),
            createRadioplayerService());

    // Build response
    BmxServicesResponseApiDto response = new BmxServicesResponseApiDto();
    response.setLinks(topLinks);
    response.setAskAgainAfter(1234567);
    response.setBmxServices(services);

    return response;
  }

  /**
   * Returns the availability status of BMX services.
   *
   * @return BMX services availability response
   */
  public BmxServicesAvailabilityResponseApiDto getBmxServicesAvailability() {
    log.info("Building BMX services availability");

    var services =
        List.of(
            createServiceAvailability("TUNEIN", true, false),
            createServiceAvailability("SIRIUSXM_EVEREST", false, true));

    BmxServicesAvailabilityResponseApiDto response = new BmxServicesAvailabilityResponseApiDto();
    response.setServices(services);

    return response;
  }

  /**
   * Creates a service availability entry.
   *
   * @param serviceName Name of the service
   * @param canAdd Whether the service can be added
   * @param canRemove Whether the service can be removed
   * @return Service availability DTO
   */
  private BmxServiceAvailabilityApiDto createServiceAvailability(
      String serviceName, boolean canAdd, boolean canRemove) {
    BmxServiceAvailabilityApiDto availability = new BmxServiceAvailabilityApiDto();
    availability.setService(serviceName);
    availability.setCanAdd(canAdd);
    availability.setCanRemove(canRemove);
    return availability;
  }

  /** Creates the TuneIn service definition. */
  private BmxServiceApiDto createTuneInService() {
    BmxServiceIdApiDto id = new BmxServiceIdApiDto("TUNEIN", 25);
    BmxServiceAssetsApiDto assets = extractTuneInAssets();
    Map<String, Object> authModel = createAuthModel();

    BmxLinksApiDto serviceLinks = new BmxLinksApiDto();
    serviceLinks.setBmxNavigate(new BmxLinkApiDto("/v1/navigate"));
    serviceLinks.setBmxToken(new BmxLinkApiDto("/v1/token"));
    serviceLinks.setSelf(new BmxLinkApiDto("/"));

    BmxServiceApiDto service =
        new BmxServiceApiDto(
            assets,
            authModel,
            urlProperties.baseUrl() + "/bmx/tunein",
            id,
            List.of(
                BmxServiceApiDto.StreamTypesEnum.LIVE_RADIO,
                BmxServiceApiDto.StreamTypesEnum.ON_DEMAND));
    service.setLinks(serviceLinks);
    service.setAskAdapter(false);

    return service;
  }

  private static @NonNull Map<String, Object> createAuthModel() {
    Map<String, Object> authModel = new LinkedHashMap<>();
    Map<String, Object> anonymousAccount = new LinkedHashMap<>();
    anonymousAccount.put("autoCreate", true);
    anonymousAccount.put("enabled", true);
    authModel.put("anonymousAccount", anonymousAccount);
    return authModel;
  }

  private @NonNull BmxServiceAssetsApiDto extractTuneInAssets() {
    var icons = createRadioIconsApiDto();

    return new BmxServiceAssetsApiDto(
        "#000000",
        "With TuneIn on SoundTouch, listen to more than 100,000 stations and the hottest podcasts, plus live games, concerts and shows from around the world. However, you cannot access your Favorites and Premium content on your existing TuneIn account at this time.",
        icons,
        "TuneIn");
  }

  private @NonNull BmxServiceIconsApiDto createRadioIconsApiDto() {
    var icons =
        new BmxServiceIconsApiDto(
            getMonochromeSvgUrl(),
            getMonochromePngUrl(),
            getMonochromeSvgUrl(),
            getMonochromeSvgUrl());
    icons.setDefaultAlbumArt(getMonochromePngUrl());
    return icons;
  }

  /** Creates the Local Internet Radio (Custom Stations) service definition. */
  private BmxServiceApiDto createLocalInternetRadioService() {
    BmxServiceIdApiDto id = new BmxServiceIdApiDto("LOCAL_INTERNET_RADIO", 11);

    var icons = createRadioIconsApiDto();

    BmxServiceAssetsApiDto assets =
        new BmxServiceAssetsApiDto(
            "#000000", "Custom radio stations with BMX.", icons, "Custom Stations");

    Map<String, Object> authModel = createAuthModel();

    BmxLinksApiDto serviceLinks = new BmxLinksApiDto();
    serviceLinks.setBmxToken(new BmxLinkApiDto("/token"));
    serviceLinks.setSelf(new BmxLinkApiDto("/"));

    BmxServiceApiDto service =
        new BmxServiceApiDto(
            assets,
            authModel,
            urlProperties.baseUrl() + "/core02/svc-bmx-adapter-orion/prod/orion",
            id,
            List.of(BmxServiceApiDto.StreamTypesEnum.LIVE_RADIO));
    service.setLinks(serviceLinks);
    service.setAskAdapter(false);

    return service;
  }

  /** Creates the SiriusXM service definition. */
  private BmxServiceApiDto createSiriusXmService() {
    BmxServiceIdApiDto id = new BmxServiceIdApiDto("SIRIUSXM_EVEREST", 38);

    BmxServiceAssetsApiDto assets = createSiriusAssets();

    Map<String, Object> authModel = new HashMap<>();
    authModel.put("loginPageProvider", "BOSE");

    BmxLinksApiDto serviceLinks = new BmxLinksApiDto();
    serviceLinks.setBmxAvailability(new BmxLinkApiDto("/availability"));
    serviceLinks.setBmxLogout(new BmxLinkApiDto("/logout"));
    serviceLinks.setBmxNavigate(new BmxLinkApiDto("/navigate/"));
    serviceLinks.setBmxToken(new BmxLinkApiDto("/token"));
    serviceLinks.setSelf(new BmxLinkApiDto("/"));

    BmxServiceApiDto service =
        new BmxServiceApiDto(
            assets,
            authModel,
            urlProperties.baseUrl()
                + "/core02/svc-bmx-adapter-siriusxm-everest-eco1/prod/live-adapter",
            id,
            List.of(
                BmxServiceApiDto.StreamTypesEnum.LIVE_RADIO,
                BmxServiceApiDto.StreamTypesEnum.ON_DEMAND));
    service.setLinks(serviceLinks);
    service.setSignupUrl(
        "https://streaming.siriusxm.com/?/flepz=true&campaign=bose30#_frmAccountLookup");
    service.setAskAdapter(false);

    return service;
  }

  private @NonNull BmxServiceAssetsApiDto createSiriusAssets() {
    var icons = createRadioIconsApiDto();

    BmxServiceAssetsApiDto assets =
        new BmxServiceAssetsApiDto(
            "#004b85",
            "Over 200 channels including commercial-free music, plus play-by-play and sports talk, world class news, comedy, exclusive entertainment and more.",
            icons,
            "SiriusXM");
    assets.setShortDescription(
        "Over 200 channels including commercial-free music, plus play-by-play and sports talk, world class news, comedy, exclusive entertainment and more.");

    return assets;
  }

  /** Creates the Radioplayer service definition. */
  private BmxServiceApiDto createRadioplayerService() {
    BmxServiceIdApiDto id = new BmxServiceIdApiDto("RADIOPLAYER", 35);

    BmxServiceAssetsApiDto assets = createRadioplayerAssets();

    Map<String, Object> authModel = new HashMap<>();
    Map<String, Object> anonymousAccount = new HashMap<>();
    anonymousAccount.put("autoCreate", false);
    anonymousAccount.put("enabled", true);
    authModel.put("anonymousAccount", anonymousAccount);

    BmxLinksApiDto serviceLinks = new BmxLinksApiDto();
    serviceLinks.setBmxAvailability(new BmxLinkApiDto("/availability"));
    serviceLinks.setBmxNavigate(new BmxLinkApiDto("/navigate"));
    // Special token URL pattern for Radioplayer
    serviceLinks.setBmxToken(
        new BmxLinkApiDto(
            urlProperties.baseUrl() + "/soundtouch-msp-token-proxy/RADIOPLAYER/token"));
    serviceLinks.setSelf(new BmxLinkApiDto("/"));

    BmxServiceApiDto service =
        new BmxServiceApiDto(
            assets,
            authModel,
            "https://boserp.radioapi.io",
            id,
            List.of(
                BmxServiceApiDto.StreamTypesEnum.LIVE_RADIO,
                BmxServiceApiDto.StreamTypesEnum.ON_DEMAND));
    service.setLinks(serviceLinks);
    service.setAskAdapter(false);

    return service;
  }

  private @NonNull BmxServiceAssetsApiDto createRadioplayerAssets() {
    var icons = createRadioIconsApiDto();

    return new BmxServiceAssetsApiDto(
        "#cc0033",
        "Radio for you, from your country. Radioplayer is a unique broadcaster owned service, with higher quality streams, full content (including all live sport), and thousands of catch-up programs and podcasts. Radioplayer is available in UK, Germany, Canada, Austria, Belgium, Denmark, Ireland, Italy, Norway, Spain and Switzerland.",
        icons,
        "Radioplayer");
  }

  private @NonNull String getMonochromePngUrl() {
    return urlProperties.baseUrl() + "/icons/radio-logo-monochrome-small.png";
  }

  private @NonNull String getMonochromeSvgUrl() {
    return urlProperties.baseUrl() + "/icons/radio-logo-monochrome.svg";
  }

  /**
   * Fetches TuneIn station playback information.
   *
   * @param stationId TuneIn station ID (e.g., "s80044")
   * @return Playback response with stream URLs and metadata
   */
  public BmxPlaybackResponseApiDto getTuneInPlayback(String stationId) {
    log.info("Getting TuneIn playback for stationId: {}", stationId);

    // Fetch metadata and stream URLs from TuneIn
    TuneInClient.StationMetadata metadata = tuneInClient.getStationMetadata(stationId);
    List<String> streamUrls = tuneInClient.getStreamUrls(stationId);

    if (streamUrls.isEmpty()) {
      throw new RuntimeException("No stream URLs available for station: " + stationId);
    }

    // Build response
    BmxPlaybackResponseApiDto response = new BmxPlaybackResponseApiDto();

    // Set links
    BmxLinksApiDto links = new BmxLinksApiDto();
    BmxLinkApiDto favoriteLink = new BmxLinkApiDto();
    favoriteLink.setHref("/v1/favorite/" + stationId);
    links.setBmxFavorite(favoriteLink);

    BmxLinkWithClientApiDto nowPlayingLink = new BmxLinkWithClientApiDto();
    nowPlayingLink.setHref("/v1/now-playing/station/" + stationId);
    nowPlayingLink.setUseInternalClient(BmxLinkWithClientApiDto.UseInternalClientEnum.ALWAYS);
    links.setBmxNowplaying(nowPlayingLink);

    // Generate IDs for reporting (these are placeholder values)
    String streamId = "e3342";
    String listenId = String.valueOf(System.currentTimeMillis());
    String reportingHref =
        String.format(
            "/v1/report?stream_id=%s&guide_id=%s&listen_id=%s&stream_type=liveRadio",
            streamId, stationId, listenId);
    BmxLinkApiDto reportingLink = new BmxLinkApiDto();
    reportingLink.setHref(reportingHref);
    links.setBmxReporting(reportingLink);

    response.setLinks(links);

    // Set audio with streams
    BmxAudioApiDto audio = new BmxAudioApiDto();
    audio.setHasPlaylist(true);
    audio.setIsRealtime(true);
    audio.setMaxTimeout(60);
    audio.setStreamUrl(streamUrls.getFirst()); // Primary stream URL

    // Create stream objects for each URL
    List<BmxStreamApiDto> streams =
        streamUrls.stream()
            .map(
                url -> {
                  BmxStreamApiDto stream = new BmxStreamApiDto();
                  BmxLinksApiDto streamLinks = new BmxLinksApiDto();
                  streamLinks.setBmxReporting(reportingLink);
                  stream.setLinks(streamLinks);
                  stream.setBufferingTimeout(20);
                  stream.setConnectingTimeout(10);
                  stream.setHasPlaylist(true);
                  stream.setIsRealtime(true);
                  stream.setStreamUrl(url);
                  return stream;
                })
            .toList();

    audio.setStreams(streams);
    response.setAudio(audio);

    // Set metadata
    response.setImageUrl(metadata.getLogo());
    response.setIsFavorite(false);
    response.setName(metadata.getName());
    response.setStreamType(BmxPlaybackResponseApiDto.StreamTypeEnum.LIVE_RADIO);

    log.info("Built TuneIn playback response for station: {}", metadata.getName());
    return response;
  }

  /**
   * Decodes and processes custom stream data.
   *
   * @param base64Data Base64-encoded JSON containing streamUrl, imageUrl, and name
   * @return Playback response for custom stream
   */
  public BmxPlaybackResponseApiDto getCustomStreamPlayback(String base64Data) {
    log.info("Processing custom stream data");

    try {
      // Decode base64
      byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
      String jsonString = new String(decodedBytes, StandardCharsets.UTF_8);

      // Parse JSON
      @SuppressWarnings("unchecked")
      Map<String, String> streamData = jsonMapper.readValue(jsonString, Map.class);

      String streamUrl = streamData.get("streamUrl");
      String imageUrl = streamData.get("imageUrl");
      String name = streamData.get("name");

      if (streamUrl == null || streamUrl.isEmpty()) {
        throw new IllegalArgumentException("streamUrl is required in custom stream data");
      }

      // Build response
      BmxPlaybackResponseApiDto response = new BmxPlaybackResponseApiDto();

      // Set audio
      BmxAudioApiDto audio = new BmxAudioApiDto();
      audio.setHasPlaylist(true);
      audio.setIsRealtime(true);
      audio.setStreamUrl(streamUrl);

      // Create single stream
      BmxStreamApiDto stream = new BmxStreamApiDto();
      stream.setHasPlaylist(true);
      stream.setIsRealtime(true);
      stream.setStreamUrl(streamUrl);
      audio.setStreams(List.of(stream));

      response.setAudio(audio);

      // Set metadata
      response.setImageUrl(imageUrl != null ? imageUrl : "");
      response.setName(name != null ? name : "Custom Stream");
      response.setStreamType(BmxPlaybackResponseApiDto.StreamTypeEnum.LIVE_RADIO);

      log.info("Built custom stream playback response for: {}", name);
      return response;

    } catch (Exception e) {
      log.error("Failed to process custom stream data", e);
      throw new RuntimeException("Failed to process custom stream data", e);
    }
  }

  /**
   * Refreshes TuneIn authentication token. This is a stub implementation that returns the same
   * token.
   *
   * @param refreshToken Refresh token
   * @return Token response with access token
   */
  public BmxTokenResponseApiDto refreshTuneInToken(String refreshToken) {
    log.info("Refreshing TuneIn token (stub implementation)");

    // Stub implementation - just return the refresh token as access token
    // In a real implementation, you would call TuneIn's token endpoint
    BmxTokenResponseApiDto response = new BmxTokenResponseApiDto();
    response.setAccessToken(refreshToken);

    return response;
  }

  /**
   * Handles analytics reporting. This is a stub implementation that logs and returns OK.
   *
   * @return Report response
   */
  public BmxReportResponseApiDto reportAnalytics() {
    log.info("Received analytics report (stub implementation)");

    BmxReportResponseApiDto response = new BmxReportResponseApiDto();
    response.setNextReportIn(1800); // Report again in 30 minutes

    return response;
  }
}
