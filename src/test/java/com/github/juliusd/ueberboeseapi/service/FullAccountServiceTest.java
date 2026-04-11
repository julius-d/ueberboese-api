package com.github.juliusd.ueberboeseapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.github.juliusd.ueberboeseapi.ProxyService;
import com.github.juliusd.ueberboeseapi.XmlMessageConverterConfig;
import com.github.juliusd.ueberboeseapi.device.Device;
import com.github.juliusd.ueberboeseapi.device.DeviceRepository;
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
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class FullAccountServiceTest {

  private static final String SPOTIFY_USER_ID = "spotify-user-123";
  private static final String SPOTIFY_SOURCE_ID = "123";

  @Mock private AccountDataService accountDataService;
  @Mock private ProxyService proxyService;
  @Mock private SpotifyAccountService spotifyAccountService;
  @Mock private RecentService recentService;
  @Mock private PresetService presetService;
  @Mock private DeviceRepository deviceRepository;
  @Mock private HttpServletRequest request;

  private FullAccountService fullAccountService;

  @BeforeEach
  void setUp() {
    var xmlMapper = new XmlMessageConverterConfig().customXmlMapper();
    var recentMapper = new RecentMapper();
    var presetMapper = new PresetMapper();

    fullAccountService =
        new FullAccountService(
            accountDataService,
            proxyService,
            xmlMapper,
            spotifyAccountService,
            recentService,
            recentMapper,
            presetService,
            presetMapper,
            deviceRepository);
  }

  @Test
  void testGetFullAccount_CacheHit_ReturnsData() throws IOException {
    // Given
    String accountId = "test-account-123";
    FullAccountResponseApiDto expectedData = new FullAccountResponseApiDto();
    expectedData.setId(accountId);

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(expectedData);
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(accountId);

    // Verify proxy was NOT called
    verify(proxyService, never()).forwardRequest(any(), any());
  }

  @Test
  void testGetFullAccount_CacheHit_LoadFails_ReturnsEmpty() throws IOException {
    // Given
    String accountId = "test-account-456";

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId))
        .thenThrow(new IOException("Cache read error"));

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then
    assertThat(result).isEmpty();

    // Verify proxy was NOT called even though cache load failed
    verify(proxyService, never()).forwardRequest(any(), any());
  }

  @Test
  void testGetFullAccount_CacheMiss_ProxySuccess_ReturnsData() throws Exception {
    // Given
    String accountId = "test-account-789";
    String xmlContent =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <account id="test-account-789">
          <accountStatus>ACTIVE</accountStatus>
          <mode>global</mode>
        </account>
        """;

    when(accountDataService.hasAccountData(accountId)).thenReturn(false);
    when(proxyService.forwardRequest(eq(request), any()))
        .thenReturn(ResponseEntity.ok(xmlContent.getBytes()));
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(accountId);

    // Verify data was cached
    verify(accountDataService).saveFullAccountDataRaw(eq(accountId), anyString());
  }

  @Test
  void testGetFullAccount_CacheMiss_ProxyFailure_ReturnsMinimalAccount() throws IOException {
    // Given
    String accountId = "test-account-error";

    when(accountDataService.hasAccountData(accountId)).thenReturn(false);
    when(proxyService.forwardRequest(eq(request), any()))
        .thenReturn(ResponseEntity.status(HttpStatus.BAD_GATEWAY).build());
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - minimal account is returned instead of empty
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(accountId);
    assertThat(result.get().getAccountStatus()).isEqualTo("ACTIVE");
    assertThat(result.get().getMode()).isEqualTo("global");
    assertThat(result.get().getPreferredLanguage()).isEqualTo("en");
    assertThat(result.get().getDevices()).isNotNull();
    assertThat(result.get().getSources()).isNotNull();

    // Verify no attempt to cache was made
    verify(accountDataService, never()).saveFullAccountDataRaw(anyString(), anyString());
    // Verify inject pipeline still ran
    verify(deviceRepository).findAllByMargeAccountId(accountId);
  }

  @Test
  void testGetFullAccount_CacheMiss_ProxyReturnsNullBody_ReturnsMinimalAccount()
      throws IOException {
    // Given
    String accountId = "test-account-null";

    when(accountDataService.hasAccountData(accountId)).thenReturn(false);
    when(proxyService.forwardRequest(eq(request), any()))
        .thenReturn(ResponseEntity.ok().build()); // No body
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - minimal account is returned instead of empty
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(accountId);

    // Verify no attempt to cache was made
    verify(accountDataService, never()).saveFullAccountDataRaw(anyString(), anyString());
  }

  @Test
  void testGetFullAccount_CacheMiss_ProxyReturns401_WithDbDevice_ReturnsDeviceInMinimalAccount() {
    // Given
    String accountId = "test-account-401";
    Device dbDevice =
        Device.builder()
            .deviceId("DEVICE_FROM_DB")
            .name("My Speaker")
            .ipAddress("192.168.1.5")
            .margeAccountId(accountId)
            .firmwareVersion("27.0.6.46330.5043500 epdbuild.trunk.hepdswbld04.2022-08-04T11:20:29")
            .deviceSerialNumber("PTEST0000000000000000001")
            .productCode("SoundTouch 10 sm2")
            .productType("5")
            .productSerialNumber("TEST000000000000002")
            .firstSeen(OffsetDateTime.parse("2025-01-01T10:00:00.000+00:00"))
            .lastSeen(OffsetDateTime.parse("2026-03-01T08:00:00.000+00:00"))
            .updatedOn(OffsetDateTime.parse("2026-03-01T08:00:00.000+00:00"))
            .build();

    when(accountDataService.hasAccountData(accountId)).thenReturn(false);
    when(proxyService.forwardRequest(eq(request), any()))
        .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of(dbDevice));
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - DB device is present in the minimal account with all stored fields
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(accountId);
    var devices = result.get().getDevices().getDevice();
    assertThat(devices).extracting(DeviceApiDto::getDeviceid).containsExactly("DEVICE_FROM_DB");
    var injectedDevice = devices.get(0);
    assertThat(injectedDevice.getFirmwareVersion())
        .isEqualTo("27.0.6.46330.5043500 epdbuild.trunk.hepdswbld04.2022-08-04T11:20:29");
    assertThat(injectedDevice.getSerialNumber()).isEqualTo("PTEST0000000000000000001");
    assertThat(injectedDevice.getAttachedProduct()).isNotNull();
    assertThat(injectedDevice.getAttachedProduct().getProductCode()).isEqualTo("SoundTouch 10 sm2");
    assertThat(injectedDevice.getAttachedProduct().getSerialnumber())
        .isEqualTo("TEST000000000000002");
  }

  @Test
  void
      testGetFullAccount_CacheMiss_ProxyReturns401_WithDbRecentsAndPresets_InjectsIntoMinimalAccount() {
    // Given
    String accountId = "test-account-401-recents-presets";
    String deviceId = "DEVICE_FROM_DB_2";
    Device dbDevice =
        Device.builder()
            .deviceId(deviceId)
            .name("My Speaker")
            .ipAddress("192.168.1.5")
            .margeAccountId(accountId)
            .firstSeen(OffsetDateTime.parse("2025-01-01T10:00:00.000+00:00"))
            .lastSeen(OffsetDateTime.parse("2026-03-01T08:00:00.000+00:00"))
            .updatedOn(OffsetDateTime.parse("2026-03-01T08:00:00.000+00:00"))
            .build();
    Recent dbRecent =
        Recent.builder()
            .id(1L)
            .accountId(accountId)
            .name("My Radio")
            .location("/v1/playback/station/s12345")
            .sourceId(SPOTIFY_SOURCE_ID)
            .contentItemType("stationurl")
            .lastPlayedAt(OffsetDateTime.parse("2026-02-01T12:00:00.000+00:00"))
            .createdOn(OffsetDateTime.parse("2026-01-01T10:00:00.000+00:00"))
            .updatedOn(OffsetDateTime.parse("2026-02-01T12:00:00.000+00:00"))
            .build();
    Preset dbPreset =
        Preset.builder()
            .id(1L)
            .accountId(accountId)
            .deviceId(deviceId)
            .buttonNumber(1)
            .name("My Playlist")
            .location("/playback/container/abc123")
            .sourceId(SPOTIFY_SOURCE_ID)
            .containerArt("https://example.org/art.png")
            .contentItemType("tracklisturl")
            .createdOn(OffsetDateTime.parse("2026-01-01T10:00:00.000+00:00"))
            .updatedOn(OffsetDateTime.parse("2026-02-01T12:00:00.000+00:00"))
            .build();

    when(accountDataService.hasAccountData(accountId)).thenReturn(false);
    when(proxyService.forwardRequest(eq(request), any()))
        .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of(dbDevice));
    when(recentService.getRecents(accountId)).thenReturn(List.of(dbRecent));
    when(presetService.getPresets(accountId, deviceId)).thenReturn(List.of(dbPreset));
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - DB recents and presets are injected into the device in the minimal account
    assertThat(result).isPresent();
    var device = result.get().getDevices().getDevice().getFirst();
    var recents = device.getRecents().getRecent();
    assertThat(recents).hasSize(1);
    assertThat(recents.getFirst().getName()).isEqualTo("My Radio");
    var presets = device.getPresets().getPreset();
    assertThat(presets).hasSize(1);
    assertThat(presets.getFirst().getName()).isEqualTo("My Playlist");
    assertThat(presets.getFirst().getButtonNumber()).isEqualTo(1);
  }

  @Test
  void testGetFullAccount_CacheMiss_ParseError_ReturnsEmpty() throws IOException {
    // Given
    String accountId = "test-account-bad-xml";
    String invalidXml = "<invalid>not complete";

    when(accountDataService.hasAccountData(accountId)).thenReturn(false);
    when(proxyService.forwardRequest(eq(request), any()))
        .thenReturn(ResponseEntity.ok(invalidXml.getBytes()));

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then
    assertThat(result).isEmpty();

    // Verify no attempt to cache was made since parsing failed
    verify(accountDataService, never()).saveFullAccountDataRaw(anyString(), anyString());
  }

  @Test
  void testGetFullAccount_CacheSaveFails_StillReturnsData() throws Exception {
    // Given
    String accountId = "test-account-cache-fail";
    String xmlContent =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <account id="test-account-cache-fail">
          <accountStatus>ACTIVE</accountStatus>
          <mode>global</mode>
        </account>
        """;

    when(accountDataService.hasAccountData(accountId)).thenReturn(false);
    when(proxyService.forwardRequest(eq(request), any()))
        .thenReturn(ResponseEntity.ok(xmlContent.getBytes()));

    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());

    // Mock cache save to throw exception
    doThrow(new IOException("Disk full"))
        .when(accountDataService)
        .saveFullAccountDataRaw(eq(accountId), anyString());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - Should still return data despite caching failure
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(accountId);

    // Verify cache save was attempted
    verify(accountDataService).saveFullAccountDataRaw(eq(accountId), anyString());
  }

  @Test
  void testPatch_SpotifyCredentialsReplaced_WhenMatchingAccountExists() throws IOException {
    // Given
    String accountId = "test-account-spotify";
    String spotifyUserId = "spotify-user-123";
    String originalToken = "old-token";
    String newRefreshToken = "new-refresh-token-abc";
    OffsetDateTime updatedTimestamp = OffsetDateTime.now().minusDays(1);

    // Create a FullAccountResponse with a Spotify source
    FullAccountResponseApiDto response =
        createFullAccountWithSpotifySources(spotifyUserId, originalToken);

    // Mock SpotifyAccountService to return matching account
    SpotifyAccount spotifyAccount =
        new SpotifyAccount(
            spotifyUserId,
            "Test User",
            newRefreshToken,
            OffsetDateTime.now().minusDays(7),
            updatedTimestamp,
            null);
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of(spotifyAccount));

    // Mock account data service
    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then
    assertThat(result).isPresent();
    SourceApiDto spotifySource = result.get().getSources().getSource().getFirst();
    assertThat(spotifySource.getCredential().getValue()).isEqualTo(newRefreshToken);
    assertThat(spotifySource.getUpdatedOn()).isEqualTo(updatedTimestamp);
  }

  @Test
  void testPatch_NonSpotifySourcesUnmodified() throws IOException {
    // Given
    String accountId = "test-account-mixed";
    String originalToken = "original-token";

    // Create response with non-Spotify source (sourceproviderid = "25")
    FullAccountResponseApiDto response = new FullAccountResponseApiDto();
    response.setId(accountId);

    SourcesContainerApiDto sources = new SourcesContainerApiDto();
    List<SourceApiDto> sourceList = new ArrayList<>();

    SourceApiDto nonSpotifySource = createSource("25", "user123", originalToken);
    sourceList.add(nonSpotifySource);

    sources.setSource(sourceList);
    response.setSources(sources);

    // Mock SpotifyAccountService to return accounts
    SpotifyAccount spotifyAccount =
        new SpotifyAccount(
            "user123", "Test User", "new-token", OffsetDateTime.now(), OffsetDateTime.now(), null);
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of(spotifyAccount));

    // Mock account data service
    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - Non-Spotify source should NOT be modified
    assertThat(result).isPresent();
    SourceApiDto resultSource = result.get().getSources().getSource().getFirst();
    assertThat(resultSource.getCredential().getValue()).isEqualTo(originalToken);
  }

  @Test
  void testPatch_SpotifySourceWithNoMatchingAccount_Unchanged() throws IOException {
    // Given
    String accountId = "test-account-no-match";
    String spotifyUserId = "spotify-user-456";
    String originalToken = "original-token";

    FullAccountResponseApiDto response =
        createFullAccountWithSpotifySources(spotifyUserId, originalToken);

    // Mock SpotifyAccountService to return different account
    SpotifyAccount differentAccount =
        new SpotifyAccount(
            "different-user",
            "Different User",
            "different-token",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            null);
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of(differentAccount));

    // Mock account data service
    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - Credential should remain unchanged
    assertThat(result).isPresent();
    SourceApiDto spotifySource = result.get().getSources().getSource().getFirst();
    assertThat(spotifySource.getCredential().getValue()).isEqualTo(originalToken);
  }

  @Test
  void testPatch_HandlesNullSources() throws IOException {
    // Given
    String accountId = "test-account-null-sources";
    FullAccountResponseApiDto response = new FullAccountResponseApiDto();
    response.setId(accountId);
    response.setSources(null);

    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of());
    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - Should not throw exception
    assertThat(result).isPresent();
    assertThat(result.get().getSources()).isNull();
  }

  @Test
  void testPatch_HandlesEmptySourceList() throws IOException {
    // Given
    String accountId = "test-account-empty-sources";
    FullAccountResponseApiDto response = new FullAccountResponseApiDto();
    response.setId(accountId);

    SourcesContainerApiDto sources = new SourcesContainerApiDto();
    sources.setSource(new ArrayList<>());
    response.setSources(sources);

    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of());
    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - Should not throw exception
    assertThat(result).isPresent();
    assertThat(result.get().getSources().getSource()).isEmpty();
  }

  @Test
  void testPatch_HandlesNullCredential() throws IOException {
    // Given
    String accountId = "test-account-null-credential";
    String spotifyUserId = "spotify-user-789";

    FullAccountResponseApiDto response = new FullAccountResponseApiDto();
    response.setId(accountId);

    SourcesContainerApiDto sources = new SourcesContainerApiDto();
    List<SourceApiDto> sourceList = new ArrayList<>();

    SourceApiDto spotifySource = createSource("15", spotifyUserId, "token");
    spotifySource.setCredential(null); // Null credential
    sourceList.add(spotifySource);

    sources.setSource(sourceList);
    response.setSources(sources);

    // Mock SpotifyAccountService to return matching account
    SpotifyAccount spotifyAccount =
        new SpotifyAccount(
            spotifyUserId,
            "Test User",
            "new-token",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            null);
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of(spotifyAccount));

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - Should not throw exception, credential remains null
    assertThat(result).isPresent();
    assertThat(result.get().getSources().getSource().getFirst().getCredential()).isNull();
  }

  @Test
  void testPatch_MultipleSpotifySources_OnlyMatchingOnesUpdated() throws IOException {
    // Given
    String accountId = "test-account-multiple";

    FullAccountResponseApiDto response = new FullAccountResponseApiDto();
    response.setId(accountId);

    SourcesContainerApiDto sources = new SourcesContainerApiDto();
    List<SourceApiDto> sourceList = new ArrayList<>();

    // Add multiple Spotify sources
    OffsetDateTime originalTimestamp = OffsetDateTime.now().minusDays(10);
    SourceApiDto source1 = createSource("15", "user1", "token1");
    source1.setUpdatedOn(originalTimestamp);
    SourceApiDto source2 = createSource("15", "user2", "token2");
    source2.setUpdatedOn(originalTimestamp);
    SourceApiDto source3 = createSource("15", "user3", "token3");
    source3.setUpdatedOn(originalTimestamp);

    sourceList.add(source1);
    sourceList.add(source2);
    sourceList.add(source3);

    sources.setSource(sourceList);
    response.setSources(sources);

    // Mock SpotifyAccountService to return only matching accounts for user1 and user3
    OffsetDateTime updatedTimestamp1 = OffsetDateTime.now().minusDays(1);
    OffsetDateTime updatedTimestamp3 = OffsetDateTime.now().minusDays(2);
    SpotifyAccount account1 =
        new SpotifyAccount(
            "user1", "User 1", "new-token1", OffsetDateTime.now(), updatedTimestamp1, null);
    SpotifyAccount account3 =
        new SpotifyAccount(
            "user3", "User 3", "new-token3", OffsetDateTime.now(), updatedTimestamp3, null);
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of(account1, account3));

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then
    assertThat(result).isPresent();
    List<SourceApiDto> resultSources = result.get().getSources().getSource();
    assertThat(resultSources.get(0).getCredential().getValue()).isEqualTo("new-token1");
    assertThat(resultSources.get(0).getUpdatedOn()).isEqualTo(updatedTimestamp1);
    assertThat(resultSources.get(1).getCredential().getValue()).isEqualTo("token2"); // Unchanged
    assertThat(resultSources.get(1).getUpdatedOn()).isEqualTo(originalTimestamp); // Unchanged
    assertThat(resultSources.get(2).getCredential().getValue()).isEqualTo("new-token3");
    assertThat(resultSources.get(2).getUpdatedOn()).isEqualTo(updatedTimestamp3);
  }

  @Test
  void testPatch_EmptySpotifyAccountList_NoChanges() throws IOException {
    // Given
    String accountId = "test-account-empty-spotify";
    String spotifyUserId = "spotify-user-999";
    String originalToken = "original-token";

    FullAccountResponseApiDto response =
        createFullAccountWithSpotifySources(spotifyUserId, originalToken);

    // Mock SpotifyAccountService to return empty list
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of());

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - Credential should remain unchanged
    assertThat(result).isPresent();
    SourceApiDto spotifySource = result.get().getSources().getSource().getFirst();
    assertThat(spotifySource.getCredential().getValue()).isEqualTo(originalToken);
  }

  @Test
  void testInjectDevicesFromDatabase_newDeviceAdded() throws IOException {
    // Given: account XML with one device, DB has an additional device for the same account
    String accountId = "test-account-new-device";

    FullAccountResponseApiDto response = new FullAccountResponseApiDto();
    response.setId(accountId);
    DevicesContainerApiDto devices = new DevicesContainerApiDto();
    DeviceApiDto existingDevice = new DeviceApiDto();
    existingDevice.setDeviceid("EXISTING_DEVICE");
    devices.addDeviceItem(existingDevice);
    response.setDevices(devices);
    response.setSources(new SourcesContainerApiDto());

    var now = OffsetDateTime.now();
    Device dbDevice =
        Device.builder()
            .deviceId("NEW_DEVICE_FROM_DB")
            .name("New Speaker")
            .ipAddress("192.168.1.99")
            .margeAccountId(accountId)
            .firstSeen(now.minusDays(1))
            .lastSeen(now)
            .updatedOn(now)
            .build();

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of(dbDevice));
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then: both devices present
    assertThat(result).isPresent();
    var deviceIds =
        result.get().getDevices().getDevice().stream().map(DeviceApiDto::getDeviceid).toList();
    assertThat(deviceIds).containsExactlyInAnyOrder("EXISTING_DEVICE", "NEW_DEVICE_FROM_DB");
  }

  @Test
  void testInjectDevicesFromDatabase_doesNotDuplicateExistingDevice() throws IOException {
    // Given: account XML already contains a device that is also in the DB
    String accountId = "test-account-no-dup";

    FullAccountResponseApiDto response = new FullAccountResponseApiDto();
    response.setId(accountId);
    DevicesContainerApiDto devices = new DevicesContainerApiDto();
    DeviceApiDto existingDevice = new DeviceApiDto();
    existingDevice.setDeviceid("SHARED_DEVICE");
    devices.addDeviceItem(existingDevice);
    response.setDevices(devices);
    response.setSources(new SourcesContainerApiDto());

    var now = OffsetDateTime.now();
    Device dbDevice =
        Device.builder()
            .deviceId("SHARED_DEVICE")
            .name("Shared Speaker")
            .ipAddress("192.168.1.10")
            .margeAccountId(accountId)
            .firstSeen(now.minusDays(2))
            .lastSeen(now)
            .updatedOn(now)
            .build();

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of(dbDevice));
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then: only one device in the list (no duplication)
    assertThat(result).isPresent();
    assertThat(result.get().getDevices().getDevice()).hasSize(1);
    assertThat(result.get().getDevices().getDevice().getFirst().getDeviceid())
        .isEqualTo("SHARED_DEVICE");
  }

  // Helper methods

  private FullAccountResponseApiDto createFullAccountWithSpotifySources(
      String username, String credentialValue) {
    FullAccountResponseApiDto response = new FullAccountResponseApiDto();
    response.setId("test-account");

    SourcesContainerApiDto sources = new SourcesContainerApiDto();
    List<SourceApiDto> sourceList = new ArrayList<>();

    SourceApiDto spotifySource = createSource("15", username, credentialValue);
    sourceList.add(spotifySource);

    sources.setSource(sourceList);
    response.setSources(sources);

    return response;
  }

  private static SourceApiDto createSource(
      String sourceproviderid, String username, String credentialValue) {
    SourceApiDto source =
        new SourceApiDto()
            .id(SPOTIFY_SOURCE_ID)
            .type("Audio")
            .sourceproviderid(sourceproviderid)
            .username(username)
            .name("Test Source")
            .sourcename("Test Source Name")
            .createdOn(OffsetDateTime.parse("2018-11-03T10:15:30+01:00"))
            .updatedOn(OffsetDateTime.parse("2022-12-03T10:15:30+01:00"));

    CredentialApiDto credential = new CredentialApiDto();
    credential.setType("token");
    credential.setValue(credentialValue);
    source.setCredential(credential);

    return source;
  }

  @Test
  void testPatch_NestedPresetsSourcesPatched() throws IOException {
    // Given
    String accountId = "test-account-nested-presets";
    String spotifyUserId = "spotify-user-preset";
    String originalToken = "old-preset-token";
    String newRefreshToken = "new-preset-token";
    OffsetDateTime updatedTimestamp = OffsetDateTime.now().minusDays(1);

    FullAccountResponseApiDto fullAccount = new FullAccountResponseApiDto();
    fullAccount.setId(accountId);

    // Create device with preset containing Spotify source
    DevicesContainerApiDto devices = new DevicesContainerApiDto();
    List<DeviceApiDto> deviceList = new ArrayList<>();

    DeviceApiDto device = new DeviceApiDto();
    device.setDeviceid("device-123");

    PresetsContainerApiDto presets = new PresetsContainerApiDto();
    List<PresetApiDto> presetList = new ArrayList<>();

    PresetApiDto preset = new PresetApiDto();
    preset.setButtonNumber(1);
    preset.setName("Test Preset");
    preset.setContainerArt("https://example.org/art.png");
    preset.setContentItemType("tracklisturl");
    preset.setLocation("/playback/container/123");
    preset.setCreatedOn(OffsetDateTime.now());
    preset.setUpdatedOn(OffsetDateTime.now());
    preset.setUsername("Test");

    SourceApiDto presetSource = createSource("15", spotifyUserId, originalToken);
    preset.setSource(presetSource);

    presetList.add(preset);
    presets.setPreset(presetList);
    device.setPresets(presets);

    deviceList.add(device);
    devices.setDevice(deviceList);
    fullAccount.setDevices(devices);
    fullAccount.setSources(new SourcesContainerApiDto());

    // Mock SpotifyAccountService
    SpotifyAccount spotifyAccount =
        new SpotifyAccount(
            spotifyUserId,
            "Test User",
            newRefreshToken,
            OffsetDateTime.now(),
            updatedTimestamp,
            null);
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of(spotifyAccount));

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(fullAccount);
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then
    assertThat(result).isPresent();
    PresetApiDto resultPreset =
        result.get().getDevices().getDevice().getFirst().getPresets().getPreset().getFirst();
    assertThat(resultPreset.getSource().getCredential().getValue()).isEqualTo(newRefreshToken);
    assertThat(resultPreset.getSource().getUpdatedOn()).isEqualTo(updatedTimestamp);
  }

  @Test
  void testPatch_NestedRecentsSourcesPatched() throws IOException {
    // Given
    String accountId = "test-account-nested-recents";
    String newRefreshToken = "new-recent-token";
    var now = OffsetDateTime.now();
    var spotifyAccountUpdatedAt = now.minusDays(2);
    var spotifyAccount =
        new SpotifyAccount(
            SPOTIFY_USER_ID, "Test User", newRefreshToken, now, spotifyAccountUpdatedAt, 1L);
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of(spotifyAccount));

    var fullAccount = createFullAccountDto(accountId);
    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(fullAccount);
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());

    Recent recent =
        Recent.builder()
            .id(1L)
            .name("Recent Item")
            .contentItemType("tracklisturl")
            .location("/playback/container/456")
            .createdOn(now)
            .lastPlayedAt(now)
            .updatedOn(now)
            .sourceId(SPOTIFY_SOURCE_ID)
            .build();
    List<Recent> recentList = List.of(recent);
    when(recentService.getRecents(accountId)).thenReturn(recentList);

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then
    assertThat(result).isPresent();
    RecentItemApiDto resultRecent =
        result.get().getDevices().getDevice().getFirst().getRecents().getRecent().getFirst();
    assertThat(resultRecent.getSource().getCredential().getValue()).isEqualTo(newRefreshToken);
    assertThat(resultRecent.getSource().getUpdatedOn()).isEqualTo(spotifyAccountUpdatedAt);
  }

  private static @NonNull FullAccountResponseApiDto createFullAccountDto(String accountId) {
    String originalToken = "old-recent-token";

    FullAccountResponseApiDto fullAccount = new FullAccountResponseApiDto();
    fullAccount.setId(accountId);
    // Create device with recent containing Spotify source
    DevicesContainerApiDto devices = new DevicesContainerApiDto();
    List<DeviceApiDto> deviceList = new ArrayList<>();

    DeviceApiDto device = new DeviceApiDto();
    device.setDeviceid("device-456");

    RecentsContainerApiDto recents = new RecentsContainerApiDto();
    device.setRecents(recents);
    deviceList.add(device);
    devices.setDevice(deviceList);
    fullAccount.setDevices(devices);

    SourceApiDto source = createSource("15", SPOTIFY_USER_ID, originalToken);

    fullAccount.setSources(new SourcesContainerApiDto().addSourceItem(source));

    return fullAccount;
  }

  @Test
  void testPatch_CombinedTopLevelAndNestedSources() throws IOException {
    // Given
    String accountId = "test-account-combined";
    String spotifyUserId = "spotify-user-combined";
    String originalToken = "old-token";
    String newRefreshToken = "new-token";
    OffsetDateTime updatedTimestamp = OffsetDateTime.now().minusDays(1);

    FullAccountResponseApiDto response = new FullAccountResponseApiDto();
    response.setId(accountId);

    // Add top-level source
    SourcesContainerApiDto sources = new SourcesContainerApiDto();
    List<SourceApiDto> sourceList = new ArrayList<>();
    SourceApiDto topLevelSource = createSource("15", spotifyUserId, originalToken);
    sourceList.add(topLevelSource);
    sources.setSource(sourceList);
    response.setSources(sources);

    // Add device with preset and recent
    DevicesContainerApiDto devices = new DevicesContainerApiDto();
    List<DeviceApiDto> deviceList = new ArrayList<>();

    DeviceApiDto device = new DeviceApiDto();
    device.setDeviceid("device-combined");

    // Add preset with Spotify source
    PresetsContainerApiDto presets = new PresetsContainerApiDto();
    List<PresetApiDto> presetList = new ArrayList<>();
    PresetApiDto preset = new PresetApiDto();
    preset.setButtonNumber(1);
    preset.setName("Preset");
    preset.setContainerArt("https://example.org/art.png");
    preset.setContentItemType("tracklisturl");
    preset.setLocation("/playback/container/123");
    preset.setCreatedOn(OffsetDateTime.now());
    preset.setUpdatedOn(OffsetDateTime.now());
    preset.setUsername("Test");
    preset.setSource(createSource("15", spotifyUserId, originalToken));
    presetList.add(preset);
    presets.setPreset(presetList);
    device.setPresets(presets);

    // Add recent with Spotify source
    RecentsContainerApiDto recents = new RecentsContainerApiDto();
    recents.setRecent(List.of());
    device.setRecents(recents);

    deviceList.add(device);
    devices.setDevice(deviceList);
    response.setDevices(devices);

    // Mock SpotifyAccountService
    SpotifyAccount spotifyAccount =
        new SpotifyAccount(
            spotifyUserId,
            "Test User",
            newRefreshToken,
            OffsetDateTime.now(),
            updatedTimestamp,
            1L);
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of(spotifyAccount));

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());

    var recent =
        Recent.builder()
            .id(1L)
            .name("Recent")
            .contentItemType("tracklisturl")
            .location("/playback/container/456")
            .createdOn(OffsetDateTime.now())
            .lastPlayedAt(OffsetDateTime.now())
            .updatedOn(OffsetDateTime.now())
            .sourceId(SPOTIFY_SOURCE_ID)
            .build();
    when(recentService.getRecents(accountId)).thenReturn(List.of(recent));

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - All three sources should be patched
    assertThat(result).isPresent();
    FullAccountResponseApiDto resultResponse = result.get();

    // Check top-level source
    assertThat(resultResponse.getSources().getSource().getFirst().getCredential().getValue())
        .isEqualTo(newRefreshToken);
    assertThat(resultResponse.getSources().getSource().getFirst().getUpdatedOn())
        .isEqualTo(updatedTimestamp);

    // Check preset source
    PresetApiDto resultPreset =
        resultResponse.getDevices().getDevice().getFirst().getPresets().getPreset().getFirst();
    assertThat(resultPreset.getSource().getCredential().getValue()).isEqualTo(newRefreshToken);
    assertThat(resultPreset.getSource().getUpdatedOn()).isEqualTo(updatedTimestamp);

    // Check recent source
    RecentItemApiDto resultRecent =
        resultResponse.getDevices().getDevice().getFirst().getRecents().getRecent().getFirst();
    assertThat(resultRecent.getSource().getCredential().getValue()).isEqualTo(newRefreshToken);
    assertThat(resultRecent.getSource().getUpdatedOn()).isEqualTo(updatedTimestamp);
  }

  @Test
  void testPatch_HandlesNullDevices() throws IOException {
    // Given
    String accountId = "test-account-null-devices";

    FullAccountResponseApiDto response = new FullAccountResponseApiDto();
    response.setId(accountId);
    response.setDevices(null);

    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of());
    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - Should not throw exception; devices is initialized to an empty container
    assertThat(result).isPresent();
    assertThat(result.get().getDevices()).isNotNull();
    assertThat(result.get().getDevices().getDevice()).isEmpty();
  }

  @Test
  void testPatch_HandlesEmptyDeviceList() throws IOException {
    // Given
    String accountId = "test-account-empty-devices";

    FullAccountResponseApiDto fullAccount = new FullAccountResponseApiDto();
    fullAccount.setId(accountId);

    DevicesContainerApiDto devices = new DevicesContainerApiDto();
    devices.setDevice(new ArrayList<>());
    fullAccount.setDevices(devices);
    fullAccount.setSources(new SourcesContainerApiDto());

    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of());
    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(fullAccount);
    when(deviceRepository.findAllByMargeAccountId(accountId)).thenReturn(List.of());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - Should not throw exception
    assertThat(result).isPresent();
    assertThat(result.get().getDevices().getDevice()).isEmpty();
  }
}
