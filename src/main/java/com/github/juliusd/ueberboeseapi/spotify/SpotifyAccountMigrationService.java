package com.github.juliusd.ueberboeseapi.spotify;

import com.github.juliusd.ueberboeseapi.DataDirectoryProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * Service responsible for migrating existing JSON files to the H2 database.
 *
 * <p>This service runs automatically on application startup and scans the data directory for
 * Spotify account JSON files. It migrates them to the database while preserving the original files.
 */
@Service
@Slf4j
public class SpotifyAccountMigrationService implements ApplicationRunner {

  private static final String ACCOUNT_FILE_PATTERN = "spotify-account-.*\\.json";

  private final SpotifyAccountRepository repository;
  private final JsonMapper jsonMapper;
  private final String dataDirectory;

  public SpotifyAccountMigrationService(
      SpotifyAccountRepository repository,
      JsonMapper jsonMapper,
      DataDirectoryProperties properties) {
    this.repository = repository;
    this.jsonMapper = jsonMapper;
    this.dataDirectory = properties.dataDirectory();
  }

  @Override
  public void run(ApplicationArguments args) {
    log.info("Starting Spotify account migration from JSON files to database");

    Path directory = Path.of(dataDirectory);

    if (!Files.exists(directory)) {
      log.info("Data directory does not exist: {}. No migration needed.", directory);
      return;
    }

    try {
      migrateAccounts(directory);
    } catch (IOException e) {
      log.error("Failed to scan data directory for migration: {}", e.getMessage());
    }
  }

  /**
   * Migrates all Spotify account JSON files from the specified directory to the database.
   *
   * @param directory The directory containing the JSON files
   * @throws IOException if the directory cannot be scanned
   */
  private void migrateAccounts(Path directory) throws IOException {
    log.info("Scanning directory for Spotify account JSON files: {}", directory);

    try (Stream<Path> files = Files.list(directory)) {
      long migratedCount =
          files
              .filter(path -> path.getFileName().toString().matches(ACCOUNT_FILE_PATTERN))
              .map(this::migrateAccountFile)
              .filter(Boolean::booleanValue)
              .count();

      log.info("Migration completed. Successfully migrated {} Spotify account(s)", migratedCount);
    }
  }

  /**
   * Migrates a single Spotify account JSON file to the database.
   *
   * @param filePath The path to the JSON file
   * @return true if migration was successful or account already exists, false if migration failed
   */
  private boolean migrateAccountFile(Path filePath) {
    try {
      log.debug("Processing file: {}", filePath);

      String jsonContent = Files.readString(filePath);
      JsonSpotifyAccount jsonAccount = jsonMapper.readValue(jsonContent, JsonSpotifyAccount.class);

      // Check if account already exists in database
      if (repository.existsBySpotifyUserId(jsonAccount.spotifyUserId())) {
        log.debug("Account {} already exists in database, skipping", jsonAccount.spotifyUserId());
        return true;
      }

      // Migrate to database
      SpotifyAccount account =
          new SpotifyAccount(
              null,
              jsonAccount.spotifyUserId(),
              jsonAccount.displayName(),
              jsonAccount.refreshToken(),
              jsonAccount.createdAt(),
              jsonAccount.createdAt(), // Set updatedAt = createdAt for migrated data
              null); // version = null for new entity

      repository.save(account);
      log.info(
          "Successfully migrated Spotify account {} from {}",
          jsonAccount.spotifyUserId(),
          filePath);
      return true;

    } catch (Exception e) {
      log.error("Failed to migrate Spotify account file {}: {}", filePath, e.getMessage());
      return false;
    }
  }

  /**
   * Record for deserializing JSON files.
   *
   * <p>This is separate from the entity class to handle any potential differences in serialization
   * format.
   */
  private record JsonSpotifyAccount(
      String spotifyUserId, String displayName, String refreshToken, OffsetDateTime createdAt) {}
}
