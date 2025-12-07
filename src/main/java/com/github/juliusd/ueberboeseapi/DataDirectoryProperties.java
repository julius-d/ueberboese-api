package com.github.juliusd.ueberboeseapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for data directory where cached account data is stored.
 *
 * <p>This property is required and must be explicitly configured. When running in Docker, ensure
 * this directory is mounted as a volume to persist cached data across container restarts.
 *
 * <p>Example configuration in application.properties:
 *
 * <pre>
 * ueberboese.data-directory=/data
 * </pre>
 *
 * <p>Example Docker Compose volume configuration:
 *
 * <pre>
 * volumes:
 *   - ~/ueberboese-data:/data
 * environment:
 *   - UEBERBOESE_DATA_DIRECTORY=/data
 * </pre>
 */
@ConfigurationProperties(prefix = "ueberboese")
public record DataDirectoryProperties(
    /**
     * The directory path where cached account data XML files will be stored. This directory must be
     * writable by the application. For Docker deployments, mount this as a volume to ensure data
     * persistence across container restarts.
     */
    String dataDirectory) {}
