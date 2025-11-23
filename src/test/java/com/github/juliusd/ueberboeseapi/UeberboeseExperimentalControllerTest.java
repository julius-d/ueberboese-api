package com.github.juliusd.ueberboeseapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import org.junit.jupiter.api.Test;
import org.xmlunit.placeholder.PlaceholderDifferenceEvaluator;

class UeberboeseExperimentalControllerTest extends TestBase {

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
                   <credential type="token_version_3">
                     token-User1-Spot
                   </credential>
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
                   <credential type="token_version_3">
                     token-User1-Spot
                   </credential>
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
                   <credential type="token_version_3">
                     token-User1-Spot
                   </credential>
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
               <recent id="123">
                 <contentItemType>tracklisturl</contentItemType>
                 <createdOn>2025-11-09T11:40:37.000+00:00</createdOn>
                 <lastplayedat>2025-11-09T19:42:09.000+00:00</lastplayedat>
                 <location>/playback/container/35546f3</location>
                 <name>A Name</name>
                 <source id="19989643" type="Audio">
                   <createdOn>${xmlunit.isDateTime}</createdOn>
                   <credential type="token_version_3">
                     token-User1-Spot
                   </credential>
                   <name>user1namespot</name>
                   <sourceproviderid>15</sourceproviderid>
                   <sourcename>user1@example.org</sourcename>
                   <sourceSettings/>
                   <updatedOn>${xmlunit.isDateTime}</updatedOn>
                   <username>user1namespot</username>
                 </source>
                 <sourceid>19989643</sourceid>
                 <updatedOn>2025-11-09T19:42:11.000+00:00</updatedOn>
               </recent>
               <recent id="67889001">
                 <contentItemType>stationurl</contentItemType>
                 <createdOn>2018-11-27T18:20:01.000+00:00</createdOn>
                 <lastplayedat>2025-11-09T19:42:06.000+00:00</lastplayedat>
                 <location>/v1/playback/station/s80044</location>
                 <name>Radio Foo</name>
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
                 <sourceid>19989342</sourceid>
                 <updatedOn>2025-11-09T19:42:09.000+00:00</updatedOn>
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
                   <credential type="token_version_3">
                     token-User1-Spot
                   </credential>
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
                   <credential type="token_version_3">
                     token-User1-Spot
                   </credential>
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
                   <credential type="token_version_3">
                     token-User1-Spot
                   </credential>
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
               <recent id="123">
                 <contentItemType>tracklisturl</contentItemType>
                 <createdOn>2025-11-09T11:40:37.000+00:00</createdOn>
                 <lastplayedat>2025-11-09T19:42:09.000+00:00</lastplayedat>
                 <location>/playback/container/35546f3</location>
                 <name>A Name</name>
                 <source id="19989643" type="Audio">
                   <createdOn>${xmlunit.isDateTime}</createdOn>
                   <credential type="token_version_3">
                     token-User1-Spot
                   </credential>
                   <name>user1namespot</name>
                   <sourceproviderid>15</sourceproviderid>
                   <sourcename>user1@example.org</sourcename>
                   <sourceSettings/>
                   <updatedOn>${xmlunit.isDateTime}</updatedOn>
                   <username>user1namespot</username>
                 </source>
                 <sourceid>19989643</sourceid>
                 <updatedOn>2025-11-09T19:42:11.000+00:00</updatedOn>
               </recent>
               <recent id="67889001">
                 <contentItemType>stationurl</contentItemType>
                 <createdOn>2018-11-27T18:20:01.000+00:00</createdOn>
                 <lastplayedat>2025-11-09T19:42:06.000+00:00</lastplayedat>
                 <location>/v1/playback/station/s80044</location>
                 <name>Radio Foo</name>
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
                 <sourceid>19989342</sourceid>
                 <updatedOn>2025-11-09T19:42:09.000+00:00</updatedOn>
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
               token-User1-Spot
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
       </account>
       """;

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
        .get("/streaming/account/6921073/full")
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
}
