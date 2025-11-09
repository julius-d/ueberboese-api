package com.github.juliusd.ueberboeseapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

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
     *
     * @param request the HTTP request
     * @param body the request body (optional)
     * @return ResponseEntity with the proxied response
     */
    @RequestMapping("/**")
    public ResponseEntity<String> proxyRequest(
            HttpServletRequest request,
            @RequestBody(required = false) String body) {
        return proxyService.forwardRequest(request, body);
    }
}
