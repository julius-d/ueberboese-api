package com.github.juliusd.ueberboeseapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.xmlunit.placeholder.PlaceholderDifferenceEvaluator;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UeberboeseControllerTest {

  @LocalServerPort private int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
  }

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
          <sourceid>19989313</sourceid>
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
          <createdOn>2018-11-27T18:20:01.000+00:00</createdOn>
          <lastplayedat>2025-11-01T17:32:59.000+00:00</lastplayedat>
          <location>/v1/playback/station/s80044</location>
          <name>Radio TEDDY</name>
          <source id="19989313" type="Audio">
            <createdOn>2018-08-11T08:55:41.000+00:00</createdOn>
            <credential type="token">
              <value>eyDu=</value>
            </credential>
            <name/>
            <sourceproviderid>25</sourceproviderid>
            <sourcename/>
            <sourceSettings/>
            <updatedOn>2019-07-20T17:48:31.000+00:00</updatedOn>
            <username/>
          </source>
          <sourceid>19989313</sourceid>
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
          <createdOn>2018-11-27T18:20:01.000+00:00</createdOn>
          <lastplayedat>2025-11-09T18:00:00.000+00:00</lastplayedat>
          <location>/v1/playback/test/station</location>
          <name>Test Station</name>
          <source id="12345678" type="Audio">
            <createdOn>2018-08-11T08:55:41.000+00:00</createdOn>
            <credential type="token">
              <value>eyDu=</value>
            </credential>
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
    String requestXml =
        """
        <?xml version="1.0" encoding="UTF-8" ?>
        <recent>
          <lastplayedat>2025-11-01T17:32:59+00:00</lastplayedat>
          <sourceid>19989313</sourceid>
          <name>Radio TEDDY</name>
          <location>/v1/playback/station/s80044</location>
          <contentItemType>stationurl</contentItemType>
        </recent>""";

    // Make first request
    String firstLocation =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .header("Authorization", "Bearer test-token")
            .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
            .body(requestXml)
            .when()
            .post("/streaming/account/6921042/device/587A628A4042/recent")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");

    // Make second request
    String secondLocation =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("User-agent", "Bose_Lisa/27.0.6")
            .header("Authorization", "Bearer test-token")
            .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
            .body(requestXml)
            .when()
            .post("/streaming/account/6921042/device/587A628A4042/recent")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");

    // Verify the IDs are different
    assert !firstLocation.equals(secondLocation)
        : "Location headers should contain different IDs for separate requests";
  }
}
