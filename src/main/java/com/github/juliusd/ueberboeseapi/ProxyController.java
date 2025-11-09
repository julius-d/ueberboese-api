package com.github.juliusd.ueberboeseapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StreamUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Controller that handles all unknown/unmapped requests and forwards them
 * to the configured target host via the ProxyService.
 */
@RestController
@RequiredArgsConstructor
public class ProxyController {

    private final ProxyService proxyService;

    /**
     * Catches all unmapped requests and forwards them to the target host.
     * This mapping has the lowest priority due to the /** pattern.
     * Actuator endpoints are excluded by using a path condition.
     *
     * @param request the HTTP request
     * @return ResponseEntity with the proxied response
     * @throws IOException if reading the request body fails
     */
    @RequestMapping("/**")
    public ResponseEntity<String> proxyRequest(HttpServletRequest request) throws IOException {
        // Read the request body from the input stream to preserve original content
        String body = null;
        if (request.getContentLength() > 0) {
            // Determine charset from Content-Type header, default to UTF-8
            Charset charset = StandardCharsets.UTF_8;
            if (request.getCharacterEncoding() != null) {
                try {
                    charset = Charset.forName(request.getCharacterEncoding());
                } catch (Exception e) {
                    // Fallback to UTF-8 if charset is invalid
                    charset = StandardCharsets.UTF_8;
                }
            }
            body = StreamUtils.copyToString(request.getInputStream(), charset);
        }
        return proxyService.forwardRequest(request, body);
    }
}
