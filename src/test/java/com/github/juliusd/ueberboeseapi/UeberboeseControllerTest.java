package com.github.juliusd.ueberboeseapi;

import static com.github.juliusd.ueberboeseapi.device.Device.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.github.juliusd.ueberboeseapi.preset.Preset;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xmlunit.placeholder.PlaceholderDifferenceEvaluator;

class UeberboeseControllerTest extends TestBase {

  @Test
  void getSourceProviders() {
    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <sourceProviders>
          <sourceprovider id="1">
            <createdOn>2012-09-19T12:43:00.000+00:00</createdOn>
            <name>PANDORA</name>
            <updatedOn>2012-09-19T12:43:00.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="2">
            <createdOn>2012-09-19T12:43:00.000+00:00</createdOn>
            <name>INTERNET_RADIO</name>
            <updatedOn>2012-09-19T12:43:00.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="3">
            <createdOn>2012-10-22T16:03:00.000+00:00</createdOn>
            <name>OFF</name>
            <updatedOn>2012-10-22T16:03:00.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="4">
            <createdOn>2012-10-22T16:04:00.000+00:00</createdOn>
            <name>LOCAL</name>
            <updatedOn>2012-10-22T16:04:00.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="5">
            <createdOn>2012-10-22T16:04:00.000+00:00</createdOn>
            <name>AIRPLAY</name>
            <updatedOn>2012-10-22T16:04:00.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="6">
            <createdOn>2012-10-22T16:04:00.000+00:00</createdOn>
            <name>CURRATED_RADIO</name>
            <updatedOn>2012-10-22T16:04:00.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="7">
            <createdOn>2012-10-22T16:04:00.000+00:00</createdOn>
            <name>STORED_MUSIC</name>
            <updatedOn>2012-10-22T16:04:00.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="8">
            <createdOn>2012-10-22T16:04:00.000+00:00</createdOn>
            <name>SLAVE_SOURCE</name>
            <updatedOn>2012-10-22T16:04:00.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="9">
            <createdOn>2012-10-22T16:04:00.000+00:00</createdOn>
            <name>AUX</name>
            <updatedOn>2012-10-22T16:04:00.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="10">
            <createdOn>2013-01-10T09:45:00.000+00:00</createdOn>
            <name>RECOMMENDED_INTERNET_RADIO</name>
            <updatedOn>2013-01-10T09:45:00.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="11">
            <createdOn>2013-01-10T09:45:00.000+00:00</createdOn>
            <name>LOCAL_INTERNET_RADIO</name>
            <updatedOn>2013-01-10T09:45:00.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="12">
            <createdOn>2013-01-10T09:45:00.000+00:00</createdOn>
            <name>GLOBAL_INTERNET_RADIO</name>
            <updatedOn>2013-01-10T09:45:00.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="13">
            <createdOn>2014-03-17T15:30:07.000+00:00</createdOn>
            <name>HELLO</name>
            <updatedOn>2014-03-17T15:30:07.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="14">
            <createdOn>2014-03-17T15:30:27.000+00:00</createdOn>
            <name>DEEZER</name>
            <updatedOn>2014-03-17T15:30:27.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="15">
            <createdOn>2014-03-17T15:30:27.000+00:00</createdOn>
            <name>SPOTIFY</name>
            <updatedOn>2014-03-17T15:30:27.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="16">
            <createdOn>2014-03-17T15:30:27.000+00:00</createdOn>
            <name>IHEART</name>
            <updatedOn>2014-03-17T15:30:27.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="17">
            <createdOn>2014-12-04T19:49:55.000+00:00</createdOn>
            <name>SIRIUSXM</name>
            <updatedOn>2014-12-04T19:49:55.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="18">
            <createdOn>2014-12-04T19:49:55.000+00:00</createdOn>
            <name>GOOGLE_PLAY_MUSIC</name>
            <updatedOn>2014-12-04T19:49:55.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="19">
            <createdOn>2014-12-04T19:49:55.000+00:00</createdOn>
            <name>QQMUSIC</name>
            <updatedOn>2014-12-04T19:49:55.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="20">
            <createdOn>2014-12-04T19:49:55.000+00:00</createdOn>
            <name>AMAZON</name>
            <updatedOn>2014-12-04T19:49:55.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="21">
            <createdOn>2015-07-13T12:00:00.000+00:00</createdOn>
            <name>LOCAL_MUSIC</name>
            <updatedOn>2015-07-13T12:00:00.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="22">
            <createdOn>2016-04-08T17:27:21.000+00:00</createdOn>
            <name>WBMX</name>
            <updatedOn>2016-04-08T17:27:21.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="23">
            <createdOn>2016-04-08T17:27:21.000+00:00</createdOn>
            <name>SOUNDCLOUD</name>
            <updatedOn>2016-04-08T17:27:21.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="24">
            <createdOn>2016-04-08T17:27:21.000+00:00</createdOn>
            <name>TIDAL</name>
            <updatedOn>2016-04-08T17:27:21.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="25">
            <createdOn>2016-04-08T17:27:21.000+00:00</createdOn>
            <name>TUNEIN</name>
            <updatedOn>2016-04-08T17:27:21.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="26">
            <createdOn>2016-06-17T18:00:54.000+00:00</createdOn>
            <name>QPLAY</name>
            <updatedOn>2016-06-17T18:00:54.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="27">
            <createdOn>2016-08-01T13:53:40.000+00:00</createdOn>
            <name>JUKE</name>
            <updatedOn>2016-08-01T13:53:40.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="28">
            <createdOn>2016-08-01T13:53:40.000+00:00</createdOn>
            <name>BBC</name>
            <updatedOn>2016-08-01T13:53:40.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="29">
            <createdOn>2016-08-01T13:53:40.000+00:00</createdOn>
            <name>DARFM</name>
            <updatedOn>2016-08-01T13:53:40.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="30">
            <createdOn>2016-08-01T13:53:40.000+00:00</createdOn>
            <name>7DIGITAL</name>
            <updatedOn>2016-08-01T13:53:40.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="31">
            <createdOn>2016-08-01T13:53:40.000+00:00</createdOn>
            <name>SAAVN</name>
            <updatedOn>2016-08-01T13:53:40.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="32">
            <createdOn>2016-08-01T13:53:40.000+00:00</createdOn>
            <name>RDIO</name>
            <updatedOn>2016-08-01T13:53:40.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="33">
            <createdOn>2016-10-26T14:42:49.000+00:00</createdOn>
            <name>PHONE_MUSIC</name>
            <updatedOn>2016-10-26T14:42:49.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="34">
            <createdOn>2017-12-04T19:18:47.000+00:00</createdOn>
            <name>ALEXA</name>
            <updatedOn>2017-12-04T19:18:47.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="35">
            <createdOn>2019-05-28T18:21:20.000+00:00</createdOn>
            <name>RADIOPLAYER</name>
            <updatedOn>2019-05-28T18:21:20.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="36">
            <createdOn>2019-05-28T18:21:41.000+00:00</createdOn>
            <name>RADIO.COM</name>
            <updatedOn>2019-05-28T18:21:41.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="37">
            <createdOn>2019-06-13T17:30:47.000+00:00</createdOn>
            <name>RADIO_COM</name>
            <updatedOn>2019-06-13T17:30:47.000+00:00</updatedOn>
          </sourceprovider>
          <sourceprovider id="38">
            <createdOn>2019-11-25T18:00:33.000+00:00</createdOn>
            <name>SIRIUSXM_EVEREST</name>
            <updatedOn>2019-11-25T18:00:33.000+00:00</updatedOn>
          </sourceprovider>
        </sourceProviders>""";

    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .when()
        .get("/streaming/sourceproviders")
        .then()
        .statusCode(200)
        .contentType("application/vnd.bose.streaming-v1.2+xml")
        .body(isSimilarTo(expectedXml).ignoreWhitespace());
  }

  @Test
  void addRecentItem_shouldCreateNewRecentItem() {
    // language=XML
    String requestXml =
        """
        <?xml version="1.0" encoding="UTF-8" ?>
        <recent>
          <lastplayedat>2025-11-01T17:32:59+00:00</lastplayedat>
          <sourceid>19989342</sourceid>
          <name>Radio TEDDY</name>
          <location>/v1/playback/station/s80044</location>
          <contentItemType>stationurl</contentItemType>
        </recent>""";

    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "Bearer foo/bar/blob")
        .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
        .body(requestXml)
        .when()
        .post("/streaming/account/6921042/device/587A628A4042/recent")
        .then()
        .statusCode(201)
        .contentType("application/vnd.bose.streaming-v1.2+xml")
        .header(
            "Location",
            containsString(
                "http://streamingqa.bose.com/account/6921042/device/587A628A4042/recent/"));

    // Extract the response for XML comparison
    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .header("Authorization", "Bearer foo/bar/blob")
            .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
            .body(requestXml)
            .when()
            .post("/streaming/account/6921042/device/587A628A4042/recent")
            .then()
            .statusCode(201)
            .extract()
            .body()
            .asString();

    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <recent id="${xmlunit.isNumber}">
          <contentItemType>stationurl</contentItemType>
          <createdOn>${xmlunit.isDateTime}</createdOn>
          <lastplayedat>2025-11-01T17:32:59.000+00:00</lastplayedat>
          <location>/v1/playback/station/s80044</location>
          <name>Radio TEDDY</name>
          <source id="19989342" type="Audio">
            <createdOn>${xmlunit.isDateTime}</createdOn>
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
        </recent>""";

    assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }

  @Test
  void addRecentItem_shouldHandleDifferentRequestData() {
    // language=XML
    String requestXml =
        """
        <?xml version="1.0" encoding="UTF-8" ?>
        <recent>
          <lastplayedat>2025-11-09T18:00:00+00:00</lastplayedat>
          <sourceid>12345678</sourceid>
          <name>Test Station</name>
          <location>/v1/playback/test/station</location>
          <contentItemType>testurl</contentItemType>
        </recent>""";

    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "Bearer test-token")
        .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
        .body(requestXml)
        .when()
        .post("/streaming/account/1234567/device/TESTDEVICE/recent")
        .then()
        .statusCode(201)
        .contentType("application/vnd.bose.streaming-v1.2+xml")
        .header(
            "Location",
            containsString(
                "http://streamingqa.bose.com/account/1234567/device/TESTDEVICE/recent/"));

    // Extract the response for XML comparison
    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .header("Authorization", "Bearer test-token")
            .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
            .body(requestXml)
            .when()
            .post("/streaming/account/1234567/device/TESTDEVICE/recent")
            .then()
            .statusCode(201)
            .extract()
            .body()
            .asString();

    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <recent id="${xmlunit.isNumber}">
          <contentItemType>testurl</contentItemType>
          <createdOn>${xmlunit.isDateTime}</createdOn>
          <lastplayedat>2025-11-09T18:00:00.000+00:00</lastplayedat>
          <location>/v1/playback/test/station</location>
          <name>Test Station</name>
          <source id="12345678" type="Audio">
            <createdOn>2018-08-11T08:55:28.000+00:00</createdOn>
            <credential type="token">eyDu=</credential>
            <name/>
            <sourceproviderid>25</sourceproviderid>
            <sourcename/>
            <sourceSettings/>
            <updatedOn>2019-07-20T17:48:31.000+00:00</updatedOn>
            <username/>
          </source>
          <sourceid>12345678</sourceid>
          <updatedOn>${xmlunit.isDateTime}</updatedOn>
        </recent>""";

    assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }

  @Test
  void addRecentItem_shouldValidateRequestBody() {
    // Test with invalid XML (malformed XML body)
    String invalidRequestXml = "invalid xml content";

    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "Bearer test-token")
        .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
        .body(invalidRequestXml)
        .when()
        .post("/streaming/account/6921042/device/587A628A4042/recent")
        .then()
        .statusCode(400);
  }

  @Test
  void addRecentItem_shouldReturnUniqueIds() {
    // language=XML
    String firstRequestXml =
        """
        <?xml version="1.0" encoding="UTF-8" ?>
        <recent>
          <lastplayedat>2025-11-01T17:32:59+00:00</lastplayedat>
          <sourceid>19989342</sourceid>
          <name>Radio TEDDY</name>
          <location>/v1/playback/station/s80044</location>
          <contentItemType>stationurl</contentItemType>
        </recent>""";

    // language=XML
    String secondRequestXml =
        """
        <?xml version="1.0" encoding="UTF-8" ?>
        <recent>
          <lastplayedat>2025-11-01T17:32:59+00:00</lastplayedat>
          <sourceid>19989314</sourceid>
          <name>Radio TEDDY2</name>
          <location>/v1/playback/station/s80045</location>
          <contentItemType>stationurl</contentItemType>
        </recent>""";

    // Make first request
    String firstLocation =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .header("Authorization", "Bearer test-token")
            .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
            .body(firstRequestXml)
            .when()
            .post("/streaming/account/6921042/device/587A628A4042/recent")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");

    // Make second request with different location+sourceId
    String secondLocation =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .header("Authorization", "Bearer test-token")
            .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
            .body(secondRequestXml)
            .when()
            .post("/streaming/account/6921042/device/587A628A4042/recent")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");

    Assertions.assertThat(firstLocation)
        .isNotEqualTo(secondLocation)
        .describedAs("Location headers should contain different IDs for separate requests");
  }

  @Test
  void getRecents_shouldReturnRecentsList() {
    givenRecentsInDB();

    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/streaming/account/6921042/device/587A628A4042/recents")
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
       <recents>
         <recent id="${xmlunit.isNumber}">
           <contentItemType>tracklisturl</contentItemType>
           <createdOn>2025-12-13T17:14:28.000+00:00</createdOn>
           <lastplayedat>${xmlunit.isDateTime}</lastplayedat>
           <location>/playback/container/c3BvdGlmeTphbGJ1bTowZ3BGWVZNbVV6VkVxeVAyeUh3cEha</location>
           <name>Ghostsitter 42 - Das Haus im Moor</name>
           <source id="19989643" type="Audio">
             <createdOn>2018-08-11T08:55:28.000+00:00</createdOn>
             <credential type="token_version_3">token-User1-Spot</credential>
             <name>user1namespot</name>
             <sourceproviderid>15</sourceproviderid>
             <sourcename>user1@example.org</sourcename>
             <sourceSettings/>
             <updatedOn>2018-08-11T08:55:28.000+00:00</updatedOn>
             <username>user1namespot</username>
           </source>
           <sourceid>19989643</sourceid>
           <updatedOn>${xmlunit.isDateTime}</updatedOn>
         </recent>
         <recent id="${xmlunit.isNumber}">
           <contentItemType>stationurl</contentItemType>
           <createdOn>2018-11-27T18:20:01.000+00:00</createdOn>
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
             <updatedOn>2018-08-11T08:55:28.000+00:00</updatedOn>
             <username/>
           </source>
           <sourceid>19989342</sourceid>
           <updatedOn>${xmlunit.isDateTime}</updatedOn>
         </recent>
       </recents>""";

    assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }

  @Test
  void getPresets_shouldReturnPresetsFromCachedData() {
    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
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
              <name/>
              <sourceproviderid>25</sourceproviderid>
              <sourcename/>
              <sourceSettings/>
              <updatedOn>2019-07-20T17:48:31.000+00:00</updatedOn>
              <username/>
            </source>
            <updatedOn>2018-11-26T18:40:45.000+00:00</updatedOn>
            <username>Radio Foobar</username>
          </preset>
          <preset buttonNumber="2">
            <containerArt>https://example.org/300/longtext1</containerArt>
            <contentItemType>tracklisturl</contentItemType>
            <createdOn>2018-11-26T18:40:45.000+00:00</createdOn>
            <location>/playback/container/131311232312121212</location>
            <name>Radio Foobar</name>
            <source id="19989643" type="Audio">
              <createdOn>${xmlunit.isDateTime}</createdOn>
              <credential type="token_version_3">token-User1-Spot</credential>
              <name>user1namespot</name>
              <sourceproviderid>15</sourceproviderid>
              <sourcename>user1@example.org</sourcename>
              <sourceSettings/>
              <updatedOn>2019-07-20T17:48:31.000+00:00</updatedOn>
              <username>user1namespot</username>
            </source>
            <updatedOn>2018-11-26T18:40:45.000+00:00</updatedOn>
            <username>Radio Bar</username>
          </preset>
          <preset buttonNumber="3">
            <containerArt>https://example.org/s25111/images/logoq.jpg?t=1</containerArt>
            <contentItemType>stationurl</contentItemType>
            <createdOn>2018-11-26T18:40:45.000+00:00</createdOn>
            <location>/v1/playback/station/s25111</location>
            <name>radioonetwo</name>
            <source id="19989342" type="Audio">
              <createdOn>${xmlunit.isDateTime}</createdOn>
              <credential type="token">eyJduTune=</credential>
              <name/>
              <sourceproviderid>25</sourceproviderid>
              <sourcename/>
              <sourceSettings/>
              <updatedOn>2019-07-20T17:48:31.000+00:00</updatedOn>
              <username/>
            </source>
            <updatedOn>2018-11-26T18:40:45.000+00:00</updatedOn>
            <username>radioeins vom rbb</username>
          </preset>
          <preset buttonNumber="4">
            <containerArt>https://example.org/300/longtext2</containerArt>
            <contentItemType>tracklisturl</contentItemType>
            <createdOn>2018-11-26T18:40:45.000+00:00</createdOn>
            <location>/playback/container/45345349538</location>
            <name>Kids</name>
            <source id="19989643" type="Audio">
              <createdOn>${xmlunit.isDateTime}</createdOn>
              <credential type="token_version_3">token-User1-Spot</credential>
              <name>user1namespot</name>
              <sourceproviderid>15</sourceproviderid>
              <sourcename>user1@example.org</sourcename>
              <sourceSettings/>
              <updatedOn>2019-07-20T17:48:31.000+00:00</updatedOn>
              <username>user1namespot</username>
            </source>
            <updatedOn>2018-11-26T18:40:45.000+00:00</updatedOn>
            <username>Kids</username>
          </preset>
          <preset buttonNumber="5">
            <containerArt/>
            <contentItemType>tracklisturl</contentItemType>
            <createdOn>2018-11-26T18:40:45.000+00:00</createdOn>
            <location>/playback/container/urlssfdfsd</location>
            <name>Artist Radio</name>
            <source id="20260226" type="Audio">
              <createdOn>${xmlunit.isDateTime}</createdOn>
              <credential type="token_version_3">token-User2-Spot</credential>
              <name>user2namespot</name>
              <sourceproviderid>15</sourceproviderid>
              <sourcename>user2@example.org</sourcename>
              <sourceSettings/>
              <updatedOn>2019-07-20T17:48:31.000+00:00</updatedOn>
              <username>user2namespot</username>
            </source>
            <updatedOn>2018-11-26T18:40:45.000+00:00</updatedOn>
            <username>Artist Radio</username>
          </preset>
          <preset buttonNumber="6">
            <containerArt>https://example.org/image/ab67706c0000da843b38733ef58fbd3530776a42</containerArt>
            <contentItemType>tracklisturl</contentItemType>
            <createdOn>2018-11-26T18:40:45.000+00:00</createdOn>
            <location>/playback/container/577667532256ht</location>
            <name>Komplett Entspannt</name>
            <source id="19989643" type="Audio">
              <createdOn>${xmlunit.isDateTime}</createdOn>
              <credential type="token_version_3">token-User1-Spot</credential>
              <name>user1namespot</name>
              <sourceproviderid>15</sourceproviderid>
              <sourcename>user1@example.org</sourcename>
              <sourceSettings/>
              <updatedOn>2019-07-20T17:48:31.000+00:00</updatedOn>
              <username>user1namespot</username>
            </source>
            <updatedOn>2018-11-26T18:40:45.000+00:00</updatedOn>
            <username>Komplett Entspannt</username>
          </preset>
        </presets>""";

    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/streaming/account/6921042/device/123980WER/presets")
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
  void powerOnSupport_shouldAcceptValidRequest() {
    // language=XML
    String requestXml =
        """
        <?xml version="1.0" encoding="UTF-8" ?>
        <device-data>
          <device id="587A628A4042">
            <serialnumber>P12343567890</serialnumber>
            <firmware-version>27.0.6.46330.5043500 epdbuild.trunk.hepdswbld04.2022-08-04T11:20:29</firmware-version>
            <product product_code="SoundTouch 10 sm2" type="5">
              <serialnumber>06123456789AE</serialnumber>
            </product>
          </device>
          <diagnostic-data>
            <device-landscape>
              <rssi>Good</rssi>
              <gateway-ip-address>192.168.123.1</gateway-ip-address>
              <macaddresses>
                <macaddress>587A628A4042</macaddress>
                <macaddress>38AA32BAB0EA</macaddress>
              </macaddresses>
              <ip-address>192.168.178.42</ip-address>
              <network-connection-type>Wireless</network-connection-type>
            </device-landscape>
            <network-landscape>
              <network-data xmlns="http://www.Bose.com/Schemas/2012-12/NetworkMonitor/" />
            </network-landscape>
          </diagnostic-data>
        </device-data>""";

    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "Bearer test-token")
        .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
        .body(requestXml)
        .when()
        .post("/streaming/support/power_on")
        .then()
        .statusCode(200)
        .contentType("application/vnd.bose.streaming-v1.2+xml");
  }

  @Test
  void powerOnSupport_shouldReturn400WhenDeviceIdMissing() {
    // language=XML - Missing device id attribute
    String requestXml =
        """
        <?xml version="1.0" encoding="UTF-8" ?>
        <device-data>
          <device>
            <serialnumber>P12343567890</serialnumber>
          </device>
          <diagnostic-data>
            <device-landscape>
              <ip-address>192.168.178.26</ip-address>
            </device-landscape>
          </diagnostic-data>
        </device-data>""";

    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "Bearer test-token")
        .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
        .body(requestXml)
        .when()
        .post("/streaming/support/power_on")
        .then()
        .statusCode(400);
  }

  @Test
  void getRecentItem_shouldReturnSingleRecent() {
    givenRecentsInDB();

    // Get all recents to extract a valid recentId
    String recentsXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/streaming/account/6921042/device/587A628A4042/recents")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString();

    // Extract the first recent ID from the response
    String recentId = recentsXml.substring(recentsXml.indexOf("id=\"") + 4);
    recentId = recentId.substring(0, recentId.indexOf("\""));

    // Now get the specific recent item
    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/streaming/account/6921042/device/587A628A4042/recent/" + recentId)
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
        <recent id="${xmlunit.isNumber}">
          <contentItemType>tracklisturl</contentItemType>
          <createdOn>2025-12-13T17:14:28.000+00:00</createdOn>
          <lastplayedat>${xmlunit.isDateTime}</lastplayedat>
          <location>/playback/container/c3BvdGlmeTphbGJ1bTowZ3BGWVZNbVV6VkVxeVAyeUh3cEha</location>
          <name>Ghostsitter 42 - Das Haus im Moor</name>
          <source id="19989643" type="Audio">
            <createdOn>${xmlunit.isDateTime}</createdOn>
            <credential type="token_version_3">token-User1-Spot</credential>
            <name>user1namespot</name>
            <sourceproviderid>15</sourceproviderid>
            <sourcename>user1@example.org</sourcename>
            <sourceSettings/>
            <updatedOn>${xmlunit.isDateTime}</updatedOn>
            <username>user1namespot</username>
          </source>
          <sourceid>19989643</sourceid>
          <updatedOn>${xmlunit.isDateTime}</updatedOn>
        </recent>""";

    assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }

  @Test
  void getRecentItem_shouldReturn404WhenNotFound() {
    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "Bearer test-token")
        .when()
        .get("/streaming/account/6921042/device/587A628A4042/recent/999999999")
        .then()
        .statusCode(404)
        .contentType("application/vnd.bose.streaming-v1.2+xml");
  }

  @Test
  void powerOnSupport_shouldReturn400WhenIpAddressMissing() {
    // language=XML - Missing ip-address element
    String requestXml =
        """
        <?xml version="1.0" encoding="UTF-8" ?>
        <device-data>
          <device id="587A628A4042">
            <serialnumber>P12343567890</serialnumber>
          </device>
          <diagnostic-data>
            <device-landscape>
              <rssi>Good</rssi>
            </device-landscape>
          </diagnostic-data>
        </device-data>""";

    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "Bearer test-token")
        .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
        .body(requestXml)
        .when()
        .post("/streaming/support/power_on")
        .then()
        .statusCode(400);
  }

  @Test
  void getPresets_shouldMergeDatabasePresetsWithXml() {
    // Given - DB presets that override some XML presets (presetRepository is injected via TestBase)
    // Add DB preset for button 1 (should override XML button 1)
    var now = OffsetDateTime.now().withNano(0);
    Preset preset1 =
        Preset.builder()
            .accountId("6921042")
            .deviceId("123980WER")
            .buttonNumber(1)
            .name("DB Override Preset 1")
            .location("/db/location/1")
            .sourceId("19989342")
            .containerArt("https://db.example.org/art1.png")
            .contentItemType("stationurl")
            .createdOn(now)
            .updatedOn(now)
            .build();
    presetRepository.save(preset1);

    // Add DB preset for button 7 (new preset, not in XML)
    Preset preset7 =
        Preset.builder()
            .accountId("6921042")
            .deviceId("123980WER")
            .buttonNumber(7)
            .name("DB New Preset 7")
            .location("/db/location/7")
            .sourceId("19989643")
            .containerArt("https://db.example.org/art7.png")
            .contentItemType("tracklisturl")
            .createdOn(now)
            .updatedOn(now)
            .build();
    presetRepository.save(preset7);

    // When / Then
    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .when()
        .get("/streaming/account/6921042/device/123980WER/presets")
        .then()
        .statusCode(200)
        .contentType("application/vnd.bose.streaming-v1.2+xml")
        // Verify DB preset 1 overrides XML preset 1
        .body(
            "presets.preset.find { it.@buttonNumber == '1' }.name", equalTo("DB Override Preset 1"))
        .body("presets.preset.find { it.@buttonNumber == '1' }.location", equalTo("/db/location/1"))
        .body(
            "presets.preset.find { it.@buttonNumber == '1' }.containerArt",
            equalTo("https://db.example.org/art1.png"))
        .body(
            "presets.preset.find { it.@buttonNumber == '1' }.contentItemType",
            equalTo("stationurl"))
        // Verify DB preset 7 is added (new preset not in XML)
        .body("presets.preset.find { it.@buttonNumber == '7' }.name", equalTo("DB New Preset 7"))
        .body("presets.preset.find { it.@buttonNumber == '7' }.location", equalTo("/db/location/7"))
        .body(
            "presets.preset.find { it.@buttonNumber == '7' }.containerArt",
            equalTo("https://db.example.org/art7.png"))
        // Verify XML preset 2 still exists (not overridden)
        .body("presets.preset.find { it.@buttonNumber == '2' }.name", equalTo("Radio Foobar"))
        .body(
            "presets.preset.find { it.@buttonNumber == '2' }.location",
            equalTo("/playback/container/131311232312121212"))
        // Verify all expected button numbers exist (1-7)
        .body("presets.preset.@buttonNumber", hasItems("1", "2", "3", "4", "5", "6", "7"))
        // Verify total count: 6 from XML + 1 new from DB (button 1 was overridden, not added)
        .body("presets.preset.size()", equalTo(7));
  }

  @Test
  void removeDevice_success() {
    var device =
        builder()
            .deviceId("587A628A4042")
            .name("Test Device")
            .ipAddress("192.168.1.100")
            .margeAccountId("6921042")
            .firstSeen(OffsetDateTime.parse("2018-08-11T08:55:25.000+00:00"))
            .lastSeen(OffsetDateTime.parse("2025-01-01T10:00:00.000+00:00"))
            .build();
    deviceRepository.save(device);

    // When: DELETE request is made to remove the device
    given()
        .header("Authorization", "Bearer mockToken123")
        .when()
        .delete("/streaming/account/6921042/device/587A628A4042")
        .then()
        .statusCode(200)
        .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
        .header("Location", "http://streamingqa.bose.com/account/6921042/device/587A628A4042")
        .header("METHOD_NAME", "removeDevice");

    // Then: Verify the device's margeAccountId was set to UN_PAIRED
    var updatedDevice = deviceRepository.findById("587A628A4042");
    Assertions.assertThat(updatedDevice).isPresent();
    Assertions.assertThat(updatedDevice.get().margeAccountId()).isEqualTo("UN_PAIRED");
  }

  @Test
  void removeDevice_deviceDoesNotExist() {
    String actualXml =
        given()
            .header("Authorization", "Bearer mockToken123")
            .when()
            .delete("/streaming/account/6921042/device/587A628A4042")
            .then()
            .statusCode(400)
            .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
            .extract()
            .asString();

    assertThat(
        actualXml,
        isSimilarTo(
                """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <status>
          <message>Device does not exist </message>
          <status-code>4012</status-code>
        </status>
        """)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }

  @Test
  void removeDevice_alreadyUnpaired() {
    var device =
        builder()
            .deviceId("587A628A4042")
            .name("Test Device")
            .ipAddress("192.168.1.100")
            .margeAccountId("UN_PAIRED")
            .firstSeen(OffsetDateTime.parse("2018-08-11T08:55:25.000+00:00"))
            .lastSeen(OffsetDateTime.parse("2025-01-01T10:00:00.000+00:00"))
            .build();
    deviceRepository.save(device);

    String actualXml =
        given()
            .header("Authorization", "Bearer mockToken123")
            .when()
            .delete("/streaming/account/6921042/device/587A628A4042")
            .then()
            .statusCode(400)
            .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
            .extract()
            .asString();

    assertThat(
        actualXml,
        isSimilarTo(
                """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <status>
          <message>Device does not exist </message>
          <status-code>4012</status-code>
        </status>
        """)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
    var untouchedDevice = deviceRepository.findById("587A628A4042");
    Assertions.assertThat(untouchedDevice).isPresent();
    Assertions.assertThat(untouchedDevice.get().margeAccountId()).isEqualTo("UN_PAIRED");
  }

  @Test
  void addDevice_success() {
    String actualXml =
        given()
            .header("Authorization", "Bearer mockToken123")
            .contentType("application/vnd.bose.streaming-v1.2+xml")
            .body(
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <device deviceid="587A628A4042">
                  <name>Kitchen</name>
                  <macaddress>587A628A4042</macaddress>
                </device>
                """)
            .when()
            .post("/streaming/account/6921042/device/")
            .then()
            .statusCode(201)
            .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
            .header("Location", "http://streamingqa.bose.com/account/6921042/device/587A628A4042")
            .header("METHOD_NAME", "addDevice")
            .header("Credentials", containsString("Bearer"))
            .extract()
            .asString();

    assertThat(
        actualXml,
        isSimilarTo(
                """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <device deviceid="587A628A4042">
          <createdOn>${xmlunit.ignore}</createdOn>
          <ipaddress></ipaddress>
          <name>Kitchen</name>
          <updatedOn>${xmlunit.ignore}</updatedOn>
        </device>
        """)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));

    // Verify device exists in database with correct margeAccountId
    var device = deviceRepository.findById("587A628A4042");
    Assertions.assertThat(device).isPresent();
    Assertions.assertThat(device.get().margeAccountId()).isEqualTo("6921042");
    Assertions.assertThat(device.get().name()).isEqualTo("Kitchen");
    Assertions.assertThat(device.get().ipAddress()).isEmpty();
  }

  @Test
  void getFullAccount_shouldIncludeNewlyPairedDevice() {
    // Given: a device paired to account 6921042 that is NOT in the XML cache
    var now = OffsetDateTime.parse("2026-03-03T10:16:30+01:00");
    var newDevice =
        builder()
            .deviceId("NEW_DEVICE_001")
            .name("New Speaker")
            .ipAddress("192.168.1.50")
            .margeAccountId("6921042")
            .firstSeen(now.minusDays(1))
            .lastSeen(now)
            .updatedOn(now)
            .build();
    deviceRepository.save(newDevice);

    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .when()
        .get("/streaming/account/6921042/full")
        .then()
        .statusCode(200)
        // New device should be present
        .body("account.devices.device.@deviceid", hasItems("NEW_DEVICE_001"))
        // Existing devices from the XML cache should still be present
        .body("account.devices.device.@deviceid", hasItems("123980WER", "42342FF23"));
  }

  @Test
  void getFullAccount_shouldNotDuplicateDevicesAlreadyInCache() {
    // Given: a device whose deviceId already exists in the XML cache (123980WER)
    var now = OffsetDateTime.now().withNano(0);
    var existingDevice =
        builder()
            .deviceId("123980WER")
            .name("Foobar DB")
            .ipAddress("10.0.0.1")
            .margeAccountId("6921042")
            .firstSeen(now.minusDays(5))
            .lastSeen(now)
            .updatedOn(now)
            .build();
    deviceRepository.save(existingDevice);

    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("User-agent", "Bose_Lisa/27.0.6")
        .when()
        .get("/streaming/account/6921042/full")
        .then()
        .statusCode(200)
        // "123980WER" should appear exactly once
        .body("account.devices.device.findAll { it.@deviceid == '123980WER' }.size()", equalTo(1));
  }

  @Test
  void addDevice_existingDevice() {
    // Given: A device already exists in DB with a different account
    var now = OffsetDateTime.now().withNano(0);
    var existingDevice =
        builder()
            .deviceId("587A628A4042")
            .name("Old Name")
            .ipAddress("192.168.1.100")
            .margeAccountId("1234567")
            .firstSeen(now.minusDays(10))
            .lastSeen(now.minusHours(1))
            .updatedOn(now.minusHours(1))
            .build();
    deviceRepository.save(existingDevice);

    // language=XML
    String requestBody =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <device deviceid="587A628A4042">
          <name>Kitchen</name>
          <macaddress>587A628A4042</macaddress>
        </device>
        """;

    String actualXml =
        given()
            .header("Authorization", "Bearer mockToken123")
            .contentType("application/vnd.bose.streaming-v1.2+xml")
            .body(requestBody)
            .when()
            .post("/streaming/account/6921042/device/")
            .then()
            .statusCode(201)
            .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
            .header("Location", "http://streamingqa.bose.com/account/6921042/device/587A628A4042")
            .header("METHOD_NAME", "addDevice")
            .header("Credentials", containsString("Bearer"))
            .extract()
            .asString();

    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <device deviceid="587A628A4042">
          <createdOn>${xmlunit.ignore}</createdOn>
          <ipaddress>192.168.1.100</ipaddress>
          <name>Kitchen</name>
          <updatedOn>${xmlunit.ignore}</updatedOn>
        </device>
        """;

    assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));

    // Verify margeAccountId is updated in database
    var device = deviceRepository.findById("587A628A4042");
    Assertions.assertThat(device).isPresent();
    Assertions.assertThat(device.get().margeAccountId()).isEqualTo("6921042");
    Assertions.assertThat(device.get().name()).isEqualTo("Kitchen");
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

    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
        .body(requestXml)
        .when()
        .put("/streaming/account/testaccount/device/testdevice/preset/3")
        .then()
        .statusCode(200);

    Optional<Preset> saved =
        presetRepository.findByAccountIdAndDeviceIdAndButtonNumber("testaccount", "testdevice", 3);
    Assertions.assertThat(saved).isPresent();
    Assertions.assertThat(saved.get().name()).isEqualTo("My Playlist");
    Assertions.assertThat(saved.get().location()).isEqualTo("/playback/container/test123");
    Assertions.assertThat(saved.get().sourceId()).isEqualTo("19989643");
    Assertions.assertThat(saved.get().containerArt()).isEqualTo("https://example.org/art123.png");
  }

  @Test
  void updatePreset_shouldUpsertExisting() {
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

    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
        .body(requestXml)
        .when()
        .put("/streaming/account/testaccount2/device/testdevice2/preset/1")
        .then()
        .statusCode(200);

    Optional<Preset> updated =
        presetRepository.findByAccountIdAndDeviceIdAndButtonNumber(
            "testaccount2", "testdevice2", 1);
    Assertions.assertThat(updated).isPresent();
    Assertions.assertThat(updated.get().id()).isEqualTo(existingId);
    Assertions.assertThat(updated.get().name()).isEqualTo("Updated Name");
    Assertions.assertThat(updated.get().location()).isEqualTo("/new/location");
    Assertions.assertThat(updated.get().sourceId()).isEqualTo("new-source");
  }

  @Test
  void updateDevice_shouldUpdateDeviceNameSuccessfully() {
    var existingDevice =
        com.github.juliusd.ueberboeseapi.device.Device.builder()
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

    com.github.juliusd.ueberboeseapi.device.Device updatedDevice =
        deviceRepository.findById("587A628A4042").orElseThrow();
    Assertions.assertThat(updatedDevice.name()).isEqualTo("Test Device");
  }

  @Test
  void updateDevice_shouldCreateNewDeviceWhenNotExists() {
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

    com.github.juliusd.ueberboeseapi.device.Device newDevice =
        deviceRepository.findById("NEW_DEVICE_123").orElseThrow();
    Assertions.assertThat(newDevice.name()).isEqualTo("New Device");
    Assertions.assertThat(newDevice.ipAddress()).isNull();
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

    given()
        .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
        .header("Authorization", "Bearer mockBearerTokenABC123xyz=")
        .when()
        .delete("/streaming/account/6921042/device/587A628A4042/preset/1")
        .then()
        .statusCode(200)
        .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml");

    Optional<Preset> deleted =
        presetRepository.findByAccountIdAndDeviceIdAndButtonNumber("6921042", "587A628A4042", 1);
    Assertions.assertThat(deleted).isEmpty();
  }

  @Test
  void deletePreset_shouldReturn404WhenPresetNotFound() {
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

  @Test
  void getDeviceBlacklist_shouldReturn404() {
    given()
        .header("Accept", "text/xml")
        .header("User-Agent", "Bose_Lisa/27.0.6")
        .header("Authorization", "Bearer mockToken123")
        .when()
        .get("/v1/blacklist/587A628A4042")
        .then()
        .statusCode(404);
  }
}
