package com.github.juliusd.ueberboeseapi.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * Filter that logs raw request bodies for event endpoints.
 *
 * <p>This filter wraps requests with ContentCachingRequestWrapper to allow reading the request body
 * multiple times - once for logging and once for Spring's normal request processing.
 */
@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    // Always log request method and URI with query parameters
    String uri = request.getRequestURI();
    String queryString = request.getQueryString();
    String fullUri = queryString != null ? uri + "?" + queryString : uri;
    log.info("Request: {} {}", request.getMethod(), fullUri);

    // Only wrap and log for event endpoints
    if (request.getRequestURI().matches(".*/v1/scmudc/.*")) {
      // Wrap request with content size limit of 1MB
      ContentCachingRequestWrapper wrappedRequest =
          new ContentCachingRequestWrapper(request, 1024 * 1024);

      // Continue with the filter chain
      filterChain.doFilter(wrappedRequest, response);

      // Log the raw request body after the request has been processed
      byte[] content = wrappedRequest.getContentAsByteArray();
      if (content.length > 0) {
        String rawBody = new String(content, StandardCharsets.UTF_8);
        log.info("Raw request body for {}: {}", request.getRequestURI(), rawBody.trim());
      }
    } else {
      // For non-event endpoints, proceed normally without wrapping
      filterChain.doFilter(request, response);
    }
  }
}
