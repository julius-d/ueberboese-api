package com.github.juliusd.ueberboeseapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.Base64;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BmxControllerTest extends TestBase {

  private static WireMockServer wireMockServer;

  @BeforeAll
  static void setupWireMock() {
    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8889));
    wireMockServer.start();
    configureFor("localhost", 8889);
  }

  @AfterAll
  static void teardownWireMock() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  @Test
  void testGetBmxServices() {
    given()
        .contentType("application/json")
        .when()
        .get("/bmx/registry/v1/services")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("bmx_services", hasSize(greaterThan(0)))
        .body("bmx_services[0].id.name", notNullValue())
        .body("bmx_services[0].baseUrl", notNullValue())
        .body("bmx_services[0].assets.name", notNullValue())
        .body("askAgainAfter", notNullValue())
        .body("bmx_services.find { it.id.name == 'TUNEIN' }.id.value", equalTo(25))
        .body("bmx_services.find { it.id.name == 'TUNEIN' }.assets.name", equalTo("TuneIn"))
        .body("bmx_services.find { it.id.name == 'LOCAL_INTERNET_RADIO' }.id.value", equalTo(11))
        .body(
            "bmx_services.find { it.id.name == 'LOCAL_INTERNET_RADIO' }.assets.name",
            equalTo("Custom Stations"));
  }

  @Test
  void testGetBmxServicesCompleteStructure() {
    given()
        .contentType("application/json")
        .when()
        .get("/bmx/registry/v1/services")
        .then()
        .statusCode(200)
        .contentType("application/json")
        // Verify top-level structure
        .body("_links", notNullValue())
        .body("_links.bmx_services_availability", notNullValue())
        .body("_links.bmx_services_availability.href", equalTo("../servicesAvailability"))
        .body("askAgainAfter", notNullValue())
        .body("bmx_services", hasSize(4))
        // Verify TuneIn service (index 0)
        .body("bmx_services[0].id.name", equalTo("TUNEIN"))
        .body("bmx_services[0].id.value", equalTo(25))
        .body("bmx_services[0].askAdapter", equalTo(false))
        .body("bmx_services[0].assets.name", equalTo("TuneIn"))
        .body("bmx_services[0].assets.color", equalTo("#000000"))
        .body("bmx_services[0].assets.description", containsString("TuneIn"))
        .body("bmx_services[0].assets.icons.largeSvg", notNullValue())
        .body("bmx_services[0].assets.icons.monochromePng", notNullValue())
        .body("bmx_services[0].assets.icons.monochromeSvg", notNullValue())
        .body("bmx_services[0].assets.icons.smallSvg", notNullValue())
        .body("bmx_services[0].assets.icons.defaultAlbumArt", notNullValue())
        .body("bmx_services[0].authenticationModel.anonymousAccount.autoCreate", equalTo(true))
        .body("bmx_services[0].authenticationModel.anonymousAccount.enabled", equalTo(true))
        .body("bmx_services[0].baseUrl", containsString("/bmx/tunein"))
        .body("bmx_services[0].streamTypes", hasItems("liveRadio", "onDemand"))
        .body("bmx_services[0]._links.bmx_navigate", notNullValue())
        .body("bmx_services[0]._links.bmx_navigate.href", equalTo("/v1/navigate"))
        .body("bmx_services[0]._links.bmx_token", notNullValue())
        .body("bmx_services[0]._links.bmx_token.href", equalTo("/v1/token"))
        .body("bmx_services[0]._links.self", notNullValue())
        .body("bmx_services[0]._links.self.href", equalTo("/"))
        // Verify Custom Stations service (index 1)
        .body("bmx_services[1].id.name", equalTo("LOCAL_INTERNET_RADIO"))
        .body("bmx_services[1].id.value", equalTo(11))
        .body("bmx_services[1].askAdapter", equalTo(false))
        .body("bmx_services[1].assets.name", equalTo("Custom Stations"))
        .body("bmx_services[1].assets.color", equalTo("#000000"))
        .body("bmx_services[1].assets.description", equalTo("Custom radio stations with BMX."))
        .body("bmx_services[1].authenticationModel.anonymousAccount.autoCreate", equalTo(true))
        .body("bmx_services[1].authenticationModel.anonymousAccount.enabled", equalTo(true))
        .body("bmx_services[1].baseUrl", containsString("/orion"))
        .body("bmx_services[1].streamTypes", hasItem("liveRadio"))
        .body("bmx_services[1]._links.bmx_token", notNullValue())
        .body("bmx_services[1]._links.bmx_token.href", equalTo("/token"))
        .body("bmx_services[1]._links.self", notNullValue())
        .body("bmx_services[1]._links.self.href", equalTo("/"))
        // Verify SiriusXM service (index 2)
        .body("bmx_services[2].id.name", equalTo("SIRIUSXM_EVEREST"))
        .body("bmx_services[2].id.value", equalTo(38))
        .body("bmx_services[2].askAdapter", equalTo(false))
        .body("bmx_services[2].assets.name", equalTo("SiriusXM"))
        .body("bmx_services[2].assets.color", equalTo("#004b85"))
        .body("bmx_services[2].assets.shortDescription", notNullValue())
        .body("bmx_services[2].authenticationModel.loginPageProvider", equalTo("BOSE"))
        .body("bmx_services[2].signupUrl", containsString("streaming.siriusxm.com"))
        .body("bmx_services[2].streamTypes", hasItems("liveRadio", "onDemand"))
        .body("bmx_services[2]._links.bmx_availability", notNullValue())
        .body("bmx_services[2]._links.bmx_availability.href", equalTo("/availability"))
        .body("bmx_services[2]._links.bmx_logout", notNullValue())
        .body("bmx_services[2]._links.bmx_logout.href", equalTo("/logout"))
        .body("bmx_services[2]._links.bmx_navigate", notNullValue())
        .body("bmx_services[2]._links.bmx_navigate.href", equalTo("/navigate/"))
        .body("bmx_services[2]._links.bmx_token", notNullValue())
        .body("bmx_services[2]._links.bmx_token.href", equalTo("/token"))
        .body("bmx_services[2]._links.self", notNullValue())
        .body("bmx_services[2]._links.self.href", equalTo("/"))
        // Verify Radioplayer service (index 3)
        .body("bmx_services[3].id.name", equalTo("RADIOPLAYER"))
        .body("bmx_services[3].id.value", equalTo(35))
        .body("bmx_services[3].askAdapter", equalTo(false))
        .body("bmx_services[3].assets.name", equalTo("Radioplayer"))
        .body("bmx_services[3].assets.color", equalTo("#cc0033"))
        .body("bmx_services[3].authenticationModel.anonymousAccount.autoCreate", equalTo(false))
        .body("bmx_services[3].authenticationModel.anonymousAccount.enabled", equalTo(true))
        .body("bmx_services[3].baseUrl", equalTo("https://boserp.radioapi.io"))
        .body("bmx_services[3].streamTypes", hasItems("liveRadio", "onDemand"))
        .body("bmx_services[3]._links.bmx_availability", notNullValue())
        .body("bmx_services[3]._links.bmx_availability.href", equalTo("/availability"))
        .body("bmx_services[3]._links.bmx_navigate", notNullValue())
        .body("bmx_services[3]._links.bmx_navigate.href", equalTo("/navigate"))
        .body("bmx_services[3]._links.bmx_token", notNullValue())
        .body(
            "bmx_services[3]._links.bmx_token.href",
            containsString("/soundtouch-msp-token-proxy/RADIOPLAYER/token"))
        .body("bmx_services[3]._links.self", notNullValue())
        .body("bmx_services[3]._links.self.href", equalTo("/"));
  }

  @Test
  void testGetTuneInPlayback() {
    // Mock TuneIn describe endpoint
    // language=XML
    String describeResponse =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <opml version="1">
          <head>
            <status>200</status>

          </head>
          <body>
            <outline type="object" text="Radio name">
              <station>
                <guide_id>s80044</guide_id>
                <preset_id>s80044</preset_id>
                <name>Radio name</name>
                <call_sign>Radio name</call_sign>
                <slogan>Have fun</slogan>
                <frequency>90.2</frequency>
                <band>FM</band>
                <url>http://www.example.org/radio</url>
                <report_url/>
                <detail_url>http://tun.in/seDU6</detail_url>
                <is_preset>false</is_preset>
                <is_available>true</is_available>
                <is_music>true</is_music>
                <has_song>false</has_song>
                <has_schedule>false</has_schedule>
                <has_topics>false</has_topics>
                <twitter_id>example_foobar_nix</twitter_id>
                <logo>https://cdn-radiotime-logos.tunein.com/s80044q.png</logo>
                <location>Berlin</location>
                <description>Das Radio.</description>
                <email>test@example.de</email>
                <phone>0331-123345</phone>
                <language>German</language>
                <genre_id>g76</genre_id>
                <genre_name>Children's Music</genre_name>
                <region_id>r100772</region_id>
                <country_region_id>100346</country_region_id>
                <tz>GMT + 1 (CEST)</tz>
                <tz_offset>60</tz_offset>
                <ad_eligible>true</ad_eligible>
                <preroll_ad_eligible>true</preroll_ad_eligible>
                <companion_ad_eligible>false</companion_ad_eligible>
                <video_preroll_ad_eligible>false</video_preroll_ad_eligible>
                <fb_share>true</fb_share>
                <twitter_share>true</twitter_share>
                <song_share>true</song_share>
                <tunein_url>http://tunein.com/station/?stationId=80044</tunein_url>
                <is_family_content>true</is_family_content>
                <is_mature_content>false</is_mature_content>
                <is_event>false</is_event>
                <content_classification>music</content_classification>
                <has_profile>true</has_profile>
                <can_cast>true</can_cast>
                <nielsen_eligible>false</nielsen_eligible>
                <use_native_player>false</use_native_player>
                <live_seek_stream>false</live_seek_stream>
                <seek_disabled>false</seek_disabled>
              </station>
            </outline>
          </body>
        </opml>""";

    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/describe.ashx?id=s80044"))
            .willReturn(aResponse().withStatus(200).withBody(describeResponse)));

    // Mock TuneIn stream URL endpoint
    String streamResponse = "https://stream.example.com/radio1\nhttps://stream.example.com/radio2";

    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/Tune.ashx?id=s80044"))
            .willReturn(aResponse().withStatus(200).withBody(streamResponse)));

    // Test the endpoint
    given()
        .contentType("application/json")
        .when()
        .get("/bmx/tunein/v1/playback/station/s80044")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("name", equalTo("Radio name"))
        .body("imageUrl", equalTo("https://cdn-radiotime-logos.tunein.com/s80044q.png"))
        .body("streamType", equalTo("liveRadio"))
        .body("audio.streamUrl", equalTo("https://stream.example.com/radio1"))
        .body("audio.streams", hasSize(2))
        .body("audio.hasPlaylist", equalTo(true))
        .body("audio.isRealtime", equalTo(true));
  }

  @Test
  void testGetCustomStreamPlayback() {
    // Create base64-encoded JSON
    String json =
        """
        {"streamUrl":"https://example.org/stream","imageUrl":"https://example.org/img.png","name":"Test Station"}
        """;
    String base64Data = Base64.getEncoder().encodeToString(json.getBytes());

    given()
        .contentType("application/json")
        .queryParam("data", base64Data)
        .when()
        .get("/core02/svc-bmx-adapter-orion/prod/orion/station")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("name", equalTo("Test Station"))
        .body("imageUrl", equalTo("https://example.org/img.png"))
        .body("streamType", equalTo("liveRadio"))
        .body("audio.streamUrl", equalTo("https://example.org/stream"))
        .body("audio.streams", hasSize(1));
  }

  @Test
  void testGetCustomStreamPlaybackWithMissingData() {
    given()
        .contentType("application/json")
        .when()
        .get("/core02/svc-bmx-adapter-orion/prod/orion/station")
        .then()
        .statusCode(400);
  }

  @Test
  void testRefreshTuneInToken() {
    given()
        .contentType("application/json")
        .body(
            """
            {
              "grant_type": "refresh_token",
              "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test"
            }
            """)
        .when()
        .post("/bmx/tunein/v1/token")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("access_token", notNullValue());
  }

  @Test
  void testReportTuneInAnalytics() {
    given()
        .auth()
        .preemptive()
        .basic("admin", "change_me!")
        .contentType("application/json")
        .queryParam("stream_id", "test123")
        .queryParam("guide_id", "s80044")
        .queryParam("listen_id", "456")
        .queryParam("stream_type", "liveRadio")
        .body(
            """
            {
              "timeStamp": "2025-10-31T05:38:55+0000",
              "eventType": "START",
              "reason": "USER_SELECT_PLAYABLE",
              "timeIntoTrack": 0,
              "playbackDelay": 6664
            }
            """)
        .when()
        .post("/bmx/tunein/v1/report")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("nextReportIn", notNullValue());
  }

  @Test
  void testReportTuneInAnalyticsWithReasonSubCode() {
    given()
        .auth()
        .preemptive()
        .basic("admin", "change_me!")
        .contentType("application/json")
        .queryParam("stream_id", "test123")
        .queryParam("guide_id", "s80044")
        .queryParam("listen_id", "456")
        .queryParam("stream_type", "liveRadio")
        .body(
            """
            {
              "timeStamp": "2025-10-31T05:38:55+0000",
              "eventType": "START",
              "reason": "USER_SELECT_PLAYABLE",
              "reasonSubCode": "NORMAL",
              "timeIntoTrack": 0,
              "playbackDelay": 6664
            }
            """)
        .when()
        .post("/bmx/tunein/v1/report")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("nextReportIn", notNullValue());
  }

  @Test
  void testReportTuneInAnalyticsWithTimedEventType() {
    given()
        .auth()
        .preemptive()
        .basic("admin", "change_me!")
        .contentType("application/json")
        .queryParam("stream_id", "e3342")
        .queryParam("guide_id", "s288368")
        .queryParam("listen_id", "1770563709735")
        .queryParam("stream_type", "liveRadio")
        .body(
            """
            {
              "timeStamp": "2026-02-08T15:52:41+0000",
              "eventType": "TIMED",
              "reason": "TIMED_REPORT",
              "timeIntoTrack": 300,
              "playbackDelay": 0
            }
            """)
        .when()
        .post("/bmx/tunein/v1/report")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("nextReportIn", notNullValue());
  }

  @Test
  void testGetBmxServicesAvailability() {
    given()
        .contentType("application/json")
        .when()
        .get("/bmx/registry/v1/servicesAvailability")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .header("X-Bmx-Adapter-Version", equalTo("master.4.40"))
        .body("services", hasSize(2))
        .body("services[0].service", equalTo("TUNEIN"))
        .body("services[0].canAdd", equalTo(true))
        .body("services[0].canRemove", equalTo(false))
        .body("services[1].service", equalTo("SIRIUSXM_EVEREST"))
        .body("services[1].canAdd", equalTo(false))
        .body("services[1].canRemove", equalTo(true));
  }
}
