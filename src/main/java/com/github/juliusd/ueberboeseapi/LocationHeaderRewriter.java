package com.github.juliusd.ueberboeseapi;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

/**
 * Rewrites Location headers in redirect responses to use the incoming request's scheme and host
 * instead of the backend's. This ensures clients following redirects stay within the proxy's
 * domain.
 */
@Component
@Slf4j
public class LocationHeaderRewriter {

  /**
   * Rewrites the Location header in the response if it's a redirect response with an absolute URL.
   *
   * @param originalHeaders the original response headers from the backend
   * @param request the incoming HTTP request
   * @param statusCode the HTTP status code of the response
   * @return modified headers with rewritten Location, or original headers if no rewriting needed
   */
  public HttpHeaders rewriteIfRedirect(
      HttpHeaders originalHeaders, HttpServletRequest request, HttpStatusCode statusCode) {

    if (!isRedirect(statusCode)) {
      return originalHeaders;
    }

    return rewriteLocationHeader(originalHeaders, request);
  }

  private boolean isRedirect(HttpStatusCode statusCode) {
    int code = statusCode.value();
    return code >= 300 && code < 400;
  }

  private HttpHeaders rewriteLocationHeader(
      HttpHeaders originalHeaders, HttpServletRequest request) {

    List<String> locationHeaders = originalHeaders.get(HttpHeaders.LOCATION);
    if (locationHeaders == null || locationHeaders.isEmpty()) {
      return originalHeaders;
    }

    String originalLocation = locationHeaders.get(0);
    try {
      java.net.URI locationUri = java.net.URI.create(originalLocation);

      // Only rewrite if Location has a host (absolute URL)
      if (locationUri.getHost() == null) {
        return originalHeaders;
      }

      // Build new Location with request's host
      String requestScheme = request.getScheme();
      String requestHost = request.getServerName();
      int requestPort = request.getServerPort();

      // Omit port if it's the default port for the scheme
      boolean isDefaultPort =
          (requestScheme.equals("http") && requestPort == 80)
              || (requestScheme.equals("https") && requestPort == 443);

      // Build the rewritten URI
      java.net.URI rewrittenUri =
          new java.net.URI(
              requestScheme,
              null, // userInfo
              requestHost,
              isDefaultPort ? -1 : requestPort, // -1 means no port in URI
              locationUri.getPath(),
              locationUri.getQuery(),
              locationUri.getFragment());

      // Create new headers with rewritten Location
      HttpHeaders modifiedHeaders = new HttpHeaders();
      modifiedHeaders.putAll(originalHeaders);
      modifiedHeaders.set(HttpHeaders.LOCATION, rewrittenUri.toString());

      log.debug("Rewrote Location header from {} to {}", originalLocation, rewrittenUri.toString());

      return modifiedHeaders;

    } catch (java.net.URISyntaxException e) {
      log.warn("Failed to parse Location header: {}", originalLocation, e);
      return originalHeaders;
    }
  }
}
