package com.github.juliusd.ueberboeseapi.mgmt;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

import com.github.juliusd.ueberboeseapi.TestBase;
import com.github.juliusd.ueberboeseapi.generated.dtos.*;
import com.github.juliusd.ueberboeseapi.service.AccountDataService;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DirtiesContext
class MgmtControllerTest extends TestBase {

  @MockitoBean private AccountDataService accountDataService;

  @Test
  void listSpeakers_shouldReturnListOfSpeakers() throws IOException {
    // Given
    String accountId = "6921042";
    FullAccountResponseApiDto accountData = createAccountDataWithSpeakers();

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(accountData);

    // When
    Response response =
        given()
            .auth()
            .basic("admin", "test-password-123")
            .accept(ContentType.JSON)
            .when()
            .get("/mgmt/accounts/{accountId}/speakers", accountId);

    // Then
    response
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("speakers", hasSize(2))
        .body("speakers[0].ipAddress", equalTo("192.168.1.100"))
        .body("speakers[1].ipAddress", equalTo("192.168.1.101"));
  }

  @Test
  void listSpeakers_shouldReturnEmptyListWhenNoDevices() throws IOException {
    // Given
    String accountId = "6921042";
    FullAccountResponseApiDto accountData = createAccountDataWithoutDevices();

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(accountData);

    // When
    Response response =
        given()
            .auth()
            .basic("admin", "test-password-123")
            .accept(ContentType.JSON)
            .when()
            .get("/mgmt/accounts/{accountId}/speakers", accountId);

    // Then
    response.then().statusCode(200).contentType("application/json").body("speakers", hasSize(0));
  }

  @Test
  void listSpeakers_shouldReturn404WhenAccountNotFound() {
    // Given
    String accountId = "nonexistent";

    when(accountDataService.hasAccountData(accountId)).thenReturn(false);

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .accept(ContentType.JSON)
        .when()
        .get("/mgmt/accounts/{accountId}/speakers", accountId)
        .then()
        .statusCode(404);
  }

  @Test
  void listSpeakers_shouldReturn500WhenIOExceptionOccurs() throws IOException {
    // Given
    String accountId = "6921042";

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId))
        .thenThrow(new IOException("Failed to read file"));

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .accept(ContentType.JSON)
        .when()
        .get("/mgmt/accounts/{accountId}/speakers", accountId)
        .then()
        .statusCode(500)
        .contentType("application/json")
        .body("error", equalTo("Internal server error"))
        .body("message", equalTo("Failed to retrieve speakers"));
  }

  @Test
  void listSpeakers_shouldFilterOutDevicesWithoutIPAddress() throws IOException {
    // Given
    String accountId = "6921042";
    FullAccountResponseApiDto accountData = createAccountDataWithMixedDevices();

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(accountData);

    // When
    Response response =
        given()
            .auth()
            .basic("admin", "test-password-123")
            .accept(ContentType.JSON)
            .when()
            .get("/mgmt/accounts/{accountId}/speakers", accountId);

    // Then
    response
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("speakers", hasSize(1))
        .body("speakers[0].ipAddress", equalTo("192.168.1.100"));
  }

  @Test
  void listSpeakers_shouldRequireAuthentication() {
    // Given
    String accountId = "6921042";

    // When / Then - No authentication
    given()
        .accept(ContentType.JSON)
        .when()
        .get("/mgmt/accounts/{accountId}/speakers", accountId)
        .then()
        .statusCode(401);
  }

  @Test
  void listSpeakers_shouldRejectInvalidCredentials() {
    // Given
    String accountId = "6921042";

    // When / Then - Wrong password
    given()
        .auth()
        .basic("admin", "wrong-password")
        .accept(ContentType.JSON)
        .when()
        .get("/mgmt/accounts/{accountId}/speakers", accountId)
        .then()
        .statusCode(401);
  }

  // Helper methods to create test data

  private static FullAccountResponseApiDto createAccountDataWithSpeakers() {
    FullAccountResponseApiDto accountData = new FullAccountResponseApiDto();
    accountData.setId("6921042");
    accountData.setAccountStatus("ACTIVE");
    accountData.setMode("global");
    accountData.setPreferredLanguage("de");

    DevicesContainerApiDto devices = new DevicesContainerApiDto();
    devices.setDevice(new ArrayList<>());

    // Device 1 with IP
    DeviceApiDto device1 = createDevice("device1", "192.168.1.100");
    devices.addDeviceItem(device1);

    // Device 2 with IP
    DeviceApiDto device2 = createDevice("device2", "192.168.1.101");
    devices.addDeviceItem(device2);

    accountData.setDevices(devices);
    accountData.setSources(new SourcesContainerApiDto());

    return accountData;
  }

  private FullAccountResponseApiDto createAccountDataWithoutDevices() {
    FullAccountResponseApiDto accountData = new FullAccountResponseApiDto();
    accountData.setId("6921042");
    accountData.setAccountStatus("ACTIVE");
    accountData.setMode("global");
    accountData.setPreferredLanguage("de");

    DevicesContainerApiDto devices = new DevicesContainerApiDto();
    devices.setDevice(new ArrayList<>());

    accountData.setDevices(devices);
    accountData.setSources(new SourcesContainerApiDto());

    return accountData;
  }

  private FullAccountResponseApiDto createAccountDataWithMixedDevices() {
    FullAccountResponseApiDto accountData = new FullAccountResponseApiDto();
    accountData.setId("6921042");
    accountData.setAccountStatus("ACTIVE");
    accountData.setMode("global");
    accountData.setPreferredLanguage("de");

    DevicesContainerApiDto devices = new DevicesContainerApiDto();
    devices.setDevice(new ArrayList<>());

    // Device with IP
    DeviceApiDto device1 = createDevice("device1", "192.168.1.100");
    devices.addDeviceItem(device1);

    // Device without IP
    DeviceApiDto device2 = createDevice("device2", null);
    devices.addDeviceItem(device2);

    // Device with empty IP
    DeviceApiDto device3 = createDevice("device3", "");
    devices.addDeviceItem(device3);

    accountData.setDevices(devices);
    accountData.setSources(new SourcesContainerApiDto());

    return accountData;
  }

  private static DeviceApiDto createDevice(String deviceId, String ipAddress) {
    DeviceApiDto device = new DeviceApiDto();
    device.setDeviceid(deviceId);
    device.setIpaddress(ipAddress);
    device.setName("Test Device " + deviceId);
    device.setFirmwareVersion("1.0.0");
    device.setSerialNumber("SN" + deviceId);
    device.setCreatedOn(OffsetDateTime.now());
    device.setUpdatedOn(OffsetDateTime.now());
    device.setAttachedProduct(new AttachedProductApiDto());
    device.setPresets(new PresetsContainerApiDto());
    device.setRecents(new RecentsContainerApiDto());
    return device;
  }
}
