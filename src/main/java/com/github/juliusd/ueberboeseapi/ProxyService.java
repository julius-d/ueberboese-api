package com.github.juliusd.ueberboeseapi;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Service responsible for forwarding unknown requests to the configured target host. Logs all
 * request and response details to a dedicated log file.
 */
@Service
public class ProxyService {

  private static final Logger logger = LoggerFactory.getLogger(ProxyService.class);

  private final WebClient webClient;
  private final ProxyProperties proxyProperties;

  public ProxyService(ProxyProperties proxyProperties) {
    this.proxyProperties = proxyProperties;
    this.webClient =
        WebClient.builder()
            .codecs(
                configurer ->
                    configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB buffer
            .build();
  }

  /**
   * Forwards the request to the configured target host.
   *
   * @param request the original HTTP request
   * @param requestBody the request body content
   * @return ResponseEntity with the proxied response
   */
  public ResponseEntity<String> forwardRequest(HttpServletRequest request, String requestBody) {
    String targetUrl = buildTargetUrl(request);
    HttpMethod method = HttpMethod.valueOf(request.getMethod());

    logger.info("=== PROXY REQUEST START ===");
    logger.info("Original URL: {}", request.getRequestURL());
    logger.info("Target URL: {}", targetUrl);
    logger.info("Method: {}", method);
    if (request.getQueryString() != null) {
      logger.info("Content-Type: {}", request.getContentType());
    }
    if (request.getContentLength() != -1) {
      logger.info("Content-Length: {}", request.getContentLength());
    }

    // Log request headers
    logRequestHeaders(request);

    // Log request body if present
    if (requestBody != null && !requestBody.isEmpty()) {
      logger.info("Request Body: {}", requestBody);
    }

    try {
      // Build the WebClient request
      WebClient.RequestBodySpec requestSpec =
          webClient.method(method).uri(targetUrl).headers(headers -> copyHeaders(request, headers));

      // Add body if present
      Mono<ClientResponse> responseMono;
      if (requestBody != null && !requestBody.isEmpty()) {
        responseMono = requestSpec.bodyValue(requestBody).exchange();
      } else {
        responseMono = requestSpec.exchange();
      }

      // Execute request and get response
      ClientResponse clientResponse = responseMono.block();

      if (clientResponse == null) {
        logger.error("Received null response from target");
        return ResponseEntity.status(502).body("Bad Gateway - No response from target");
      }

      // Get response body
      String responseBody = clientResponse.bodyToMono(String.class).block();

      // Log response details
      logger.info("=== PROXY RESPONSE ===");
      logger.info("Status: {}", clientResponse.statusCode().value());
      logger.info("Response Headers:");
      clientResponse
          .headers()
          .asHttpHeaders()
          .forEach((name, values) -> logger.info("  {}: {}", name, String.join(", ", values)));

      if (responseBody != null) {
        logger.info("Response Body: {}", responseBody);
      }
      logger.info("=== PROXY REQUEST END ===");

      // Build response entity
      return ResponseEntity.status(clientResponse.statusCode())
          .headers(clientResponse.headers().asHttpHeaders())
          .body(responseBody);

    } catch (WebClientResponseException e) {
      logger.error("=== PROXY ERROR ===");
      logger.error(
          "Error forwarding request to {}: {} {}", targetUrl, e.getStatusCode(), e.getStatusText());
      logger.error("Error response body: {}", e.getResponseBodyAsString());
      logger.error("=== PROXY REQUEST END ===");

      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());

    } catch (Exception e) {
      logger.error("=== PROXY ERROR ===");
      logger.error("Unexpected error forwarding request to {}", targetUrl, e);
      logger.error("=== PROXY REQUEST END ===");

      return ResponseEntity.status(502).body("Bad Gateway - Error forwarding request");
    }
  }

  private String buildTargetUrl(HttpServletRequest request) {
    String targetHost = proxyProperties.targetHost();
    if (targetHost.endsWith("/")) {
      targetHost = targetHost.substring(0, targetHost.length() - 1);
    }

    String path = request.getRequestURI();
    String queryString = request.getQueryString();

    StringBuilder url = new StringBuilder(targetHost).append(path);
    if (queryString != null && !queryString.isEmpty()) {
      url.append("?").append(queryString);
    }

    return url.toString();
  }

  private void logRequestHeaders(HttpServletRequest request) {
    logger.info("Request Headers:");
    request
        .getHeaderNames()
        .asIterator()
        .forEachRemaining(
            headerName -> {
              String headerValue = request.getHeader(headerName);
              logger.info("  {}: {}", headerName, headerValue);
            });
  }

  private void copyHeaders(HttpServletRequest request, HttpHeaders targetHeaders) {
    request
        .getHeaderNames()
        .asIterator()
        .forEachRemaining(
            headerName -> {
              // Skip certain headers that shouldn't be forwarded
              if (shouldForwardHeader(headerName)) {
                String headerValue = request.getHeader(headerName);
                if (headerValue != null) {
                  targetHeaders.add(headerName, headerValue);
                }
              }
            });
  }

  private static boolean shouldForwardHeader(String headerName) {
    String lowerHeaderName = headerName.toLowerCase();
    // Skip host and connection-related headers
    return !lowerHeaderName.equals("host")
        && !lowerHeaderName.equals("connection")
        && !lowerHeaderName.equals("content-length")
        && !lowerHeaderName.equals("transfer-encoding");
  }
}
