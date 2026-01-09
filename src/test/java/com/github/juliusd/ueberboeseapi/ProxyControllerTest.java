package com.github.juliusd.ueberboeseapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToXml;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class ProxyControllerTest extends TestBase {

  @Autowired private MockMvc mockMvc;

  private WireMockServer wireMockServer;
  private WireMockServer authWireMockServer;
  private WireMockServer softwareUpdateWireMockServer;
  private WireMockServer statsWireMockServer;
  private WireMockServer bmxRegistryWireMockServer;

  @BeforeEach
  void setUp() {
    // Set up main target host mock server
    wireMockServer = new WireMockServer(options().port(8089));
    wireMockServer.start();

    // Set up auth target host mock server
    authWireMockServer = new WireMockServer(options().port(8090));
    authWireMockServer.start();

    // Set up software update target host mock server
    softwareUpdateWireMockServer = new WireMockServer(options().port(8091));
    softwareUpdateWireMockServer.start();

    // Set up stats target host mock server
    statsWireMockServer = new WireMockServer(options().port(8092));
    statsWireMockServer.start();

    // Set up BMX registry target host mock server
    bmxRegistryWireMockServer = new WireMockServer(options().port(8093));
    bmxRegistryWireMockServer.start();
  }

  @AfterEach
  void tearDown() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
    if (authWireMockServer != null) {
      authWireMockServer.stop();
    }
    if (softwareUpdateWireMockServer != null) {
      softwareUpdateWireMockServer.stop();
    }
    if (statsWireMockServer != null) {
      statsWireMockServer.stop();
    }
    if (bmxRegistryWireMockServer != null) {
      bmxRegistryWireMockServer.stop();
    }
  }

  @Test
  void shouldForwardJsonRequestBodyCorrectly() throws Exception {
    // Given
    String requestBody =
        """
                         {"test": "data", "action": "create"}
                         """;
    String responseBody =
        """
                          {"status": "success", "id": 123}
                          """;

    wireMockServer.stubFor(
        WireMock.post(urlEqualTo("/api/test"))
            .withRequestBody(equalToJson(requestBody))
            .withHeader("Content-Type", containing("application/json"))
            .withHeader("X-Test-Header", equalTo("test-value"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody)));

    // When & Then
    mockMvc
        .perform(
            post("/api/test")
                .header("X-Test-Header", "test-value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(content().json(responseBody));

    // Verify the request was received by WireMock with correct body
    wireMockServer.verify(
        postRequestedFor(urlEqualTo("/api/test"))
            .withRequestBody(equalToJson(requestBody))
            .withHeader("Content-Type", containing("application/json"))
            .withHeader("X-Test-Header", equalTo("test-value")));
  }

  @Test
  void shouldForwardXmlRequestBodyCorrectly() throws Exception {
    // Given
    String xmlRequestBody =
        """
       <?xml version="1.0" encoding="UTF-8"?>
       <streaming>
         <source>test-source</source>
         <action>play</action>
         <volume>75</volume>
       </streaming>""";

    String xmlResponseBody =
        """
       <?xml version="1.0" encoding="UTF-8"?>
       <response>
         <status>success</status>
         <message>Playback started</message>
       </response>""";

    wireMockServer.stubFor(
        WireMock.post(urlEqualTo("/streaming/play"))
            .withRequestBody(equalToXml(xmlRequestBody))
            .withHeader("Content-Type", containing("application/vnd.bose.streaming-v1.2+xml"))
            .withHeader("X-Test-Header", equalTo("xml-test"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
                    .withBody(xmlResponseBody)));

    // When & Then
    mockMvc
        .perform(
            post("/streaming/play")
                .header("X-Test-Header", "xml-test")
                .contentType("application/vnd.bose.streaming-v1.2+xml")
                .content(xmlRequestBody))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/vnd.bose.streaming-v1.2+xml"))
        .andExpect(content().string(xmlResponseBody));

    // Verify the XML request was received correctly
    wireMockServer.verify(
        postRequestedFor(urlEqualTo("/streaming/play"))
            .withRequestBody(equalToXml(xmlRequestBody))
            .withHeader("Content-Type", containing("application/vnd.bose.streaming-v1.2+xml"))
            .withHeader("X-Test-Header", equalTo("xml-test")));
  }

  @Test
  void shouldForwardLargeRequestBodyCorrectly() throws Exception {
    // Given - Create a large XML body
    StringBuilder largeXmlBody = new StringBuilder();
    largeXmlBody.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<streaming>\n");
    for (int i = 0; i < 100; i++) {
      largeXmlBody
          .append("  <item id=\"")
          .append(i)
          .append("\">")
          .append("Content for item ")
          .append(i)
          .append("</item>\n");
    }
    largeXmlBody.append("</streaming>");

    String responseBody = "<status>processed</status>";

    wireMockServer.stubFor(
        WireMock.post(urlEqualTo("/streaming/bulk"))
            .withHeader("Content-Type", containing("application/vnd.bose.streaming-v1.2+xml"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/xml")
                    .withBody(responseBody)));

    // When & Then
    mockMvc
        .perform(
            post("/streaming/bulk")
                .header("X-Test-Header", "large-body-test")
                .contentType("application/vnd.bose.streaming-v1.2+xml")
                .content(largeXmlBody.toString()))
        .andExpect(status().isOk())
        .andExpect(content().string(responseBody));

    // Verify the large request was received correctly
    wireMockServer.verify(
        postRequestedFor(urlEqualTo("/streaming/bulk"))
            .withRequestBody(equalToXml(largeXmlBody.toString())));
  }

  @Test
  void shouldHandleEmptyRequestBodyCorrectly() throws Exception {
    wireMockServer.stubFor(
        WireMock.post(urlEqualTo("/api/empty"))
            .willReturn(aResponse().withStatus(200).withBody("OK")));

    mockMvc
        .perform(
            post("/api/empty")
                .header("X-Test-Header", "empty-body-test")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string("OK"));

    wireMockServer.verify(postRequestedFor(urlEqualTo("/api/empty")));
  }

  @Test
  void shouldForwardRequestsWithSpecialCharacters() throws Exception {
    // Given - XML with special characters and encoding
    String xmlWithSpecialChars =
        """
       <?xml version="1.0" encoding="UTF-8"?>
       <streaming>
         <title>Café &amp; Résumé</title>
         <description>Special chars: äöü €£¥ ñáéíóú</description>
         <emoji>🎵🎶</emoji>
       </streaming>""";

    wireMockServer.stubFor(
        WireMock.post(urlEqualTo("/streaming/special"))
            .willReturn(aResponse().withStatus(200).withBody("Special chars handled")));

    // When & Then
    mockMvc
        .perform(
            post("/streaming/special")
                .contentType("application/vnd.bose.streaming-v1.2+xml")
                .content(xmlWithSpecialChars))
        .andExpect(status().isOk())
        .andExpect(content().string("Special chars handled"));

    // Verify special characters were preserved
    wireMockServer.verify(
        postRequestedFor(urlEqualTo("/streaming/special"))
            .withRequestBody(equalToXml(xmlWithSpecialChars)));
  }

  @Test
  void shouldForwardRequestsToAuthTargetHost() throws Exception {
    // Given
    String authRequestBody =
        """
                             {"username": "test", "password": "secret"}
                             """;

    authWireMockServer.stubFor(
        WireMock.post(urlEqualTo("/api/login"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"token\": \"abc123\"}")));

    // When & Then
    mockMvc
        .perform(
            post("/api/login")
                .header("Host", "auth.example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authRequestBody))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"token\": \"abc123\"}"));

    // Verify request went to auth server
    authWireMockServer.verify(
        postRequestedFor(urlEqualTo("/api/login")).withRequestBody(equalToJson(authRequestBody)));
  }

  @Test
  void shouldHandleTargetServerError() throws Exception {
    // Given
    String requestBody = "{\"test\": \"error\"}";

    wireMockServer.stubFor(
        WireMock.post(urlEqualTo("/api/error"))
            .willReturn(
                aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\": \"Internal server error\"}")));

    // When & Then
    mockMvc
        .perform(post("/api/error").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isInternalServerError())
        .andExpect(content().json("{\"error\": \"Internal server error\"}"));

    // Verify request was still forwarded correctly
    wireMockServer.verify(
        postRequestedFor(urlEqualTo("/api/error")).withRequestBody(equalToJson(requestBody)));
  }

  @Test
  void shouldForwardGetRequest() throws Exception {
    // Given
    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/unknown-endpoint"))
            .withHeader("X-Test-Header", equalTo("test-value"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"forwarded\"}")));

    // When & Then
    mockMvc
        .perform(
            get("/unknown-endpoint")
                .header("X-Test-Header", "test-value")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"message\": \"forwarded\"}"));

    // Verify request was forwarded correctly
    wireMockServer.verify(
        getRequestedFor(urlEqualTo("/unknown-endpoint"))
            .withHeader("X-Test-Header", equalTo("test-value")));
  }

  @Test
  void shouldNotForwardKnownEndpoints() throws Exception {
    // Test that known endpoints are not forwarded but handled internally
    mockMvc
        .perform(
            get("/streaming/sourceproviders")
                .contentType("application/vnd.bose.streaming-v1.2+xml"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/vnd.bose.streaming-v1.2+xml"));

    // Verify no request was made to WireMock (since it should be handled internally)
    wireMockServer.verify(0, getRequestedFor(urlMatching("/streaming/.*")));
  }

  @Test
  void shouldForwardGetRequestWithQueryParameters() throws Exception {
    // Given
    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/test-with-params?param1=value1&param2=value2"))
            .withHeader("X-Custom-Header", equalTo("custom-value"))
            .willReturn(aResponse().withStatus(200).withBody("Query params forwarded")));

    // When & Then
    mockMvc
        .perform(
            get("/test-with-params?param1=value1&param2=value2")
                .header("X-Custom-Header", "custom-value"))
        .andExpect(status().isOk())
        .andExpect(content().string("Query params forwarded"));

    // Verify query parameters were forwarded
    wireMockServer.verify(
        getRequestedFor(urlEqualTo("/test-with-params?param1=value1&param2=value2"))
            .withHeader("X-Custom-Header", equalTo("custom-value")));
  }

  @Test
  void shouldForwardNonAuthRequestsToDefaultTargetHost() throws Exception {
    // Given
    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/products"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                              {"products": []}""")));

    // When & Then
    mockMvc
        .perform(get("/api/products").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .json(
                    """
                                  {"products": []}"""));

    // Verify request went to main server
    wireMockServer.verify(getRequestedFor(urlEqualTo("/api/products")));

    // Verify request did NOT go to auth server
    authWireMockServer.verify(0, getRequestedFor(urlEqualTo("/api/products")));
  }

  @Test
  void shouldForwardRequestsToSoftwareUpdateTargetHostButReturn404() throws Exception {
    // Given
    String updateRequestBody =
        """
                             {"version": "1.2.3", "checksum": "abc123"}
                             """;

    softwareUpdateWireMockServer.stubFor(
        WireMock.post(urlEqualTo("/api/firmware/update"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"status\": \"update available\"}")));

    // When & Then
    mockMvc
        .perform(
            post("/api/firmware/update")
                .header("Host", "downloads.example.org")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestBody))
        .andExpect(status().isNotFound());

    // Verify request went to software update server
    softwareUpdateWireMockServer.verify(
        postRequestedFor(urlEqualTo("/api/firmware/update"))
            .withRequestBody(equalToJson(updateRequestBody)));

    // Verify request did NOT go to main or auth servers
    wireMockServer.verify(0, postRequestedFor(urlEqualTo("/api/firmware/update")));
    authWireMockServer.verify(0, postRequestedFor(urlEqualTo("/api/firmware/update")));
  }

  @Test
  void shouldNotForwardNonSoftwareUpdateRequestsToUpdateTarget() throws Exception {
    // Given
    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/data"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                              {"data": "content"}""")));

    // When & Then
    mockMvc
        .perform(
            get("/api/data")
                .header("Host", "api.example.com")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .json(
                    """
                                  {"data": "content"}"""));

    // Verify request went to default server
    wireMockServer.verify(getRequestedFor(urlEqualTo("/api/data")));

    // Verify request did NOT go to software update server
    softwareUpdateWireMockServer.verify(0, getRequestedFor(urlEqualTo("/api/data")));
  }

  @Test
  void shouldHandleEmptyResponseBody() throws Exception {
    // Given - Mock server returns 204 No Content with empty body
    wireMockServer.stubFor(
        WireMock.post(urlEqualTo("/api/no-content"))
            .willReturn(aResponse().withStatus(204).withHeader("X-Custom-Header", "test-value")));

    // When & Then
    mockMvc
        .perform(
            post("/api/no-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"data\": \"test\"}"))
        .andExpect(status().isNoContent());

    // Verify request was forwarded correctly
    wireMockServer.verify(postRequestedFor(urlEqualTo("/api/no-content")));
  }

  @Test
  void shouldHandleEmptyResponseBodyWithOkStatus() throws Exception {
    // Given - Mock server returns 200 OK with empty body
    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/empty-ok")).willReturn(aResponse().withStatus(200)));

    // When & Then
    mockMvc.perform(get("/api/empty-ok")).andExpect(status().isOk());

    // Verify request was forwarded correctly
    wireMockServer.verify(getRequestedFor(urlEqualTo("/api/empty-ok")));
  }

  @Test
  void shouldHandleEmptyResponseBodyWithCreatedStatus() throws Exception {
    // Given - Mock server returns 201 Created with empty body
    wireMockServer.stubFor(
        WireMock.post(urlEqualTo("/api/create"))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Location", "http://localhost:8089/api/resource/123")));

    // When & Then
    mockMvc
        .perform(
            post("/api/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"test\"}"))
        .andExpect(status().isCreated());

    // Verify request was forwarded correctly
    wireMockServer.verify(postRequestedFor(urlEqualTo("/api/create")));
  }

  @Test
  void shouldForwardRequestsToStatsTargetHost() throws Exception {
    // Given
    String statsRequestBody =
        """
                        {"event": "device_usage", "timestamp": "2024-01-01T12:00:00Z"}
                        """;

    statsWireMockServer.stubFor(
        WireMock.post(urlEqualTo("/api/events"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"status\": \"recorded\"}")));

    // When & Then
    mockMvc
        .perform(
            post("/api/events")
                .header("Host", "stats.example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(statsRequestBody))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"status\": \"recorded\"}"));

    // Verify request went to stats server
    statsWireMockServer.verify(
        postRequestedFor(urlEqualTo("/api/events")).withRequestBody(equalToJson(statsRequestBody)));

    // Verify request did NOT go to other servers
    wireMockServer.verify(0, postRequestedFor(urlEqualTo("/api/events")));
    authWireMockServer.verify(0, postRequestedFor(urlEqualTo("/api/events")));
    softwareUpdateWireMockServer.verify(0, postRequestedFor(urlEqualTo("/api/events")));
    bmxRegistryWireMockServer.verify(0, postRequestedFor(urlEqualTo("/api/events")));
  }

  @Test
  void shouldForwardRequestsToBmxRegistryTargetHost() throws Exception {
    // Given
    String bmxRequestBody =
        """
                        {"contentType": "playlist", "contentId": "abc123"}
                        """;

    bmxRegistryWireMockServer.stubFor(
        WireMock.post(urlEqualTo("/api/content"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"status\": \"success\"}")));

    // When & Then
    mockMvc
        .perform(
            post("/api/content")
                .header("Host", "bmx.example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bmxRequestBody))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"status\": \"success\"}"));

    // Verify request went to BMX registry server
    bmxRegistryWireMockServer.verify(
        postRequestedFor(urlEqualTo("/api/content")).withRequestBody(equalToJson(bmxRequestBody)));

    // Verify request did NOT go to other servers
    wireMockServer.verify(0, postRequestedFor(urlEqualTo("/api/content")));
    authWireMockServer.verify(0, postRequestedFor(urlEqualTo("/api/content")));
    softwareUpdateWireMockServer.verify(0, postRequestedFor(urlEqualTo("/api/content")));
    statsWireMockServer.verify(0, postRequestedFor(urlEqualTo("/api/content")));
  }

  @Test
  void shouldNotForwardNonStatsNonBmxRequestsToNewTargets() throws Exception {
    // Given
    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/other"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                              {"data": "regular"}""")));

    // When & Then
    mockMvc
        .perform(
            get("/api/other")
                .header("Host", "api.example.com")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .json(
                    """
                                  {"data": "regular"}"""));

    // Verify request went to default server
    wireMockServer.verify(getRequestedFor(urlEqualTo("/api/other")));

    // Verify request did NOT go to stats or BMX registry servers
    statsWireMockServer.verify(0, getRequestedFor(urlEqualTo("/api/other")));
    bmxRegistryWireMockServer.verify(0, getRequestedFor(urlEqualTo("/api/other")));
  }

  @Test
  void shouldForwardMultiValuedHeaders() throws Exception {
    // Given
    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/multi-header"))
            .withHeader("Accept", equalTo("text/html"))
            .withHeader("Accept", equalTo("application/xml"))
            .withHeader("X-Custom", equalTo("value1"))
            .withHeader("X-Custom", equalTo("value2"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"multi-header test\"}")));

    // When & Then
    mockMvc
        .perform(
            get("/api/multi-header")
                .header("Accept", "text/html")
                .header("Accept", "application/xml")
                .header("X-Custom", "value1")
                .header("X-Custom", "value2"))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"message\": \"multi-header test\"}"));

    // Verify all header values were forwarded
    wireMockServer.verify(
        getRequestedFor(urlEqualTo("/api/multi-header"))
            .withHeader("Accept", equalTo("text/html"))
            .withHeader("Accept", equalTo("application/xml"))
            .withHeader("X-Custom", equalTo("value1"))
            .withHeader("X-Custom", equalTo("value2")));
  }

  @Test
  void shouldForwardAuthorizationHeader() throws Exception {
    // Given
    String authToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";

    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/secure"))
            .withHeader("Authorization", equalTo(authToken))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"authenticated\"}")));

    // When & Then
    mockMvc
        .perform(get("/api/secure").header("Authorization", authToken))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"message\": \"authenticated\"}"));

    // Verify Authorization header was forwarded
    wireMockServer.verify(
        getRequestedFor(urlEqualTo("/api/secure")).withHeader("Authorization", equalTo(authToken)));
  }

  @Test
  void shouldForwardBasicAuthorizationHeader() throws Exception {
    // Given
    String basicAuth = "Basic dXNlcm5hbWU6cGFzc3dvcmQ=";

    wireMockServer.stubFor(
        WireMock.post(urlEqualTo("/api/authenticate"))
            .withHeader("Authorization", equalTo(basicAuth))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"status\": \"success\"}")));

    // When & Then
    mockMvc
        .perform(
            post("/api/authenticate")
                .header("Authorization", basicAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"status\": \"success\"}"));

    // Verify Basic Auth header was forwarded
    wireMockServer.verify(
        postRequestedFor(urlEqualTo("/api/authenticate"))
            .withHeader("Authorization", equalTo(basicAuth)));
  }

  @Test
  void shouldForwardCookieHeader() throws Exception {
    // Given
    String cookieValue = "sessionId=abc123; userId=user456; preferences=darkMode";

    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/profile"))
            .withHeader("Cookie", equalTo(cookieValue))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"name\": \"Test User\"}")));

    // When & Then
    mockMvc
        .perform(get("/api/profile").header("Cookie", cookieValue))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"name\": \"Test User\"}"));

    // Verify Cookie header was forwarded
    wireMockServer.verify(
        getRequestedFor(urlEqualTo("/api/profile")).withHeader("Cookie", equalTo(cookieValue)));
  }

  @Test
  void shouldForwardUserAgentHeader() throws Exception {
    // Given
    String userAgent = "Foobar/5.0 (iOS; iPhone14,3; iOS 16.0)";

    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/device-info"))
            .withHeader("User-Agent", equalTo(userAgent))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"compatible\": true}")));

    // When & Then
    mockMvc
        .perform(get("/api/device-info").header("User-Agent", userAgent))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"compatible\": true}"));

    // Verify User-Agent header was forwarded
    wireMockServer.verify(
        getRequestedFor(urlEqualTo("/api/device-info"))
            .withHeader("User-Agent", equalTo(userAgent)));
  }

  @Test
  void shouldForwardAcceptLanguageHeader() throws Exception {
    // Given
    String acceptLanguage = "en-US,en;q=0.9,de;q=0.8";

    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/localized"))
            .withHeader("Accept-Language", equalTo(acceptLanguage))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"greeting\": \"Hello\"}")));

    // When & Then
    mockMvc
        .perform(get("/api/localized").header("Accept-Language", acceptLanguage))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"greeting\": \"Hello\"}"));

    // Verify Accept-Language header was forwarded
    wireMockServer.verify(
        getRequestedFor(urlEqualTo("/api/localized"))
            .withHeader("Accept-Language", equalTo(acceptLanguage)));
  }

  @Test
  void shouldForwardApiKeyHeader() throws Exception {
    // Given
    String apiKey = "X-API-Key-12345-ABCDEF";

    wireMockServer.stubFor(
        WireMock.post(urlEqualTo("/api/data"))
            .withHeader("X-API-Key", equalTo(apiKey))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"id\": \"created\"}")));

    // When & Then
    mockMvc
        .perform(
            post("/api/data")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"data\": \"test\"}"))
        .andExpect(status().isCreated())
        .andExpect(content().json("{\"id\": \"created\"}"));

    // Verify X-API-Key header was forwarded
    wireMockServer.verify(
        postRequestedFor(urlEqualTo("/api/data")).withHeader("X-API-Key", equalTo(apiKey)));
  }

  @Test
  void shouldForwardMultipleSensitiveHeaders() throws Exception {
    // Given
    String authToken = "Bearer token123";
    String cookieValue = "sessionId=xyz789; JSESSIONID=ABC123DEF456; token=secret-token-value";
    String userAgent = "Foobar/5.0";
    String apiKey = "secret-api-key";
    String sessionToken = "session-abc-123-xyz";
    String xAuthToken = "x-auth-token-value";
    String acceptLanguage = "en-US,en;q=0.9";
    String xRequestId = "req-12345-67890";

    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/all-headers"))
            .withHeader("Authorization", equalTo(authToken))
            .withHeader("Cookie", equalTo(cookieValue))
            .withHeader("User-Agent", equalTo(userAgent))
            .withHeader("X-API-Key", equalTo(apiKey))
            .withHeader("X-Session-Token", equalTo(sessionToken))
            .withHeader("X-Auth-Token", equalTo(xAuthToken))
            .withHeader("Accept-Language", equalTo(acceptLanguage))
            .withHeader("X-Request-ID", equalTo(xRequestId))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"status\": \"all headers received\"}")));

    // When & Then
    mockMvc
        .perform(
            get("/api/all-headers")
                .header("Authorization", authToken)
                .header("Cookie", cookieValue)
                .header("User-Agent", userAgent)
                .header("X-API-Key", apiKey)
                .header("X-Session-Token", sessionToken)
                .header("X-Auth-Token", xAuthToken)
                .header("Accept-Language", acceptLanguage)
                .header("X-Request-ID", xRequestId))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"status\": \"all headers received\"}"));

    // Verify all sensitive headers were forwarded
    wireMockServer.verify(
        getRequestedFor(urlEqualTo("/api/all-headers"))
            .withHeader("Authorization", equalTo(authToken))
            .withHeader("Cookie", equalTo(cookieValue))
            .withHeader("User-Agent", equalTo(userAgent))
            .withHeader("X-API-Key", equalTo(apiKey))
            .withHeader("X-Session-Token", equalTo(sessionToken))
            .withHeader("X-Auth-Token", equalTo(xAuthToken))
            .withHeader("Accept-Language", equalTo(acceptLanguage))
            .withHeader("X-Request-ID", equalTo(xRequestId)));
  }

  @Test
  void shouldForward301PermanentRedirect() throws Exception {
    // Given
    String redirectLocation = "http://localhost:8089/api/new-location";

    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/old-endpoint"))
            .willReturn(
                aResponse()
                    .withStatus(301)
                    .withHeader("Location", redirectLocation)
                    .withHeader("Cache-Control", "max-age=3600")));

    // When & Then - Location should be rewritten to use request host
    mockMvc
        .perform(get("/api/old-endpoint"))
        .andExpect(status().isMovedPermanently())
        .andExpect(header().string("Location", "http://localhost/api/new-location"))
        .andExpect(header().string("Cache-Control", "max-age=3600"));

    // Verify request was forwarded
    wireMockServer.verify(getRequestedFor(urlEqualTo("/api/old-endpoint")));
  }

  @Test
  void shouldForward302TemporaryRedirect() throws Exception {
    // Given
    String redirectLocation = "http://localhost:8089/api/temporary-location";

    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/redirect-me"))
            .willReturn(
                aResponse()
                    .withStatus(302)
                    .withHeader("Location", redirectLocation)
                    .withHeader("Cache-Control", "no-cache")));

    // When & Then - Location should be rewritten to use request host
    mockMvc
        .perform(get("/api/redirect-me"))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "http://localhost/api/temporary-location"))
        .andExpect(header().string("Cache-Control", "no-cache"));

    // Verify request was forwarded
    wireMockServer.verify(getRequestedFor(urlEqualTo("/api/redirect-me")));
  }

  @Test
  void shouldRewriteLocationHeaderFor302RedirectWithQueryParams() throws Exception {
    // Given
    String backendLocation =
        "http://localhost:8089/api/redirect-target?param1=value1&param2=value2";

    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/start"))
            .willReturn(aResponse().withStatus(302).withHeader("Location", backendLocation)));

    // When & Then - Location should be rewritten to use request host but preserve path and query
    mockMvc
        .perform(get("/api/start"))
        .andExpect(status().isFound())
        .andExpect(
            header()
                .string(
                    "Location",
                    "http://localhost/api/redirect-target?param1=value1&param2=value2"));

    // Verify request was forwarded
    wireMockServer.verify(getRequestedFor(urlEqualTo("/api/start")));
  }

  @Test
  void shouldForward303SeeOtherRedirect() throws Exception {
    // Given
    String redirectLocation = "http://localhost:8089/api/result";

    wireMockServer.stubFor(
        WireMock.post(urlEqualTo("/api/submit"))
            .willReturn(aResponse().withStatus(303).withHeader("Location", redirectLocation)));

    // When & Then - Location should be rewritten to use request host
    mockMvc
        .perform(
            post("/api/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"data\": \"test\"}"))
        .andExpect(status().isSeeOther())
        .andExpect(header().string("Location", "http://localhost/api/result"));

    // Verify request was forwarded
    wireMockServer.verify(postRequestedFor(urlEqualTo("/api/submit")));
  }

  @Test
  void shouldForward307TemporaryRedirectWithBody() throws Exception {
    // Given
    String redirectLocation = "http://localhost:8089/api/alternative-endpoint";
    String requestBody = "{\"action\": \"process\"}";

    wireMockServer.stubFor(
        WireMock.post(urlEqualTo("/api/endpoint"))
            .withRequestBody(equalToJson(requestBody))
            .willReturn(aResponse().withStatus(307).withHeader("Location", redirectLocation)));

    // When & Then - Location should be rewritten to use request host
    mockMvc
        .perform(post("/api/endpoint").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isTemporaryRedirect())
        .andExpect(header().string("Location", "http://localhost/api/alternative-endpoint"));

    // Verify request was forwarded with correct body
    wireMockServer.verify(
        postRequestedFor(urlEqualTo("/api/endpoint")).withRequestBody(equalToJson(requestBody)));
  }

  @Test
  void shouldForward308PermanentRedirect() throws Exception {
    // Given
    String redirectLocation = "http://localhost:8089/api/permanent-new-location";

    wireMockServer.stubFor(
        WireMock.put(urlEqualTo("/api/legacy"))
            .willReturn(aResponse().withStatus(308).withHeader("Location", redirectLocation)));

    // When & Then - Location should be rewritten to use request host
    mockMvc
        .perform(
            put("/api/legacy")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"update\": \"data\"}"))
        .andExpect(status().isPermanentRedirect())
        .andExpect(header().string("Location", "http://localhost/api/permanent-new-location"));

    // Verify request was forwarded
    wireMockServer.verify(putRequestedFor(urlEqualTo("/api/legacy")));
  }

  @Test
  void shouldForwardRedirectWithAuthorizationHeader() throws Exception {
    // Given
    String redirectLocation = "http://localhost:8089/api/redirected";
    String authToken = "Bearer secure-token-456";

    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/secure-redirect"))
            .withHeader("Authorization", equalTo(authToken))
            .willReturn(aResponse().withStatus(302).withHeader("Location", redirectLocation)));

    // When & Then
    mockMvc
        .perform(get("/api/secure-redirect").header("Authorization", authToken))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "http://localhost/api/redirected"));

    // Verify Authorization header was forwarded
    wireMockServer.verify(
        getRequestedFor(urlEqualTo("/api/secure-redirect"))
            .withHeader("Authorization", equalTo(authToken)));
  }

  @Test
  void shouldPreserveRelativeLocationUrls() throws Exception {
    // Given - Backend returns a relative Location URL (no host)
    String relativeLocation = "/api/relative-redirect";

    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/start-relative"))
            .willReturn(aResponse().withStatus(302).withHeader("Location", relativeLocation)));

    // When & Then - Relative Location should remain unchanged
    mockMvc
        .perform(get("/api/start-relative"))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", relativeLocation));

    // Verify request was forwarded
    wireMockServer.verify(getRequestedFor(urlEqualTo("/api/start-relative")));
  }

  @Test
  void shouldRewriteLocationWithComplexUrl() throws Exception {
    // Given - Location with query params, fragment, and special characters
    String backendLocation =
        "http://localhost:8089/api/redirect?param1=value%201&param2=value2#section";

    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/complex"))
            .willReturn(aResponse().withStatus(302).withHeader("Location", backendLocation)));

    // When & Then - All URL components should be preserved except host
    mockMvc
        .perform(get("/api/complex"))
        .andExpect(status().isFound())
        .andExpect(
            header()
                .string(
                    "Location",
                    "http://localhost/api/redirect?param1=value%201&param2=value2#section"));

    // Verify request was forwarded
    wireMockServer.verify(getRequestedFor(urlEqualTo("/api/complex")));
  }

  @Test
  void shouldHandleRedirectWithoutLocationHeader() throws Exception {
    // Given - 302 response without Location header (edge case)
    wireMockServer.stubFor(
        WireMock.get(urlEqualTo("/api/no-location")).willReturn(aResponse().withStatus(302)));

    // When & Then - Should handle gracefully
    mockMvc.perform(get("/api/no-location")).andExpect(status().isFound());

    // Verify request was forwarded
    wireMockServer.verify(getRequestedFor(urlEqualTo("/api/no-location")));
  }

  @Test
  void shouldRewriteLocationWhenProxyingToAuthTarget() throws Exception {
    // Given - Request goes to auth target host
    String authRedirectLocation = "http://localhost:8090/oauth/callback";

    authWireMockServer.stubFor(
        WireMock.get(urlEqualTo("/oauth/authorize"))
            .willReturn(aResponse().withStatus(302).withHeader("Location", authRedirectLocation)));

    // When & Then - Location should be rewritten to use request Host header value
    mockMvc
        .perform(get("/oauth/authorize").header("Host", "auth.example.com"))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "http://auth.example.com/oauth/callback"));

    // Verify request went to auth server
    authWireMockServer.verify(getRequestedFor(urlEqualTo("/oauth/authorize")));
  }
}
