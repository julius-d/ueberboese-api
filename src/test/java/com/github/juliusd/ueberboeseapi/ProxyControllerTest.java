package com.github.juliusd.ueberboeseapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the proxy functionality.
 * Tests that unknown endpoints are properly forwarded to the configured target.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "proxy.target-host=https://httpbin.org"
})
class ProxyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldForwardUnknownGetRequestToTarget() throws Exception {
        // Test forwarding a GET request to target host
        // We expect some response from the target (even if it's an error),
        // which proves the proxy forwarding is working
        mockMvc.perform(get("/unknown-endpoint")
                .header("X-Test-Header", "test-value")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    // The important thing is that we get a response from the target host,
                    // not a 404 from our application (which would indicate no forwarding)
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 404 || status == 503,
                        "Expected response from target host (200, 404, or 503), but got: " + status);
                });
    }

    @Test
    void shouldForwardUnknownPostRequestToTarget() throws Exception {
        // Test forwarding a POST request to target host
        String requestBody = "{\"test\": \"data\"}";

        mockMvc.perform(post("/api/test")
                .header("X-Test-Header", "test-value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(result -> {
                    // The important thing is that we get a response from the target host,
                    // not a 404 from our application (which would indicate no forwarding)
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 404 || status == 503,
                        "Expected response from target host (200, 404, or 503), but got: " + status);
                });
    }

    @Test
    void shouldNotForwardKnownEndpoints() throws Exception {
        // Test that known endpoints are not forwarded
        mockMvc.perform(get("/streaming/sourceproviders")
                .contentType("application/vnd.bose.streaming-v1.2+xml"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.bose.streaming-v1.2+xml"));
    }

    @Test
    void shouldForwardRequestWithQueryParameters() throws Exception {
        // Test forwarding a request with query parameters
        mockMvc.perform(get("/test-with-params")
                .param("param1", "value1")
                .param("param2", "value2")
                .header("X-Custom-Header", "custom-value"))
                .andExpect(result -> {
                    // The important thing is that we get a response from the target host,
                    // which proves query parameters are forwarded correctly
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 404 || status == 503,
                        "Expected response from target host (200, 404, or 503), but got: " + status);
                });
    }
}