package com.github.juliusd.ueberboeseapi.mgmt;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.github.juliusd.ueberboeseapi.TestBase;
import com.github.juliusd.ueberboeseapi.device.Device;
import com.github.juliusd.ueberboeseapi.device.DeviceRepository;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MgmtControllerTest extends TestBase {

  @Autowired private DeviceRepository deviceRepository;

  @Test
  void listSpeakers_shouldReturnListOfSpeakers() {
    // Given
    String accountId = "6921042";
    OffsetDateTime now = OffsetDateTime.now();
    deviceRepository.save(
        Device.builder()
            .deviceId("device1")
            .name(null)
            .ipAddress("192.168.1.100")
            .firstSeen(now)
            .lastSeen(now)
            .version(null)
            .build());
    deviceRepository.save(
        Device.builder()
            .deviceId("device2")
            .name(null)
            .ipAddress("192.168.1.101")
            .firstSeen(now)
            .lastSeen(now)
            .version(null)
            .build());

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
  void listSpeakers_shouldReturnEmptyListWhenNoDevices() {
    // Given
    String accountId = "6921042";
    // No devices in DB (TestBase clears DB before each test)

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

  @Test
  void listAccountIds_shouldReturnDistinctListOfAccountIds() {
    // Given
    OffsetDateTime now = OffsetDateTime.now();

    deviceRepository.save(
        Device.builder()
            .deviceId("d1")
            .margeAccountId("6921042")
            .ipAddress("10.0.0.1")
            .firstSeen(now)
            .lastSeen(now)
            .build());
    deviceRepository.save(
        Device.builder()
            .deviceId("d2")
            .margeAccountId("6921042")
            .ipAddress("10.0.0.2")
            .firstSeen(now)
            .lastSeen(now)
            .build()); // Dubbel ID
    deviceRepository.save(
        Device.builder()
            .deviceId("d3")
            .margeAccountId("0")
            .ipAddress("10.0.0.3")
            .firstSeen(now)
            .lastSeen(now)
            .build());
    deviceRepository.save(
        Device.builder()
            .deviceId("d4")
            .margeAccountId("7543119")
            .ipAddress("10.0.0.4")
            .firstSeen(now)
            .lastSeen(now)
            .build());
    deviceRepository.save(
        Device.builder()
            .deviceId("d5")
            .margeAccountId(null)
            .ipAddress("10.0.0.5")
            .firstSeen(now)
            .lastSeen(now)
            .build()); // Null mag niet mee

    // When
    Response response =
        given()
            .auth()
            .basic("admin", "test-password-123")
            .accept(ContentType.JSON)
            .when()
            .get("/mgmt/accounts");

    // Then
    response
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("accountIds", hasSize(3)) // "6921042", "0", en "7543119"
        .body("accountIds", containsInAnyOrder("6921042", "0", "7543119"));
  }

  @Test
  void listAccountIds_shouldReturnEmptyListWhenNoAccountsExist() {
    // Given - Geen devices in database

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .accept(ContentType.JSON)
        .when()
        .get("/mgmt/accounts")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("accountIds", hasSize(0));
  }

  @Test
  void listAccountIds_shouldRequireAuthentication() {
    // When / Then
    given().accept(ContentType.JSON).when().get("/mgmt/accounts").then().statusCode(401);
  }
}
