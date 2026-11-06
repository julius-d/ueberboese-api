package com.github.juliusd.ueberboeseapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "ueberboese.http-client.response-timeout=500ms")
class HttpClientTimeoutTest extends TestBase {

  private WireMockServer wireMockServer;

  @BeforeEach
  void setUpWireMock() {
    wireMockServer = new WireMockServer(options().port(8089));
    wireMockServer.start();
  }

  @AfterEach
  void tearDownWireMock() {
    wireMockServer.stop();
  }

  @Test
  void proxy_shouldFailFastWhenUpstreamDoesNotRespond() {
    wireMockServer.stubFor(
        get(urlEqualTo("/slow/endpoint"))
            .willReturn(aResponse().withStatus(200).withBody("too late").withFixedDelay(5_000)));

    long start = System.nanoTime();
    given().when().get("/slow/endpoint").then().statusCode(502);
    long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

    // Fails after the 500ms response timeout instead of waiting for the 5s upstream delay
    assertThat(elapsedMillis).isLessThan(4_000);
  }
}
