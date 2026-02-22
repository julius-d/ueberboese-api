package com.github.juliusd.ueberboeseapi;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.github.juliusd.ueberboeseapi.device.Device;
import com.github.juliusd.ueberboeseapi.preset.Preset;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.placeholder.PlaceholderDifferenceEvaluator;

@SuppressWarnings("CheckTagEmptyBody")
class UeberboeseExperimentalControllerTest extends TestBase {

  private WireMockServer wireMockServer;

  @BeforeEach
  void setUpWireMock() {
    wireMockServer = new WireMockServer(options().port(8089));
    wireMockServer.start();
  }

  @AfterEach
  void tearDownWireMock() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  @Test
  void getFullAccount_shouldReturnCompleteAccountDetails() {
    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <account id="6921042">
          <accountStatus>CHANGE_PASSWORD</accountStatus>
          <devices>
            <device deviceid="123980WER">
              <attachedProduct product_code="SoundTouch 10 sm2">
                <components/>
                <productlabel>soundtouch_10</productlabel>
                <serialnumber>123SERIA1</serialnumber>
              </attachedProduct>
              <createdOn>2018-08-11T08:55:25.000+00:00</createdOn>
              <firmwareVersion>27.0.6.46330.5043500 epdbuild.trunk.hepdswbld04.2022-08-04T11:20:29</firmwareVersion>
              <ipaddress>192.168.178.2</ipaddress>
              <name>Foobar</name>
              <presets>
                <preset buttonNumber="1">
                  <containerArt>http://example.org/s80044q.png</containerArt>
                  <contentItemType>stationurl</contentItemType>
                  <createdOn>2018-11-26T18:40:45.000+00:00</createdOn>
                  <location>/v1/playback/station/s80044</location>
                  <name>Radio Foobar</name>
                  <source id="19989342" type="Audio">
                    <createdOn>${xmlunit.isDateTime}</createdOn>
                    <credential type="token">eyJduTune=</credential>
                    <name></name>
                    <sourceproviderid>25</sourceproviderid>
                    <sourcename></sourcename>
                    <sourceSettings/>
                    <updatedOn>${xmlunit.isDateTime}</updatedOn>
                    <username></username>
                  </source>
                  <updatedOn>${xmlunit.isDateTime}</updatedOn>
                  <username>Radio Foobar</username>
                </preset>
                <preset buttonNumber="2">
                  <containerArt>
                    https://example.org/300/longtext1
                  </containerArt>
                  <contentItemType>tracklisturl</contentItemType>
                  <createdOn>${xmlunit.isDateTime}</createdOn>
                  <location>/playback/container/131311232312121212</location>
                  <name>Radio Foobar</name>
                  <source id="19989643" type="Audio">
                    <createdOn>${xmlunit.isDateTime}</createdOn>
                    <credential type="token_version_3">mockTokenUser2</credential>
                    <name>user1namespot</name>
                    <sourceproviderid>15</sourceproviderid>
                    <sourcename>user1@example.org</sourcename>
                    <sourceSettings/>
                    <updatedOn>${xmlunit.isDateTime}</updatedOn>
                    <username>user1namespot</username>
                  </source>
                  <updatedOn>${xmlunit.isDateTime}</updatedOn>
                  <username>Radio Bar</username>
                </preset>
                <preset buttonNumber="3">
                  <containerArt>https://example.org/s25111/images/logoq.jpg?t=1</containerArt>
                  <contentItemType>stationurl</contentItemType>
                  <createdOn>${xmlunit.isDateTime}</createdOn>
                  <location>/v1/playback/station/s25111</location>
                  <name>radioonetwo</name>
                  <source id="19989342" type="Audio">
                    <createdOn>${xmlunit.isDateTime}</createdOn>
                    <credential type="token">eyJduTune=</credential>
                    <name></name>
                    <sourceproviderid>25</sourceproviderid>
                    <sourcename></sourcename>
                    <sourceSettings/>
                    <updatedOn>${xmlunit.isDateTime}</updatedOn>
                    <username></username>
                  </source>
                  <updatedOn>${xmlunit.isDateTime}</updatedOn>
                  <username>radioeins vom rbb</username>
                </preset>
                <preset buttonNumber="4">
                  <containerArt>
                    https://example.org/300/longtext2
                  </containerArt>
                  <contentItemType>tracklisturl</contentItemType>
                  <createdOn>${xmlunit.isDateTime}</createdOn>
                  <location>/playback/container/45345349538</location>
                  <name>Kids</name>
                  <source id="19989643" type="Audio">
                    <createdOn>${xmlunit.isDateTime}</createdOn>
                    <credential type="token_version_3">mockTokenUser2</credential>
                    <name>user1namespot</name>
                    <sourceproviderid>15</sourceproviderid>
                    <sourcename>user1@example.org</sourcename>
                    <sourceSettings/>
                    <updatedOn>${xmlunit.isDateTime}</updatedOn>
                    <username>user1namespot</username>
                  </source>
                  <updatedOn>${xmlunit.isDateTime}</updatedOn>
                  <username>Kids</username>
                </preset>
                <preset buttonNumber="5">
                  <containerArt></containerArt>
                  <contentItemType>tracklisturl</contentItemType>
                  <createdOn>${xmlunit.isDateTime}</createdOn>
                  <location>/playback/container/urlssfdfsd</location>
                  <name>Artist Radio</name>
                  <source id="20260226" type="Audio">
                    <createdOn>${xmlunit.isDateTime}</createdOn>
                    <credential type="token_version_3">
                      ${xmlunit.ignore}
                    </credential>
                    <name>${xmlunit.ignore}</name>
                    <sourceproviderid>15</sourceproviderid>
                    <sourcename>${xmlunit.ignore}</sourcename>
                    <sourceSettings/>
                    <updatedOn>${xmlunit.isDateTime}</updatedOn>
                    <username>${xmlunit.ignore}</username>
                  </source>
                  <updatedOn>${xmlunit.isDateTime}</updatedOn>
                  <username>Artist Radio</username>
                </preset>
                <preset buttonNumber="6">
                  <containerArt>https://example.org/image/ab67706c0000da843b38733ef58fbd3530776a42
                  </containerArt>
                  <contentItemType>tracklisturl</contentItemType>
                  <createdOn>${xmlunit.isDateTime}</createdOn>
                  <location>/playback/container/577667532256ht</location>
                  <name>Komplett Entspannt</name>
                  <source id="19989643" type="Audio">
                    <createdOn>${xmlunit.isDateTime}</createdOn>
                    <credential type="token_version_3">mockTokenUser2</credential>
                    <name>user1namespot</name>
                    <sourceproviderid>15</sourceproviderid>
                    <sourcename>user1@example.org</sourcename>
                    <sourceSettings/>
                    <updatedOn>${xmlunit.isDateTime}</updatedOn>
                    <username>user1namespot</username>
                  </source>
                  <updatedOn>${xmlunit.isDateTime}</updatedOn>
                  <username>Komplett Entspannt</username>
                </preset>
              </presets>
              <recents>
                <recent id="${xmlunit.isNumber}">
                  <contentItemType>tracklisturl</contentItemType>
                  <createdOn>${xmlunit.isDateTime}</createdOn>
                  <lastplayedat>${xmlunit.isDateTime}</lastplayedat>
                  <location>/playback/container/c3BvdGlmeTphbGJ1bTowZ3BGWVZNbVV6VkVxeVAyeUh3cEha</location>
                  <name>Ghostsitter 42 - Das Haus im Moor</name>
                  <source id="19989643" type="Audio">
                    <createdOn>${xmlunit.isDateTime}</createdOn>
                    <credential type="token_version_3">mockTokenUser2</credential>
                    <name>user1namespot</name>
                    <sourceproviderid>15</sourceproviderid>
                    <sourcename>user1@example.org</sourcename>
                    <sourceSettings/>
                    <updatedOn>${xmlunit.isDateTime}</updatedOn>
                    <username>user1namespot</username>
                  </source>
                  <sourceid>19989643</sourceid>
                  <updatedOn>${xmlunit.isDateTime}</updatedOn>
                </recent>
                <recent id="${xmlunit.isNumber}">
                  <contentItemType>stationurl</contentItemType>
                  <createdOn>${xmlunit.isDateTime}</createdOn>
                  <lastplayedat>${xmlunit.isDateTime}</lastplayedat>
                  <location>/v1/playback/station/s80044</location>
                  <name>Radio TEDDY</name>
                  <source id="19989342" type="Audio">
                    <createdOn>2018-08-11T08:55:28.000+00:00</createdOn>
                    <credential type="token">eyJduTune=</credential>
                    <name/>
                    <sourceproviderid>25</sourceproviderid>
                    <sourcename/>
                    <sourceSettings/>
                    <updatedOn>${xmlunit.isDateTime}</updatedOn>
                    <username/>
                  </source>
                  <sourceid>19989342</sourceid>
                  <updatedOn>${xmlunit.isDateTime}</updatedOn>
                </recent>
              </recents>
              <serialNumber>PUP43434234</serialNumber>
              <updatedOn>2025-09-06T08:25:49.000+00:00</updatedOn>
            </device>
            <device deviceid="42342FF23">
              <attachedProduct product_code="SoundTouch 20 sm2">
                <components/>
                <productlabel>soundtouch_20_series3</productlabel>
                <serialnumber>7878SERI001</serialnumber>
              </attachedProduct>
              <createdOn>2020-01-25T09:21:42.000+00:00</createdOn>
              <firmwareVersion>27.0.6.46330.5043500 epdbuild.trunk.hepdswbld04.2022-08-04T11:20:29</firmwareVersion>
              <ipaddress>192.168.178.3</ipaddress>
              <name>SoundTouch 20</name>
              <presets>
                <preset buttonNumber="1">
                  <containerArt>http://example.org/s80044q.png</containerArt>
                  <contentItemType>stationurl</contentItemType>
                  <createdOn>2018-11-26T18:40:45.000+00:00</createdOn>
                  <location>/v1/playback/station/s80044</location>
                  <name>Radio Foobar</name>
                  <source id="19989342" type="Audio">
                    <createdOn>${xmlunit.isDateTime}</createdOn>
                    <credential type="token">eyJduTune=</credential>
                    <name></name>
                    <sourceproviderid>25</sourceproviderid>
                    <sourcename></sourcename>
                    <sourceSettings/>
                    <updatedOn>${xmlunit.isDateTime}</updatedOn>
                    <username></username>
                  </source>
                  <updatedOn>${xmlunit.isDateTime}</updatedOn>
                  <username>Radio Foobar</username>
                </preset>
                <preset buttonNumber="2">
                  <containerArt>
                    https://example.org/300/longtext1
                  </containerArt>
                  <contentItemType>tracklisturl</contentItemType>
                  <createdOn>${xmlunit.isDateTime}</createdOn>
                  <location>/playback/container/131311232312121212</location>
                  <name>Radio Foobar</name>
                  <source id="19989643" type="Audio">
                    <createdOn>${xmlunit.isDateTime}</createdOn>
                    <credential type="token_version_3">mockTokenUser2</credential>
                    <name>user1namespot</name>
                    <sourceproviderid>15</sourceproviderid>
                    <sourcename>user1@example.org</sourcename>
                    <sourceSettings/>
                    <updatedOn>${xmlunit.isDateTime}</updatedOn>
                    <username>user1namespot</username>
                  </source>
                  <updatedOn>${xmlunit.isDateTime}</updatedOn>
                  <username>Radio Bar</username>
                </preset>
                <preset buttonNumber="3">
                  <containerArt>https://example.org/s25111/images/logoq.jpg?t=1</containerArt>
                  <contentItemType>stationurl</contentItemType>
                  <createdOn>${xmlunit.isDateTime}</createdOn>
                  <location>/v1/playback/station/s25111</location>
                  <name>radioonetwo</name>
                  <source id="19989342" type="Audio">
                    <createdOn>${xmlunit.isDateTime}</createdOn>
                    <credential type="token">eyJduTune=</credential>
                    <name></name>
                    <sourceproviderid>25</sourceproviderid>
                    <sourcename></sourcename>
                    <sourceSettings/>
                    <updatedOn>${xmlunit.isDateTime}</updatedOn>
                    <username></username>
                  </source>
                  <updatedOn>${xmlunit.isDateTime}</updatedOn>
                  <username>radioeins vom rbb</username>
                </preset>
                <preset buttonNumber="4">
                  <containerArt>
                    https://example.org/300/longtext2
                  </containerArt>
                  <contentItemType>tracklisturl</contentItemType>
                  <createdOn>${xmlunit.isDateTime}</createdOn>
                  <location>/playback/container/45345349538</location>
                  <name>Kids</name>
                  <source id="19989643" type="Audio">
                    <createdOn>${xmlunit.isDateTime}</createdOn>
                    <credential type="token_version_3">mockTokenUser2</credential>
                    <name>user1namespot</name>
                    <sourceproviderid>15</sourceproviderid>
                    <sourcename>user1@example.org</sourcename>
                    <sourceSettings/>
                    <updatedOn>${xmlunit.isDateTime}</updatedOn>
                    <username>user1namespot</username>
                  </source>
                  <updatedOn>${xmlunit.isDateTime}</updatedOn>
                  <username>Kids</username>
                </preset>
                <preset buttonNumber="5">
                  <containerArt></containerArt>
                  <contentItemType>tracklisturl</contentItemType>
                  <createdOn>${xmlunit.isDateTime}</createdOn>
                  <location>/playback/container/urlssfdfsd</location>
                  <name>Artist Radio</name>
                  <source id="20260226" type="Audio">
                    <createdOn>${xmlunit.isDateTime}</createdOn>
                    <credential type="token_version_3">
                      ${xmlunit.ignore}
                    </credential>
                    <name>${xmlunit.ignore}</name>
                    <sourceproviderid>15</sourceproviderid>
                    <sourcename>${xmlunit.ignore}</sourcename>
                    <sourceSettings/>
                    <updatedOn>${xmlunit.isDateTime}</updatedOn>
                    <username>${xmlunit.ignore}</username>
                  </source>
                  <updatedOn>${xmlunit.isDateTime}</updatedOn>
                  <username>Artist Radio</username>
                </preset>
                <preset buttonNumber="6">
                  <containerArt>https://example.org/image/ab67706c0000da843b38733ef58fbd3530776a42
                  </containerArt>
                  <contentItemType>tracklisturl</contentItemType>
                  <createdOn>${xmlunit.isDateTime}</createdOn>
                  <location>/playback/container/577667532256ht</location>
                  <name>Komplett Entspannt</name>
                  <source id="19989643" type="Audio">
                    <createdOn>${xmlunit.isDateTime}</createdOn>
                    <credential type="token_version_3">mockTokenUser2</credential>
                    <name>user1namespot</name>
                    <sourceproviderid>15</sourceproviderid>
                    <sourcename>user1@example.org</sourcename>
                    <sourceSettings/>
                    <updatedOn>${xmlunit.isDateTime}</updatedOn>
                    <username>user1namespot</username>
                  </source>
                  <updatedOn>${xmlunit.isDateTime}</updatedOn>
                  <username>Komplett Entspannt</username>
                </preset>
              </presets>
              <recents>
                <recent id="${xmlunit.isNumber}">
                  <contentItemType>tracklisturl</contentItemType>
                  <createdOn>${xmlunit.isDateTime}</createdOn>
                  <lastplayedat>${xmlunit.isDateTime}</lastplayedat>
                  <location>/playback/container/c3BvdGlmeTphbGJ1bTowZ3BGWVZNbVV6VkVxeVAyeUh3cEha</location>
                  <name>Ghostsitter 42 - Das Haus im Moor</name>
                  <source id="19989643" type="Audio">
                    <createdOn>${xmlunit.isDateTime}</createdOn>
                    <credential type="token_version_3">mockTokenUser2</credential>
                    <name>user1namespot</name>
                    <sourceproviderid>15</sourceproviderid>
                    <sourcename>user1@example.org</sourcename>
                    <sourceSettings/>
                    <updatedOn>${xmlunit.isDateTime}</updatedOn>
                    <username>user1namespot</username>
                  </source>
                  <sourceid>19989643</sourceid>
                  <updatedOn>${xmlunit.isDateTime}</updatedOn>
                </recent>
                <recent id="${xmlunit.isNumber}">
                  <contentItemType>stationurl</contentItemType>
                  <createdOn>${xmlunit.isDateTime}</createdOn>
                  <lastplayedat>${xmlunit.isDateTime}</lastplayedat>
                  <location>/v1/playback/station/s80044</location>
                  <name>Radio TEDDY</name>
                  <source id="19989342" type="Audio">
                    <createdOn>2018-08-11T08:55:28.000+00:00</createdOn>
                    <credential type="token">eyJduTune=</credential>
                    <name/>
                    <sourceproviderid>25</sourceproviderid>
                    <sourcename/>
                    <sourceSettings/>
                    <updatedOn>${xmlunit.isDateTime}</updatedOn>
                    <username/>
                  </source>
                  <sourceid>19989342</sourceid>
                  <updatedOn>${xmlunit.isDateTime}</updatedOn>
                </recent>
              </recents>
              <serialNumber>SERI234980432894284848</serialNumber>
              <updatedOn>2020-09-24T05:42:07.000+00:00</updatedOn>
            </device>
          </devices>
          <mode>global</mode>
          <preferredLanguage>de</preferredLanguage>
          <sources>
            <source id="19989552" type="Audio">
              <createdOn>2018-08-11T08:55:28.000+00:00</createdOn>
              <credential type="token"></credential>
              <name></name>
              <sourceproviderid>2</sourceproviderid>
              <sourcename></sourcename>
              <sourceSettings/>
              <updatedOn>2018-08-11T08:55:28.000+00:00</updatedOn>
              <username></username>
            </source>
            <source id="25443887" type="Audio">
              <createdOn>${xmlunit.isDateTime}</createdOn>
              <credential type="token"></credential>
              <name>647d54be-81e8-4351-95b1-30775853c8af/0</name>
              <sourceproviderid>7</sourceproviderid>
              <sourcename>Mediaserver</sourcename>
              <sourceSettings/>
              <updatedOn>${xmlunit.isDateTime}</updatedOn>
              <username>647d54be-81e8-4351-95b1-30775853c8af/0</username>
            </source>
            <source id="21465524" type="Audio">
              <createdOn>${xmlunit.isDateTime}</createdOn>
              <credential type="token">eyJWasgeHt2=</credential>
              <name></name>
              <sourceproviderid>11</sourceproviderid>
              <sourcename></sourcename>
              <sourceSettings/>
              <updatedOn>${xmlunit.isDateTime}</updatedOn>
              <username></username>
            </source>
            <source id="20260226" type="Audio">
              <createdOn>${xmlunit.isDateTime}</createdOn>
              <credential type="token_version_3">
                token-User2-Spot
              </credential>
              <name>user2namespot</name>
              <sourceproviderid>15</sourceproviderid>
              <sourcename>user2@example.org</sourcename>
              <sourceSettings/>
              <updatedOn>${xmlunit.isDateTime}</updatedOn>
              <username>user2namespot</username>
            </source>
            <source id="19989643" type="Audio">
              <createdOn>${xmlunit.isDateTime}</createdOn>
              <credential type="token_version_3">
                mockTokenUser2
              </credential>
              <name>user1namespot</name>
              <sourceproviderid>15</sourceproviderid>
              <sourcename>user1@example.org</sourcename>
              <sourceSettings/>
              <updatedOn>${xmlunit.isDateTime}</updatedOn>
              <username>user1namespot</username>
            </source>
            <source id="19989342" type="Audio">
              <createdOn>${xmlunit.isDateTime}</createdOn>
              <credential type="token">eyJduTune=</credential>
              <name></name>
              <sourceproviderid>25</sourceproviderid>
              <sourcename></sourcename>
              <sourceSettings/>
              <updatedOn>${xmlunit.isDateTime}</updatedOn>
              <username></username>
            </source>
            <source id="26668320" type="Audio">
              <createdOn>${xmlunit.isDateTime}</createdOn>
              <credential type="token">cf8ba540-711c-4f1c-bebc-f0edcca14676</credential>
              <name></name>
              <sourceproviderid>35</sourceproviderid>
              <sourcename></sourcename>
              <sourceSettings/>
              <updatedOn>${xmlunit.isDateTime}</updatedOn>
              <username></username>
            </source>
           </sources>
        <providerSettings/>
        </account>
        """;

    givenRecentsInDB();
    givenSpotifyAccountsInDB();

    var responseXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .header("Authorization", "Bearer foo/qtwq6FH/bar")
            .when()
            .get("/streaming/account/6921042/full")
            .then()
            .statusCode(200)
            .contentType("application/vnd.bose.streaming-v1.2+xml")
            .header("METHOD_NAME", "getFullAccount")
            .extract()
            .body()
            .asString();

    assertThat(
        responseXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }

  @Test
  void getFullAccount_shouldReturnCorrectDeviceDetails() {
    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "Bearer foo/qtwq6FH/bar")
        .when()
        .get("/streaming/account/6921042/full")
        .then()
        .statusCode(200)
        .body(containsString("<name>Foobar</name>"))
        .body(containsString("<name>SoundTouch 20</name>"))
        .body(
            containsString(
                "<firmwareVersion>27.0.6.46330.5043500 epdbuild.trunk.hepdswbld04.2022-08-04T11:20:29</firmwareVersion>"))
        .body(containsString("<ipaddress>192.168.178.2</ipaddress>"))
        .body(containsString("<ipaddress>192.168.178.3</ipaddress>"))
        .body(containsString("<productlabel>soundtouch_10</productlabel>"))
        .body(containsString("<productlabel>soundtouch_20_series3</productlabel>"));
  }

  @Test
  void getFullAccount_shouldHandleDifferentAccountId() {
    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "Bearer test-token")
        .when()
        .get("/streaming/account/1234567/full")
        .then()
        .statusCode(200)
        .contentType("application/vnd.bose.streaming-v1.2+xml")
        .body(containsString("<account id=\"1234567\">"))
        .body(containsString("<accountStatus>CHANGE_PASSWORD</accountStatus>"))
        .body(containsString("<mode>global</mode>"));
  }

  @Test
  void getFullAccount_shouldCacheThenServeFromCache() throws Exception {
    // Given - use unique account ID that doesn't have cached file
    String testAccountId = "cache-roundtrip-test";
    Path cacheFile =
        Path.of("src/test/resources/test-data", "streaming-account-full-" + testAccountId + ".xml");

    // Clean up cache file if exists from previous test run
    Files.deleteIfExists(cacheFile);

    // language=XML
    String mockXmlResponse =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <account id="cache-roundtrip-test">
          <accountStatus>ACTIVE</accountStatus>
          <mode>regional</mode>
          <preferredLanguage>en</preferredLanguage>
        </account>
        """;

    wireMockServer.stubFor(
        get(urlEqualTo("/streaming/account/" + testAccountId + "/full"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
                    .withBody(mockXmlResponse)));

    // When - First request (cache miss)
    String firstResponse =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .when()
            .get("/streaming/account/" + testAccountId + "/full")
            .then()
            .statusCode(200)
            .contentType("application/vnd.bose.streaming-v1.2+xml")
            .extract()
            .body()
            .asString();

    // Then - Verify proxy was called once
    wireMockServer.verify(
        1, getRequestedFor(urlEqualTo("/streaming/account/" + testAccountId + "/full")));

    // Verify cache file was created
    assertTrue(Files.exists(cacheFile), "Cache file should exist after first request");

    // When - Second request (cache hit)
    String secondResponse =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .when()
            .get("/streaming/account/" + testAccountId + "/full")
            .then()
            .statusCode(200)
            .contentType("application/vnd.bose.streaming-v1.2+xml")
            .extract()
            .body()
            .asString();

    // Then - Verify proxy was STILL only called once (not twice)
    wireMockServer.verify(
        1, getRequestedFor(urlEqualTo("/streaming/account/" + testAccountId + "/full")));

    // Verify responses are identical
    assertThat(firstResponse, isSimilarTo(secondResponse).ignoreWhitespace());

    // Cleanup - remove test cache file
    Files.deleteIfExists(cacheFile);
  }

  @Test
  void updatePreset_shouldUpdatePresetSuccessfully() {
    // language=XML
    String requestXml =
        """
        <?xml version="1.0" encoding="UTF-8" ?>
        <preset buttonNumber="2">
          <sourceid>19989643</sourceid>
          <name>Radio Mix</name>
          <username>Radio Mix</username>
          <location>/playback/container/c3BvdGlmeTpwbGF5bGlzdDoyM1NNZHlPSEE2S2t6SG9QT0o1S1E5</location>
          <contentItemType>tracklisturl</contentItemType>
          <containerArt>https://image-cdn-ak.spotifycdn.com/image/ab67706c0000da84993ee084406c4089ad8f4b2a</containerArt>
        </preset>""";

    // Extract the response for XML comparison
    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .header("Authorization", "Bearer mockToken123")
            .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
            .body(requestXml)
            .when()
            .put("/streaming/account/6921042/device/587A628A4042/preset/2")
            .then()
            .statusCode(200)
            .contentType("application/vnd.bose.streaming-v1.2+xml")
            .header(
                "Location",
                containsString(
                    "http://streamingqa.bose.com/account/6921042/device/587A628A4042/preset/2"))
            .extract()
            .body()
            .asString();

    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <preset buttonNumber="2">
          <containerArt>https://image-cdn-ak.spotifycdn.com/image/ab67706c0000da84993ee084406c4089ad8f4b2a</containerArt>
          <contentItemType>tracklisturl</contentItemType>
          <createdOn>${xmlunit.isDateTime}</createdOn>
          <location>/playback/container/c3BvdGlmeTpwbGF5bGlzdDoyM1NNZHlPSEE2S2t6SG9QT0o1S1E5</location>
          <name>Radio Mix</name>
          <source id="19989643" type="Audio">
            <createdOn>${xmlunit.isDateTime}</createdOn>
            <credential type="token_version_3">${xmlunit.ignore}</credential>
            <name>${xmlunit.ignore}</name>
            <sourceproviderid>15</sourceproviderid>
            <sourcename>${xmlunit.ignore}</sourcename>
            <sourceSettings/>
            <updatedOn>${xmlunit.isDateTime}</updatedOn>
            <username>${xmlunit.ignore}</username>
          </source>
          <updatedOn>${xmlunit.isDateTime}</updatedOn>
          <username>${xmlunit.ignore}</username>
        </preset>""";

    org.hamcrest.MatcherAssert.assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }

  @Test
  void updatePreset_shouldSaveToDatabase() {
    // Given - presetRepository is injected via TestBase

    // language=XML
    String requestXml =
        """
        <?xml version="1.0" encoding="UTF-8" ?>
        <preset buttonNumber="3">
          <sourceid>19989643</sourceid>
          <name>My Playlist</name>
          <username>My Playlist</username>
          <location>/playback/container/test123</location>
          <contentItemType>tracklisturl</contentItemType>
          <containerArt>https://example.org/art123.png</containerArt>
        </preset>""";

    // When
    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
        .body(requestXml)
        .when()
        .put("/streaming/account/testaccount/device/testdevice/preset/3")
        .then()
        .statusCode(200);

    // Then - verify preset was saved to database
    Optional<Preset> saved =
        presetRepository.findByAccountIdAndDeviceIdAndButtonNumber("testaccount", "testdevice", 3);
    assertThat(saved).isPresent();
    assertThat(saved.get().name()).isEqualTo("My Playlist");
    assertThat(saved.get().location()).isEqualTo("/playback/container/test123");
    assertThat(saved.get().sourceId()).isEqualTo("19989643");
    assertThat(saved.get().containerArt()).isEqualTo("https://example.org/art123.png");
  }

  @Test
  void updatePreset_shouldUpsertExisting() {
    // Given - existing preset (presetRepository is injected via TestBase)
    var now = java.time.OffsetDateTime.now().withNano(0);
    Preset existing =
        Preset.builder()
            .accountId("testaccount2")
            .deviceId("testdevice2")
            .buttonNumber(1)
            .name("Old Name")
            .location("/old/location")
            .sourceId("old-source")
            .containerArt("https://example.org/old.png")
            .contentItemType("stationurl")
            .createdOn(now)
            .updatedOn(now)
            .build();
    Preset saved = presetRepository.save(existing);
    Long existingId = saved.id();

    // language=XML
    String requestXml =
        """
        <?xml version="1.0" encoding="UTF-8" ?>
        <preset buttonNumber="1">
          <sourceid>new-source</sourceid>
          <name>Updated Name</name>
          <username>Updated Name</username>
          <location>/new/location</location>
          <contentItemType>tracklisturl</contentItemType>
          <containerArt>https://example.org/new.png</containerArt>
        </preset>""";

    // When - update with same button number
    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
        .body(requestXml)
        .when()
        .put("/streaming/account/testaccount2/device/testdevice2/preset/1")
        .then()
        .statusCode(200);

    // Then - verify preset was updated (same ID)
    Optional<Preset> updated =
        presetRepository.findByAccountIdAndDeviceIdAndButtonNumber(
            "testaccount2", "testdevice2", 1);
    assertThat(updated).isPresent();
    assertThat(updated.get().id()).isEqualTo(existingId); // Same ID
    assertThat(updated.get().name()).isEqualTo("Updated Name");
    assertThat(updated.get().location()).isEqualTo("/new/location");
    assertThat(updated.get().sourceId()).isEqualTo("new-source");
  }

  @Test
  void getSoftwareUpdate_shouldReturnEmptyUpdateLocation() {
    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <software_update>
          <softwareUpdateLocation></softwareUpdateLocation>
        </software_update>
        """;

    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .header("Authorization", "Bearer mockToken123")
            .when()
            .get("/streaming/software/update/account/6921042")
            .then()
            .statusCode(200)
            .contentType("application/vnd.bose.streaming-v1.2+xml")
            .extract()
            .body()
            .asString();

    assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }

  @Test
  void updateDevice_shouldUpdateDeviceNameSuccessfully() {
    var existingDevice =
        Device.builder()
            .deviceId("587A628A4042")
            .name("Old Name")
            .ipAddress("192.168.178.33")
            .firstSeen(OffsetDateTime.parse("2018-08-11T08:55:25.000+00:00"))
            .lastSeen(OffsetDateTime.parse("2025-01-01T10:00:00.000+00:00"))
            .version(null)
            .build();
    deviceRepository.save(existingDevice);

    // language=XML
    String requestXml =
        """
        <?xml version="1.0" encoding="UTF-8" ?>
        <device deviceid="587A628A4042">
          <name>Test Device</name>
          <macaddress>587A628A4042</macaddress>
        </device>""";

    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .header("Authorization", "Bearer mockToken123")
            .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
            .body(requestXml)
            .when()
            .put("/streaming/account/6921042/device/587A628A4042")
            .then()
            .statusCode(200)
            .contentType("application/vnd.bose.streaming-v1.2+xml")
            .header(
                "Location",
                containsString("http://streamingqa.bose.com/account/6921042/device/587A628A4042"))
            .header("METHOD_NAME", "updateDevice")
            .extract()
            .body()
            .asString();

    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <device deviceid="587A628A4042">
          <createdOn>2018-08-11T08:55:25.000+00:00</createdOn>
          <ipaddress>192.168.178.33</ipaddress>
          <name>Test Device</name>
          <updatedOn>${xmlunit.isDateTime}</updatedOn>
        </device>""";

    org.hamcrest.MatcherAssert.assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));

    // Verify database was updated
    Device updatedDevice = deviceRepository.findById("587A628A4042").orElseThrow();
    assertThat(updatedDevice.name()).isEqualTo("Test Device");
  }

  @Test
  void updateDevice_shouldCreateNewDeviceWhenNotExists() {
    // No existing device - will trigger the orElseGet branch

    // language=XML
    String requestXml =
        """
        <?xml version="1.0" encoding="UTF-8" ?>
        <device deviceid="NEW_DEVICE_123">
          <name>New Device</name>
          <macaddress>NEW_DEVICE_123</macaddress>
        </device>""";

    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .header("Authorization", "Bearer mockToken123")
            .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
            .body(requestXml)
            .when()
            .put("/streaming/account/6921042/device/NEW_DEVICE_123")
            .then()
            .statusCode(200)
            .contentType("application/vnd.bose.streaming-v1.2+xml")
            .header(
                "Location",
                containsString("http://streamingqa.bose.com/account/6921042/device/NEW_DEVICE_123"))
            .header("METHOD_NAME", "updateDevice")
            .extract()
            .body()
            .asString();

    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <device deviceid="NEW_DEVICE_123">
          <createdOn>${xmlunit.isDateTime}</createdOn>
          <ipaddress>${xmlunit.ignore}</ipaddress>
          <name>New Device</name>
          <updatedOn>${xmlunit.isDateTime}</updatedOn>
        </device>""";

    assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));

    // Verify database was updated
    Device newDevice = deviceRepository.findById("NEW_DEVICE_123").orElseThrow();
    assertThat(newDevice.name()).isEqualTo("New Device");
    assertThat(newDevice.ipAddress()).isNull();
  }

  @Test
  void getStreamingToken_shouldReturnAuthorizationHeader() {
    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "Bearer inputToken123")
        .when()
        .get("/streaming/device/587A628A4042/streaming_token")
        .then()
        .statusCode(200)
        .header("Authorization", "mockRefreshedToken123xyz");
  }

  @Test
  void getProviderSettings_shouldReturnEmptyResponse() {
    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "Bearer mockToken123")
        .when()
        .get("/streaming/account/6921042/provider_settings")
        .then()
        .statusCode(200)
        .contentType("application/vnd.bose.streaming-v1.2+xml")
        .header("METHOD_NAME", "getProviderSettings");
  }

  @Test
  void customerSupport_shouldAcceptDeviceDiagnosticData() {
    // language=XML
    String requestXml =
        """
        <?xml version="1.0" encoding="UTF-8" ?>
        <device-data>
          <device id="587A628A4042">
            <serialnumber>P123456789101123456789</serialnumber>
            <firmware-version>27.0.6.46330.5043500 epdbuild.trunk.hepdswbld04.2022-08-04T11:20:29</firmware-version>
            <product product_code="SoundTouch 10 sm2" type="5">
              <serialnumber>069236P81556160AE</serialnumber>
            </product>
          </device>
          <diagnostic-data>
            <device-landscape>
              <rssi>Good</rssi>
              <gateway-ip-address>192.168.1.1</gateway-ip-address>
              <macaddresses>
                <macaddress>587A628A4042</macaddress>
                <macaddress>40BD32BAB0EB</macaddress>
              </macaddresses>
              <ip-address>192.168.1.100</ip-address>
              <network-connection-type>Wireless</network-connection-type>
            </device-landscape>
            <network-landscape>
              <network-data xmlns="http://www.Bose.com/Schemas/2012-12/NetworkMonitor/"/>
            </network-landscape>
          </diagnostic-data>
        </device-data>
        """;

    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "Bearer mockToken123")
        .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
        .body(requestXml)
        .when()
        .post("/streaming/support/customersupport")
        .then()
        .statusCode(200)
        .contentType("application/vnd.bose.streaming-v1.2+xml");
  }

  @Test
  void deletePreset_shouldRemovePresetSuccessfully() {
    // Given - Create a preset first
    Preset preset =
        Preset.builder()
            .accountId("6921042")
            .deviceId("587A628A4042")
            .buttonNumber(1)
            .containerArt("http://example.com/art.png")
            .contentItemType("stationurl")
            .location("/v1/playback/station/s12345")
            .name("Test Station")
            .sourceId("19989342")
            .createdOn(OffsetDateTime.now())
            .updatedOn(OffsetDateTime.now())
            .build();
    presetRepository.save(preset);

    // When - Delete the preset
    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("Authorization", "Bearer mockBearerTokenABC123xyz=")
        .when()
        .delete("/streaming/account/6921042/device/587A628A4042/preset/1")
        .then()
        .statusCode(200)
        .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml");

    // Then - Verify preset was deleted from database
    Optional<Preset> deleted =
        presetRepository.findByAccountIdAndDeviceIdAndButtonNumber("6921042", "587A628A4042", 1);
    assertThat(deleted).isEmpty();
  }

  @Test
  void deletePreset_shouldReturn404WhenPresetNotFound() {
    // When - Try to delete non-existent preset
    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("Authorization", "Bearer mockBearerTokenABC123xyz=")
            .when()
            .delete("/streaming/account/6921042/device/587A628A4042/preset/5")
            .then()
            .statusCode(404)
            .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
            .extract()
            .asString();

    // Then - Verify error response XML
    assertThat(
        actualXml,
        isSimilarTo(
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <status>
                  <message>Not found</message>
                  <status-code>404</status-code>
                </status>
                """)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }

  @Test
  void getPreset_shouldReturnPreset() {
    // Given - Create a preset in the database
    var now = OffsetDateTime.now().withNano(0);
    Preset preset4 =
        Preset.builder()
            .accountId("6921042")
            .deviceId("587A628A4042")
            .buttonNumber(4)
            .name("Seasonal Mix")
            .location("/playback/container/c3BvdGlmeTpwbGF5bGlzdDoyd0JCOGIzUWhDWXd5T0d2dE9id3dI")
            .sourceId("19989621")
            .containerArt("https://mosaic.scdn.co/300/mockimageurl")
            .contentItemType("tracklisturl")
            .createdOn(OffsetDateTime.parse("2018-11-26T18:47:06.000+00:00"))
            .updatedOn(OffsetDateTime.parse("2022-11-17T19:35:37.000+00:00"))
            .build();
    presetRepository.save(preset4);

    // When / Then
    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .header(
                "Authorization",
                "Bearer nRBCU6Iaiuu0MV498UmdWZv7Y1/qtwtEhLaERcp5C1jBxCDJjTS21UJItr2xw3RYSx808JkS9pOdUVGgP4FAPDd5wpT8MPVgmKtDjztBxRn1lCq6FH/riDIMW0OD9SyP")
            .when()
            .get("/streaming/account/6921042/device/587A628A4042/preset/4")
            .then()
            .statusCode(200)
            .contentType("application/vnd.bose.streaming-v1.2+xml")
            .extract()
            .body()
            .asString();

    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <preset buttonNumber="4">
          <containerArt>https://mosaic.scdn.co/300/mockimageurl</containerArt>
          <contentItemType>tracklisturl</contentItemType>
          <createdOn>2018-11-26T18:47:06.000+00:00</createdOn>
          <location>/playback/container/c3BvdGlmeTpwbGF5bGlzdDoyd0JCOGIzUWhDWXd5T0d2dE9id3dI</location>
          <name>Seasonal Mix</name>
          <source id="19989621" type="Audio">
            <createdOn>2018-08-11T09:52:31.000+00:00</createdOn>
            <credential type="token_version_3">mockToken789xyz=</credential>
            <name>mockuser789xyz</name>
            <sourceproviderid>15</sourceproviderid>
            <sourcename>user@example.com</sourcename>
            <sourceSettings/>
            <updatedOn>2018-11-26T18:42:27.000+00:00</updatedOn>
            <username>mockuser789xyz</username>
          </source>
          <updatedOn>2022-11-17T19:35:37.000+00:00</updatedOn>
          <username>mockuser789xyz</username>
        </preset>""";

    assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }

  @Test
  void getPreset_shouldReturn404WhenNotFound() {
    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "Bearer test-token")
        .when()
        .get("/streaming/account/6921042/device/587A628A4042/preset/9")
        .then()
        .statusCode(404)
        .contentType("application/vnd.bose.streaming-v1.2+xml")
        .body("status.message", equalTo("Not found"))
        .body("status.'status-code'", equalTo("404"));
  }
}
