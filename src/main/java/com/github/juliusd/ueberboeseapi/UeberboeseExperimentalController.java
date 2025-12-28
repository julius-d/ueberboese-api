package com.github.juliusd.ueberboeseapi;

import com.github.juliusd.ueberboeseapi.generated.ExperimentalApi;
import com.github.juliusd.ueberboeseapi.generated.dtos.CredentialApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetUpdateRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.PresetUpdateResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SoftwareUpdateResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceApiDto;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ueberboese.experimental.enabled", havingValue = "true")
public class UeberboeseExperimentalController implements ExperimentalApi {

  @Override
  public ResponseEntity<PresetUpdateResponseApiDto> updatePreset(
      String accountId,
      String deviceId,
      Integer presetNumber,
      PresetUpdateRequestApiDto presetUpdateRequestApiDto) {

    log.info("Updating preset {} for account {} and device {}", presetNumber, accountId, deviceId);

    // Build the credential
    CredentialApiDto credential = new CredentialApiDto();
    credential.setType("token_version_3");
    credential.setValue("mockToken456xyz=");

    // Build the source object from the request
    SourceApiDto source = new SourceApiDto();
    source.setId(presetUpdateRequestApiDto.getSourceid());
    source.setType("Audio");
    source.setCreatedOn(OffsetDateTime.parse("2018-08-11T09:52:31.000+00:00"));
    source.setCredential(credential);
    source.setName("mockuser5zt8py3wuxy123");
    source.setSourceproviderid("15");
    source.setSourcename("user@example.com");
    source.setUpdatedOn(OffsetDateTime.parse("2018-11-26T18:42:27.000+00:00"));
    source.setUsername("mockuser5zt8py3wuxy123");

    // Build the response
    PresetUpdateResponseApiDto response = new PresetUpdateResponseApiDto();
    response.setButtonNumber(presetUpdateRequestApiDto.getButtonNumber());
    response.setContainerArt(presetUpdateRequestApiDto.getContainerArt());
    response.setContentItemType(presetUpdateRequestApiDto.getContentItemType());
    response.setCreatedOn(OffsetDateTime.parse("2018-11-14T18:27:39.000+00:00"));
    response.setLocation(presetUpdateRequestApiDto.getLocation());
    response.setName(presetUpdateRequestApiDto.getName());
    response.setSource(source);
    response.setUpdatedOn(OffsetDateTime.parse("2025-12-28T16:38:41.000+00:00"));
    response.setUsername(presetUpdateRequestApiDto.getUsername());

    return ResponseEntity.ok()
        .header(
            "Location",
            String.format(
                "http://streamingqa.bose.com/account/%s/device/%s/preset/%d",
                accountId, deviceId, presetNumber))
        .body(response);
  }

  @Override
  public ResponseEntity<SoftwareUpdateResponseApiDto> getSoftwareUpdate(String accountId) {
    log.info("Getting software update for account {}", accountId);

    SoftwareUpdateResponseApiDto response = new SoftwareUpdateResponseApiDto();
    response.setSoftwareUpdateLocation("");

    return ResponseEntity.ok(response);
  }
}
