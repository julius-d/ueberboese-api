package com.github.juliusd.ueberboeseapi;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ProxyService {

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
  public ResponseEntity<byte[]> forwardRequest(HttpServletRequest request, String requestBody) {
    String targetUrl = buildTargetUrl(request);
    HttpMethod method = HttpMethod.valueOf(request.getMethod());
    boolean isSoftwareUpdate = isSoftwareUpdateRequest(request);

    // Build consolidated request log message
    StringBuilder requestLog = new StringBuilder("\n=== PROXY REQUEST START ===");
    requestLog.append("\n  Original URL: ").append(request.getRequestURL());
    requestLog.append("\n  Target URL: ").append(targetUrl);
    requestLog.append("\n  Method: ").append(method);
    if (request.getQueryString() != null) {
      requestLog.append("\n  Content-Type: ").append(request.getContentType());
    }
    if (request.getContentLength() != -1) {
      requestLog.append("\n  Content-Length: ").append(request.getContentLength());
    }
    requestLog.append("\n  Request Headers:").append(buildHeadersString(request));
    if (requestBody != null && !requestBody.isEmpty()) {
      requestLog.append("\n  Request Body: ").append(requestBody);
    }

    log.info(requestLog.toString());

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
        log.error("Received null response from target");
        return ResponseEntity.status(502).body("Bad Gateway - No response from target".getBytes());
      }

      // Get response body as bytes
      byte[] responseBodyBytes = clientResponse.bodyToMono(byte[].class).block();
      String responseBodyString = responseBodyBytes != null ? new String(responseBodyBytes) : null;

      // Build consolidated response log message
      StringBuilder responseLog = new StringBuilder("\n=== PROXY RESPONSE ===");
      responseLog.append("\n  Status: ").append(clientResponse.statusCode().value());
      responseLog
          .append("\n  Response Headers:")
          .append(buildResponseHeadersString(clientResponse.headers().asHttpHeaders()));
      if (responseBodyString != null) {
        responseLog.append("\n  Response Body: ").append(responseBodyString);
      }
      responseLog.append("\n=== PROXY REQUEST END ===");

      log.info(responseLog.toString());

      // For software update requests, return 404 instead of forwarding the response
      if (isSoftwareUpdate) {
        log.info("Returning 404 for software update request");
        return ResponseEntity.notFound().build();
      }

      // Build response entity
      return ResponseEntity.status(clientResponse.statusCode())
          .headers(clientResponse.headers().asHttpHeaders())
          .body(responseBodyBytes);

    } catch (WebClientResponseException e) {
      String errorLog =
          String.format(
              "\n=== PROXY ERROR ===\n  Error forwarding request to %s: %s %s\n  Error response"
                  + " body: %s\n=== PROXY REQUEST END ===",
              targetUrl, e.getStatusCode(), e.getStatusText(), e.getResponseBodyAsString());
      log.error(errorLog);

      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString().getBytes());

    } catch (Exception e) {
      String errorLog =
          String.format(
              "\n=== PROXY ERROR ===\n  Unexpected error forwarding request to %s\n=== PROXY REQUEST"
                  + " END ===",
              targetUrl);
      log.error(errorLog, e);

      return ResponseEntity.status(502).body("Bad Gateway - Error forwarding request".getBytes());
    }
  }

  private String buildTargetUrl(HttpServletRequest request) {
    String targetHost = determineTargetHost(request);
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

  private String determineTargetHost(HttpServletRequest request) {
    boolean isAuthRequest = isAuthRelatedRequest(request);
    if (isAuthRequest
        && proxyProperties.authTargetHost() != null
        && !proxyProperties.authTargetHost().isEmpty()) {
      return proxyProperties.authTargetHost();
    }

    boolean isSoftwareUpdateRequest = isSoftwareUpdateRequest(request);
    if (isSoftwareUpdateRequest
        && proxyProperties.softwareUpdateTargetHost() != null
        && !proxyProperties.softwareUpdateTargetHost().isEmpty()) {
      return proxyProperties.softwareUpdateTargetHost();
    }

    // Default target for all other requests
    return proxyProperties.targetHost();
  }

  private boolean isAuthRelatedRequest(HttpServletRequest request) {
    String hostHeader = request.getHeader("Host");
    if (hostHeader == null) {
      return false;
    }

    return hostHeader.toLowerCase().contains("auth");
  }

  private boolean isSoftwareUpdateRequest(HttpServletRequest request) {
    String hostHeader = request.getHeader("Host");
    if (hostHeader == null) {
      return false;
    }

    return hostHeader.toLowerCase().contains("downloads");
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

  private static String buildHeadersString(HttpServletRequest request) {
    StringBuilder headers = new StringBuilder();
    request
        .getHeaderNames()
        .asIterator()
        .forEachRemaining(
            headerName -> {
              String headerValue = request.getHeader(headerName);
              headers.append("\n    ").append(headerName).append(": ").append(headerValue);
            });
    return headers.toString();
  }

  private static String buildResponseHeadersString(HttpHeaders headers) {
    StringBuilder result = new StringBuilder();
    headers.forEach(
        (name, values) ->
            result.append("\n    ").append(name).append(": ").append(String.join(", ", values)));
    return result.toString();
  }
}
