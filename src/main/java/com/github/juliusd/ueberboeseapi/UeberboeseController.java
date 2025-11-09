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
    response.addSourceproviderItem(createSourceProvider(1, "2012-09-19T12:43:00.000+00:00", "PANDORA", "2012-09-19T12:43:00.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(2, "2012-09-19T12:43:00.000+00:00", "INTERNET_RADIO", "2012-09-19T12:43:00.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(3, "2012-10-22T16:03:00.000+00:00", "OFF", "2012-10-22T16:03:00.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(4, "2012-10-22T16:04:00.000+00:00", "LOCAL", "2012-10-22T16:04:00.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(5, "2012-10-22T16:04:00.000+00:00", "AIRPLAY", "2012-10-22T16:04:00.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(6, "2012-10-22T16:04:00.000+00:00", "CURRATED_RADIO", "2012-10-22T16:04:00.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(7, "2012-10-22T16:04:00.000+00:00", "STORED_MUSIC", "2012-10-22T16:04:00.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(8, "2012-10-22T16:04:00.000+00:00", "SLAVE_SOURCE", "2012-10-22T16:04:00.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(9, "2012-10-22T16:04:00.000+00:00", "AUX", "2012-10-22T16:04:00.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(10, "2013-01-10T09:45:00.000+00:00", "RECOMMENDED_INTERNET_RADIO", "2013-01-10T09:45:00.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(11, "2013-01-10T09:45:00.000+00:00", "LOCAL_INTERNET_RADIO", "2013-01-10T09:45:00.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(12, "2013-01-10T09:45:00.000+00:00", "GLOBAL_INTERNET_RADIO", "2013-01-10T09:45:00.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(13, "2014-03-17T15:30:07.000+00:00", "HELLO", "2014-03-17T15:30:07.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(14, "2014-03-17T15:30:27.000+00:00", "DEEZER", "2014-03-17T15:30:27.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(15, "2014-03-17T15:30:27.000+00:00", "SPOTIFY", "2014-03-17T15:30:27.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(16, "2014-03-17T15:30:27.000+00:00", "IHEART", "2014-03-17T15:30:27.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(17, "2014-12-04T19:49:55.000+00:00", "SIRIUSXM", "2014-12-04T19:49:55.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(18, "2014-12-04T19:49:55.000+00:00", "GOOGLE_PLAY_MUSIC", "2014-12-04T19:49:55.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(19, "2014-12-04T19:49:55.000+00:00", "QQMUSIC", "2014-12-04T19:49:55.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(20, "2014-12-04T19:49:55.000+00:00", "AMAZON", "2014-12-04T19:49:55.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(21, "2015-07-13T12:00:00.000+00:00", "LOCAL_MUSIC", "2015-07-13T12:00:00.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(22, "2016-04-08T17:27:21.000+00:00", "WBMX", "2016-04-08T17:27:21.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(23, "2016-04-08T17:27:21.000+00:00", "SOUNDCLOUD", "2016-04-08T17:27:21.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(24, "2016-04-08T17:27:21.000+00:00", "TIDAL", "2016-04-08T17:27:21.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(25, "2016-04-08T17:27:21.000+00:00", "TUNEIN", "2016-04-08T17:27:21.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(26, "2016-06-17T18:00:54.000+00:00", "QPLAY", "2016-06-17T18:00:54.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(27, "2016-08-01T13:53:40.000+00:00", "JUKE", "2016-08-01T13:53:40.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(28, "2016-08-01T13:53:40.000+00:00", "BBC", "2016-08-01T13:53:40.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(29, "2016-08-01T13:53:40.000+00:00", "DARFM", "2016-08-01T13:53:40.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(30, "2016-08-01T13:53:40.000+00:00", "7DIGITAL", "2016-08-01T13:53:40.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(31, "2016-08-01T13:53:40.000+00:00", "SAAVN", "2016-08-01T13:53:40.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(32, "2016-08-01T13:53:40.000+00:00", "RDIO", "2016-08-01T13:53:40.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(33, "2016-10-26T14:42:49.000+00:00", "PHONE_MUSIC", "2016-10-26T14:42:49.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(34, "2017-12-04T19:18:47.000+00:00", "ALEXA", "2017-12-04T19:18:47.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(35, "2019-05-28T18:21:20.000+00:00", "RADIOPLAYER", "2019-05-28T18:21:20.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(36, "2019-05-28T18:21:41.000+00:00", "RADIO.COM", "2019-05-28T18:21:41.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(37, "2019-06-13T17:30:47.000+00:00", "RADIO_COM", "2019-06-13T17:30:47.000+00:00"));
    response.addSourceproviderItem(createSourceProvider(38, "2019-11-25T18:00:33.000+00:00", "SIRIUSXM_EVEREST", "2019-11-25T18:00:33.000+00:00"));

    return ResponseEntity.ok()
        .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        .header("Access-Control-Allow-Headers", "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
        .header("Access-Control-Expose-Headers", "Authorization")
        .body(response);
  }

  private SourceProviderApiDto createSourceProvider(int id, String createdOn, String name, String updatedOn) {
    SourceProviderApiDto provider = new SourceProviderApiDto();
    provider.setId(id);
    provider.setCreatedOn(java.time.OffsetDateTime.parse(createdOn));
    provider.setName(name);
    provider.setUpdatedOn(java.time.OffsetDateTime.parse(updatedOn));
    return provider;
  }
}
