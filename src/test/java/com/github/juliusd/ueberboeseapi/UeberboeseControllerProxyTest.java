package com.github.juliusd.ueberboeseapi;

import static com.github.juliusd.ueberboeseapi.device.Device.builder;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.github.juliusd.ueberboeseapi.preset.Preset;
import com.github.juliusd.ueberboeseapi.recent.Recent;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.time.OffsetDateTime;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.placeholder.PlaceholderDifferenceEvaluator;

class UeberboeseControllerProxyTest extends TestBase {

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
  void getFullAccount_shouldReturnMinimalAccountWhenBoseReturns401() {
    String accountId = "9999999";
    String deviceId = "DEVICE_BOSE_DOWN";

    deviceRepository.save(
        builder()
            .deviceId(deviceId)
            .name("Living Room Speaker")
            .ipAddress("192.168.1.77")
            .margeAccountId(accountId)
            .firstSeen(OffsetDateTime.parse("2025-01-01T10:00:00.000+00:00"))
            .lastSeen(OffsetDateTime.parse("2026-03-01T08:00:00.000+00:00"))
            .updatedOn(OffsetDateTime.parse("2026-03-01T08:00:00.000+00:00"))
            .build());

    recentRepository.save(
        Recent.builder()
            .id(null)
            .accountId(accountId)
            .name("My Radio Station")
            .location("/v1/playback/station/s12345")
            .sourceId("source-001")
            .contentItemType("stationurl")
            .deviceId(deviceId)
            .lastPlayedAt(OffsetDateTime.parse("2026-02-01T12:00:00.000+00:00"))
            .createdOn(OffsetDateTime.parse("2026-01-01T10:00:00.000+00:00"))
            .updatedOn(OffsetDateTime.parse("2026-02-01T12:00:00.000+00:00"))
            .version(null)
            .build());

    presetRepository.save(
        Preset.builder()
            .id(null)
            .accountId(accountId)
            .deviceId(deviceId)
            .buttonNumber(1)
            .name("My Playlist")
            .location("/playback/container/abc123")
            .sourceId("source-001")
            .containerArt("https://example.org/art.png")
            .contentItemType("tracklisturl")
            .createdOn(OffsetDateTime.parse("2026-01-01T10:00:00.000+00:00"))
            .updatedOn(OffsetDateTime.parse("2026-02-01T12:00:00.000+00:00"))
            .build());

    wireMockServer.stubFor(
        get(urlEqualTo("/streaming/account/" + accountId + "/full"))
            .willReturn(
                aResponse()
                    .withStatus(401)
                    .withHeader("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
                    .withBody(
                        "<status><message>Not Authorized</message><status-code>401</status-code></status>")));

    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .when()
            .get("/streaming/account/" + accountId + "/full")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString();

    // language=XML
    String expectedXml =
        """
       <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
       <account id="9999999">
         <accountStatus>ACTIVE</accountStatus>
         <devices>
           <device deviceid="DEVICE_BOSE_DOWN">
             <attachedProduct/>
             <createdOn>2025-01-01T10:00:00.000+00:00</createdOn>
             <firmwareVersion/>
             <ipaddress>192.168.1.77</ipaddress>
             <name>Living Room Speaker</name>
             <presets>
               <preset buttonNumber="1">
                 <containerArt>https://example.org/art.png</containerArt>
                 <contentItemType>tracklisturl</contentItemType>
                 <createdOn>2026-01-01T10:00:00.000+00:00</createdOn>
                 <location>/playback/container/abc123</location>
                 <name>My Playlist</name>
                 <source id="source-001" type="Audio">
                   <createdOn>${xmlunit.isDateTime}</createdOn>
                   <credential type="${xmlunit.ignore}">${xmlunit.ignore}</credential>
                   <name>${xmlunit.ignore}</name>
                   <sourceproviderid>${xmlunit.ignore}</sourceproviderid>
                   <sourcename>${xmlunit.ignore}</sourcename>
                   <sourceSettings/>
                   <updatedOn>${xmlunit.isDateTime}</updatedOn>
                   <username>${xmlunit.ignore}</username>
                 </source>
                 <updatedOn>2026-02-01T12:00:00.000+00:00</updatedOn>
                 <username>${xmlunit.ignore}</username>
               </preset>
             </presets>
             <recents>
               <recent id="${xmlunit.ignore}">
                 <contentItemType>stationurl</contentItemType>
                 <createdOn>2026-01-01T10:00:00.000+00:00</createdOn>
                 <lastplayedat>2026-02-01T12:00:00.000+00:00</lastplayedat>
                 <location>/v1/playback/station/s12345</location>
                 <name>My Radio Station</name>
                 <source id="source-001" type="Audio">
                   <createdOn>${xmlunit.isDateTime}</createdOn>
                   <credential type="${xmlunit.ignore}">${xmlunit.ignore}</credential>
                   <name>${xmlunit.ignore}</name>
                   <sourceproviderid>${xmlunit.ignore}</sourceproviderid>
                   <sourcename>${xmlunit.ignore}</sourcename>
                   <sourceSettings/>
                   <updatedOn>${xmlunit.isDateTime}</updatedOn>
                   <username>${xmlunit.ignore}</username>
                 </source>
                 <sourceid>source-001</sourceid>
                 <updatedOn>${xmlunit.isDateTime}</updatedOn>
               </recent>
             </recents>
             <serialNumber/>
             <updatedOn>2026-03-01T08:00:00.000+00:00</updatedOn>
           </device>
         </devices>
         <mode>global</mode>
         <preferredLanguage>en</preferredLanguage>
         <sources>
           <source id="1" type="Audio">
             <createdOn>2018-08-11T08:55:41.000+00:00</createdOn>
             <credential type="token">eyJ...</credential>
             <name/>
             <sourceproviderid>25</sourceproviderid>
             <sourcename/>
             <sourceSettings/>
             <updatedOn>2019-07-20T17:48:31.000+00:00</updatedOn>
             <username/>
           </source>
         </sources>
         <providerSettings/>
       </account>
       """;
    MatcherAssert.assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }
}
