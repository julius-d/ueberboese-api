package com.github.juliusd.ueberboeseapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the proxy/forwarding functionality. Defines the target hosts where
 * unknown requests should be forwarded.
 */
@ConfigurationProperties(prefix = "proxy")
public record ProxyProperties(
    /**
     * The default target host URL where unknown requests should be forwarded. Example:
     * https://example.org
     */
    String targetHost,

    /**
     * The auth target host URL where auth-related requests should be forwarded. If not configured,
     * auth requests will be forwarded to the default target host. Example: https://auth.example.org
     */
    String authTargetHost,

    /**
     * The software update target host URL where software update requests (Host header contains
     * "downloads") should be forwarded. If not configured, software update requests will be
     * forwarded to the default target host. Example: https://downloads.example.org
     */
    String softwareUpdateTargetHost,

    /**
     * The stats target host URL where stats-related requests (Host header contains "stats") should
     * be forwarded. If not configured, stats requests will be forwarded to the default target host.
     * Example: https://events.api.bosecm.com
     */
    String statsTargetHost,

    /**
     * The BMX registry target host URL where BMX registry requests (Host header contains "bmx")
     * should be forwarded. If not configured, BMX registry requests will be forwarded to the
     * default target host. Example: https://content.api.bose.io
     */
    String bmxRegistryHost) {}
