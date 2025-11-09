package com.github.juliusd.ueberboeseapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the proxy/forwarding functionality.
 * Defines the target host where unknown requests should be forwarded.
 */
@ConfigurationProperties(prefix = "proxy")
public record ProxyProperties(
    /**
     * The target host URL where unknown requests should be forwarded.
     * Example: https://example.org
     */
    String targetHost
) {
}