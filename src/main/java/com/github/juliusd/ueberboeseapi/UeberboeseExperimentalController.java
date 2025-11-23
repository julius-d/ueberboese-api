package com.github.juliusd.ueberboeseapi;

import com.github.juliusd.ueberboeseapi.generated.ExperimentalApi;
import com.github.juliusd.ueberboeseapi.generated.dtos.AccountSourceApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.AttachedProductApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.CredentialApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.DeviceApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.DeviceSourceApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.DevicesContainerApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.FullAccountResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetsContainerApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentItemApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentsContainerApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourcesContainerApiDto;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "ueberboese.experimental.enabled", havingValue = "true")
public class UeberboeseExperimentalController implements ExperimentalApi {
  @Override
  public ResponseEntity<FullAccountResponseApiDto> getFullAccount(String accountId) {
    FullAccountResponseApiDto response = new FullAccountResponseApiDto();

    // Set basic account information
    response.setId(accountId);
    response.setAccountStatus("CHANGE_PASSWORD");
    response.setMode("global");
    response.setPreferredLanguage("de");

    // Create devices
    DevicesContainerApiDto devicesContainer = new DevicesContainerApiDto();
    List<DeviceApiDto> devices = new ArrayList<>();

    // First device (based on log data)
    DeviceApiDto device1 = createDevice1();
    devices.add(device1);

    // Second device (based on log data)
    DeviceApiDto device2 = createDevice2();
    devices.add(device2);

    devicesContainer.setDevice(devices);
    response.setDevices(devicesContainer);

    // Create account-level sources
    SourcesContainerApiDto sourcesContainer = new SourcesContainerApiDto();
    List<AccountSourceApiDto> sources = new ArrayList<>();

    sources.add(createAccountSource("19989552", "2", "", ""));
    sources.add(
        createAccountSource(
            "25443887",
            "7",
            "647d54be-81e8-4351-95b1-30775853c8af/0",
            "Mediaserver",
            "",
            "token",
            "647d54be-81e8-4351-95b1-30775853c8af/0"));
    sources.add(createAccountSource("21465524", "11", "", "", "eyJWasgeHt2="));
    sources.add(
        createAccountSource(
            "20260226",
            "15",
            "user2namespot",
            "user2@example.org",
            "token-User2-Spot",
            "token_version_3",
            "user2namespot"));
    sources.add(
        createAccountSource(
            "19989643",
            "15",
            "user1namespot",
            "user1@example.org",
            "token-User1-Spot",
            "token_version_3",
            "user1namespot"));
    sources.add(createAccountSource("19989342", "25", "", "", "eyJduTune="));
    sources.add(
        createAccountSource("26668320", "35", "", "", "cf8ba540-711c-4f1c-bebc-f0edcca14676"));

    sourcesContainer.setSource(sources);
    response.setSources(sourcesContainer);

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
  }

  private DeviceApiDto createDevice1() {
    DeviceApiDto device = new DeviceApiDto();
    device.setDeviceid("123980WER");
    device.setCreatedOn(OffsetDateTime.parse("2018-08-11T08:55:25.000+00:00"));
    device.setFirmwareVersion(
        "27.0.6.46330.5043500 epdbuild.trunk.hepdswbld04.2022-08-04T11:20:29");
    device.setIpaddress("192.168.178.2");
    device.setName("Foobar");
    device.setSerialNumber("PUP43434234");
    device.setUpdatedOn(OffsetDateTime.parse("2025-09-06T08:25:49.000+00:00"));

    // Attached product
    AttachedProductApiDto attachedProduct = new AttachedProductApiDto();
    attachedProduct.setProductCode("SoundTouch 10 sm2");
    attachedProduct.setProductlabel("soundtouch_10");
    attachedProduct.setSerialnumber("123SERIA1");
    device.setAttachedProduct(attachedProduct);

    // Presets
    device.setPresets(createPresetsForDevice());

    // Recents
    device.setRecents(createRecentsForDevice());

    return device;
  }

  private DeviceApiDto createDevice2() {
    DeviceApiDto device = new DeviceApiDto();
    device.setDeviceid("42342FF23");
    device.setCreatedOn(OffsetDateTime.parse("2020-01-25T09:21:42.000+00:00"));
    device.setFirmwareVersion(
        "27.0.6.46330.5043500 epdbuild.trunk.hepdswbld04.2022-08-04T11:20:29");
    device.setIpaddress("192.168.178.3");
    device.setName("SoundTouch 20");
    device.setSerialNumber("SERI234980432894284848");
    device.setUpdatedOn(OffsetDateTime.parse("2020-09-24T05:42:07.000+00:00"));

    // Attached product
    AttachedProductApiDto attachedProduct = new AttachedProductApiDto();
    attachedProduct.setProductCode("SoundTouch 20 sm2");
    attachedProduct.setProductlabel("soundtouch_20_series3");
    attachedProduct.setSerialnumber("7878SERI001");
    device.setAttachedProduct(attachedProduct);

    // Presets (same structure as device 1)
    device.setPresets(createPresetsForDevice());

    // Recents (same structure as device 1)
    device.setRecents(createRecentsForDevice());

    return device;
  }

  private PresetsContainerApiDto createPresetsForDevice() {
    PresetsContainerApiDto presets = new PresetsContainerApiDto();
    List<PresetApiDto> presetList = new ArrayList<>();

    // Preset 1 - Radio Foobar
    presetList.add(
        createPreset(
            1,
            "http://example.org/s80044q.png",
            "stationurl",
            "/v1/playback/station/s80044",
            "Radio Foobar",
            "19989342",
            "Radio Foobar"));

    // Preset 2 - Radio Foobar
    presetList.add(
        createPreset(
            2,
            "https://example.org/300/longtext1",
            "tracklisturl",
            "/playback/container/131311232312121212",
            "Radio Foobar",
            "19989643",
            "Radio Bar"));

    // Preset 3 - radioonetwo
    presetList.add(
        createPreset(
            3,
            "https://example.org/s25111/images/logoq.jpg?t=1",
            "stationurl",
            "/v1/playback/station/s25111",
            "radioonetwo",
            "19989342",
            "radioeins vom rbb"));

    // Preset 4 - Kids
    presetList.add(
        createPreset(
            4,
            "https://example.org/300/longtext2",
            "tracklisturl",
            "/playback/container/45345349538",
            "Kids",
            "19989643",
            "Kids"));

    // Preset 5 - Artist Radio
    presetList.add(
        createPreset(
            5,
            "",
            "tracklisturl",
            "/playback/container/urlssfdfsd",
            "Artist Radio",
            "20260226",
            "Artist Radio"));

    // Preset 6 - Komplett Entspannt
    presetList.add(
        createPreset(
            6,
            "https://example.org/image/ab67706c0000da843b38733ef58fbd3530776a42",
            "tracklisturl",
            "/playback/container/577667532256ht",
            "Komplett Entspannt",
            "19989643",
            "Komplett Entspannt"));

    presets.setPreset(presetList);
    return presets;
  }

  private PresetApiDto createPreset(
      int buttonNumber,
      String containerArt,
      String contentItemType,
      String location,
      String name,
      String sourceId,
      String username) {
    PresetApiDto preset = new PresetApiDto();
    preset.setButtonNumber(buttonNumber);
    preset.setContainerArt(containerArt);
    preset.setContentItemType(contentItemType);
    preset.setCreatedOn(OffsetDateTime.parse("2018-11-26T18:40:45.000+00:00"));
    preset.setLocation(location);
    preset.setName(name);
    preset.setUpdatedOn(OffsetDateTime.parse("2018-11-26T18:40:45.000+00:00"));
    preset.setUsername(username);

    // Create source for preset
    DeviceSourceApiDto source = createSourceForPreset(sourceId);
    preset.setSource(source);

    return preset;
  }

  private RecentsContainerApiDto createRecentsForDevice() {
    RecentsContainerApiDto recents = new RecentsContainerApiDto();
    List<RecentItemApiDto> recentList = new ArrayList<>();

    // Recent 1
    RecentItemApiDto recent1 = new RecentItemApiDto();
    recent1.setId("123");
    recent1.setContentItemType("tracklisturl");
    recent1.setCreatedOn(OffsetDateTime.parse("2025-11-09T11:40:37.000+00:00"));
    recent1.setLastplayedat(OffsetDateTime.parse("2025-11-09T19:42:09.000+00:00"));
    recent1.setLocation("/playback/container/35546f3");
    recent1.setName("A Name");
    recent1.setSourceid("19989643");
    recent1.setUpdatedOn(OffsetDateTime.parse("2025-11-09T19:42:11.000+00:00"));
    recent1.setSource(createSourceForPreset("19989643"));
    recentList.add(recent1);

    // Recent 2
    RecentItemApiDto recent2 = new RecentItemApiDto();
    recent2.setId("67889001");
    recent2.setContentItemType("stationurl");
    recent2.setCreatedOn(OffsetDateTime.parse("2018-11-27T18:20:01.000+00:00"));
    recent2.setLastplayedat(OffsetDateTime.parse("2025-11-09T19:42:06.000+00:00"));
    recent2.setLocation("/v1/playback/station/s80044");
    recent2.setName("Radio Foo");
    recent2.setSourceid("19989342");
    recent2.setUpdatedOn(OffsetDateTime.parse("2025-11-09T19:42:09.000+00:00"));
    recent2.setSource(createSourceForPreset("19989342"));
    recentList.add(recent2);

    recents.setRecent(recentList);
    return recents;
  }

  private DeviceSourceApiDto createSourceForPreset(String sourceId) {
    DeviceSourceApiDto source = new DeviceSourceApiDto();
    source.setId(sourceId);
    source.setType("Audio");
    source.setCreatedOn(OffsetDateTime.parse("2018-08-11T08:55:41.000+00:00"));
    source.setName("");
    source.setSourcename("");
    source.setUpdatedOn(OffsetDateTime.parse("2019-07-20T17:48:31.000+00:00"));
    source.setUsername("");

    var credential = new CredentialApiDto();
    if ("19989643".equals(sourceId) || "20260226".equals(sourceId)) {
      credential.setType("token_version_3");
      credential.setValue("token-User1-Spot");
      source.setSourceproviderid("15");
      source.setName("user1namespot");
      source.setSourcename("user1@example.org");
      source.setUsername("user1namespot");
    } else {
      credential.setType("token");
      credential.setValue("eyJduTune=");
      source.setSourceproviderid("25");
    }
    source.setCredential(credential);

    return source;
  }

  private AccountSourceApiDto createAccountSource(
      String id, String providerId, String name, String sourcename) {
    return createAccountSource(id, providerId, name, sourcename, "", "token", "");
  }

  private AccountSourceApiDto createAccountSource(
      String id, String providerId, String name, String sourcename, String credentialValue) {
    return createAccountSource(id, providerId, name, sourcename, credentialValue, "token", "");
  }

  private AccountSourceApiDto createAccountSource(
      String id,
      String providerId,
      String name,
      String sourcename,
      String credentialValue,
      String credentialType,
      String username) {
    AccountSourceApiDto source = new AccountSourceApiDto();
    source.setId(id);
    source.setType("Audio");
    source.setCreatedOn(OffsetDateTime.parse("2018-08-11T08:55:28.000+00:00"));
    source.setName(name);
    source.setSourceproviderid(providerId);
    source.setSourcename(sourcename);
    source.setUpdatedOn(OffsetDateTime.parse("2018-08-11T08:55:28.000+00:00"));
    source.setUsername(username);

    var credential = new CredentialApiDto();
    credential.setType(credentialType);
    credential.setValue(credentialValue);
    source.setCredential(credential);

    return source;
  }
}
