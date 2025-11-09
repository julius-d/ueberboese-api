package com.github.juliusd.ueberboeseapi;

import com.github.juliusd.ueberboeseapi.generated.DefaultApi;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceProviderApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceProvidersResponseApiDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UeberboeseController implements DefaultApi {

  @Override
  public ResponseEntity<SourceProvidersResponseApiDto> getSourceProviders() {

    SourceProvidersResponseApiDto response = new SourceProvidersResponseApiDto();

    // Create all source providers from the captured response
    response.addSourceproviderItem(
        createSourceProvider(
            1, "PANDORA", "2012-09-19T12:43:00.000+00:00", "2012-09-19T12:43:00.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            2, "INTERNET_RADIO", "2012-09-19T12:43:00.000+00:00", "2012-09-19T12:43:00.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            3, "OFF", "2012-10-22T16:03:00.000+00:00", "2012-10-22T16:03:00.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            4, "LOCAL", "2012-10-22T16:04:00.000+00:00", "2012-10-22T16:04:00.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            5, "AIRPLAY", "2012-10-22T16:04:00.000+00:00", "2012-10-22T16:04:00.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            6, "CURRATED_RADIO", "2012-10-22T16:04:00.000+00:00", "2012-10-22T16:04:00.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            7, "STORED_MUSIC", "2012-10-22T16:04:00.000+00:00", "2012-10-22T16:04:00.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            8, "SLAVE_SOURCE", "2012-10-22T16:04:00.000+00:00", "2012-10-22T16:04:00.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            9, "AUX", "2012-10-22T16:04:00.000+00:00", "2012-10-22T16:04:00.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            10,
            "RECOMMENDED_INTERNET_RADIO",
            "2013-01-10T09:45:00.000+00:00",
            "2013-01-10T09:45:00.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            11,
            "LOCAL_INTERNET_RADIO",
            "2013-01-10T09:45:00.000+00:00",
            "2013-01-10T09:45:00.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            12,
            "GLOBAL_INTERNET_RADIO",
            "2013-01-10T09:45:00.000+00:00",
            "2013-01-10T09:45:00.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            13, "HELLO", "2014-03-17T15:30:07.000+00:00", "2014-03-17T15:30:07.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            14, "DEEZER", "2014-03-17T15:30:27.000+00:00", "2014-03-17T15:30:27.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            15, "SPOTIFY", "2014-03-17T15:30:27.000+00:00", "2014-03-17T15:30:27.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            16, "IHEART", "2014-03-17T15:30:27.000+00:00", "2014-03-17T15:30:27.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            17, "SIRIUSXM", "2014-12-04T19:49:55.000+00:00", "2014-12-04T19:49:55.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            18,
            "GOOGLE_PLAY_MUSIC",
            "2014-12-04T19:49:55.000+00:00",
            "2014-12-04T19:49:55.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            19, "QQMUSIC", "2014-12-04T19:49:55.000+00:00", "2014-12-04T19:49:55.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            20, "AMAZON", "2014-12-04T19:49:55.000+00:00", "2014-12-04T19:49:55.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            21, "LOCAL_MUSIC", "2015-07-13T12:00:00.000+00:00", "2015-07-13T12:00:00.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            22, "WBMX", "2016-04-08T17:27:21.000+00:00", "2016-04-08T17:27:21.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            23, "SOUNDCLOUD", "2016-04-08T17:27:21.000+00:00", "2016-04-08T17:27:21.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            24, "TIDAL", "2016-04-08T17:27:21.000+00:00", "2016-04-08T17:27:21.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            25, "TUNEIN", "2016-04-08T17:27:21.000+00:00", "2016-04-08T17:27:21.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            26, "QPLAY", "2016-06-17T18:00:54.000+00:00", "2016-06-17T18:00:54.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            27, "JUKE", "2016-08-01T13:53:40.000+00:00", "2016-08-01T13:53:40.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            28, "BBC", "2016-08-01T13:53:40.000+00:00", "2016-08-01T13:53:40.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            29, "DARFM", "2016-08-01T13:53:40.000+00:00", "2016-08-01T13:53:40.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            30, "7DIGITAL", "2016-08-01T13:53:40.000+00:00", "2016-08-01T13:53:40.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            31, "SAAVN", "2016-08-01T13:53:40.000+00:00", "2016-08-01T13:53:40.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            32, "RDIO", "2016-08-01T13:53:40.000+00:00", "2016-08-01T13:53:40.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            33, "PHONE_MUSIC", "2016-10-26T14:42:49.000+00:00", "2016-10-26T14:42:49.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            34, "ALEXA", "2017-12-04T19:18:47.000+00:00", "2017-12-04T19:18:47.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            35, "RADIOPLAYER", "2019-05-28T18:21:20.000+00:00", "2019-05-28T18:21:20.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            36, "RADIO.COM", "2019-05-28T18:21:41.000+00:00", "2019-05-28T18:21:41.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            37, "RADIO_COM", "2019-06-13T17:30:47.000+00:00", "2019-06-13T17:30:47.000+00:00"));
    response.addSourceproviderItem(
        createSourceProvider(
            38,
            "SIRIUSXM_EVEREST",
            "2019-11-25T18:00:33.000+00:00",
            "2019-11-25T18:00:33.000+00:00"));

    return ResponseEntity.ok()
        .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        .header(
            "Access-Control-Allow-Headers",
            "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
        .header("Access-Control-Expose-Headers", "Authorization")
        .body(response);
  }

  private static SourceProviderApiDto createSourceProvider(
      int id, String name, String createdOn, String updatedOn) {
    SourceProviderApiDto provider = new SourceProviderApiDto();
    provider.setId(id);
    provider.setCreatedOn(java.time.OffsetDateTime.parse(createdOn));
    provider.setName(name);
    provider.setUpdatedOn(java.time.OffsetDateTime.parse(updatedOn));
    return provider;
  }
}
