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
}
