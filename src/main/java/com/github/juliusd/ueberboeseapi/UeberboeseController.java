package com.github.juliusd.ueberboeseapi;

import com.github.juliusd.ueberboeseapi.generated.DefaultApi;
import com.github.juliusd.ueberboeseapi.generated.dtos.CredentialApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentItemRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentItemResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceProviderApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceProvidersResponseApiDto;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UeberboeseController implements DefaultApi {

  @Override
  public ResponseEntity<RecentItemResponseApiDto> addRecentItem(
      String accountId, String deviceId, RecentItemRequestApiDto recentItemRequestApiDto) {

    // Generate a unique ID for the recent item (simulating the real API behavior)
    String recentItemId =
        String.valueOf(ThreadLocalRandom.current().nextLong(1000000000L, 9999999999L));

    SourceApiDto source = buildSourceApiDto(recentItemRequestApiDto);

    // Create the response object
    RecentItemResponseApiDto response = new RecentItemResponseApiDto();
    response.setId(recentItemId);
    response.setContentItemType(recentItemRequestApiDto.getContentItemType());
    response.setCreatedOn(OffsetDateTime.parse("2018-11-27T18:20:01.000+00:00"));
    response.setLastplayedat(recentItemRequestApiDto.getLastplayedat());
    response.setLocation(recentItemRequestApiDto.getLocation());
    response.setName(recentItemRequestApiDto.getName());
    response.setSource(source);
    response.setSourceid(recentItemRequestApiDto.getSourceid());
    response.setUpdatedOn(OffsetDateTime.now());

    // Build the Location header
    String locationHeader =
        String.format(
            "http://streamingqa.bose.com/account/%s/device/%s/recent/%s",
            accountId, deviceId, recentItemId);

    return ResponseEntity.created(URI.create(locationHeader))
        .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        .header(
            "Access-Control-Allow-Headers",
            "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
        .header("Access-Control-Expose-Headers", "Credentials")
        .body(response);
  }

  private static SourceApiDto buildSourceApiDto(RecentItemRequestApiDto recentItemRequestApiDto) {
    var credential = new CredentialApiDto();
    credential.setType("token");
    credential.setValue("eyDu=");

    SourceApiDto source = new SourceApiDto();
    source.setId(recentItemRequestApiDto.getSourceid());
    source.setType("Audio");
    source.setCreatedOn(OffsetDateTime.parse("2018-08-11T08:55:41.000+00:00"));
    source.setCredential(credential);
    source.setName("");
    source.setSourceproviderid("25");
    source.setSourcename("");
    source.setUpdatedOn(OffsetDateTime.parse("2019-07-20T17:48:31.000+00:00"));
    source.setUsername("");
    return source;
  }

  @Override
  public ResponseEntity<SourceProvidersResponseApiDto> getSourceProviders() {

    SourceProvidersResponseApiDto response = new SourceProvidersResponseApiDto();

    // Create all source providers from the enum
    for (SourceProvider provider : SourceProvider.values()) {
      response.addSourceproviderItem(createSourceProvider(provider));
    }

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

  private static SourceProviderApiDto createSourceProvider(SourceProvider sourceProvider) {
    SourceProviderApiDto provider = new SourceProviderApiDto();
    provider.setId(sourceProvider.getId());
    provider.setCreatedOn(sourceProvider.getCreatedOn());
    provider.setName(sourceProvider.getName());
    provider.setUpdatedOn(sourceProvider.getUpdatedOn());
    return provider;
  }
}
