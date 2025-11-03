package com.github.juliusd.ueberboeseapi;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

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
}
