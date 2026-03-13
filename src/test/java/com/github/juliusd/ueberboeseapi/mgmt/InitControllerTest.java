package com.github.juliusd.ueberboeseapi.mgmt;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.ueberboeseapi.TestBase;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

class InitControllerTest extends TestBase {

  @LocalServerPort private int port;

  @Test
  void setupScript_shouldReturn200WithoutAuthentication() {
    given().when().get("/mgmt/init/set-up-this-speaker.sh").then().statusCode(200);
  }

  @Test
  void setupScript_shouldReturnTextPlainContentType() {
    given()
        .when()
        .get("/mgmt/init/set-up-this-speaker.sh")
        .then()
        .statusCode(200)
        .contentType("text/plain");
  }

  @Test
  void setupScript_shouldContainBaseUrlFromHostHeader() {
    String body =
        given()
            .header("Host", "my-server.example.com")
            .when()
            .get("/mgmt/init/set-up-this-speaker.sh")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString();

    assertThat(body).contains("http://my-server.example.com");
  }

  @Test
  void setupScript_shouldUseHttpsWhenXForwardedProtoIsHttps() {
    String body =
        given()
            .header("Host", "my-server.example.com")
            .header("X-Forwarded-Proto", "https")
            .when()
            .get("/mgmt/init/set-up-this-speaker.sh")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString();

    assertThat(body).contains("https://my-server.example.com");
  }

  @Test
  void setupScript_shouldContainKeyScriptElements() {
    Response response = given().when().get("/mgmt/init/set-up-this-speaker.sh");

    String body = response.then().statusCode(200).extract().body().asString();

    assertThat(body).contains("OverrideSdkPrivateCfg.xml");
    assertThat(body).contains(".bak.");
    assertThat(body).contains("reboot");
    assertThat(body).contains("/var/lib/Bose/PersistenceDataRoot");
  }

  @Test
  void setupScript_shouldEmbedBaseUrlInXmlConfig() {
    String body =
        given()
            .header("Host", "localhost:" + port)
            .when()
            .get("/mgmt/init/set-up-this-speaker.sh")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString();

    String expectedBaseUrl = "http://localhost:" + port;
    assertThat(body).contains("<margeServerUrl>" + expectedBaseUrl + "</margeServerUrl>");
    assertThat(body).contains("<statsServerUrl>" + expectedBaseUrl + "</statsServerUrl>");
    assertThat(body)
        .contains(
            "<bmxRegistryUrl>" + expectedBaseUrl + "/bmx/registry/v1/services</bmxRegistryUrl>");
  }
}
