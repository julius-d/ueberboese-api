package com.github.juliusd.ueberboeseapi.bmx;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.ueberboeseapi.bmx.report.RadioReportStorageService;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxServiceAvailabilityApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.BmxServicesAvailabilityResponseApiDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BmxServiceTest {

  @Mock private TuneInClient tuneInClient;
  @Mock private RadioReportStorageService radioReportStorageService;

  private BmxService bmxService;

  @BeforeEach
  void setUp() {
    bmxService = new BmxService(tuneInClient, radioReportStorageService);
  }

  @Test
  void getBmxServicesAvailability_localInternetRadioDisabled_returnsDefaultServices() {
    BmxServicesAvailabilityResponseApiDto response = bmxService.getBmxServicesAvailability();

    assertThat(response.getServices())
        .extracting(BmxServiceAvailabilityApiDto::getService)
        .containsExactly("TUNEIN", "SIRIUSXM_EVEREST");
  }

  @Test
  void getBmxServicesAvailability_localInternetRadioEnabled_addsLocalInternetRadio() {
    ReflectionTestUtils.setField(bmxService, "localInternetRadioEnabled", true);

    BmxServicesAvailabilityResponseApiDto response = bmxService.getBmxServicesAvailability();

    assertThat(response.getServices())
        .extracting(BmxServiceAvailabilityApiDto::getService)
        .containsExactly("TUNEIN", "SIRIUSXM_EVEREST", "LOCAL_INTERNET_RADIO");
    BmxServiceAvailabilityApiDto lir = response.getServices().get(2);
    assertThat(lir.getCanAdd()).isTrue();
    assertThat(lir.getCanRemove()).isFalse();
  }
}
