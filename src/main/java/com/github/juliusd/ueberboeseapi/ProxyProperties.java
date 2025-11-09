package com.github.juliusd.ueberboeseapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the proxy/forwarding functionality.
 * Defines the target hosts where unknown requests should be forwarded.
 */
@ConfigurationProperties(prefix = "proxy")
public record ProxyProperties(
    /**
     * The default target host URL where unknown requests should be forwarded.
     * Example: https://example.org
     */
    String targetHost,

    /**
     * The auth target host URL where auth-related requests should be forwarded.
     * If not configured, auth requests will be forwarded to the default target host.
     * Example: https://auth.example.org
     */
    String authTargetHost
) {
}