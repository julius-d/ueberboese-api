package com.github.juliusd.ueberboeseapi.spotify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.juliusd.ueberboeseapi.DataDirectoryProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SpotifyAccountService {
  private static final String ACCOUNT_FILE_PATTERN = "spotify-account-%s.json";

  private final ObjectMapper objectMapper;
  private final String dataDirectory;

  public SpotifyAccountService(ObjectMapper objectMapper, DataDirectoryProperties properties) {
    this.objectMapper = objectMapper;
    this.dataDirectory = properties.dataDirectory();
    log.info("SpotifyAccountService initialized with data directory: {}", dataDirectory);
  }

  /**
   * Constructs the file path for a Spotify account data file.
   *
   * @param accountId The account ID
   * @return The complete file path
   */
  private Path getAccountFilePath(String accountId) {
    String filename = String.format(ACCOUNT_FILE_PATTERN, accountId);
    return Paths.get(dataDirectory, filename);
  }

  /**
   * Ensures the data directory exists, creating it if necessary.
   *
   * @param filePath The file path whose parent directory should exist
   * @throws IOException if the directory cannot be created
   */
  private void ensureDirectoryExists(Path filePath) throws IOException {
    Path directory = filePath.getParent();
    if (directory != null && !Files.exists(directory)) {
      log.info("Creating data directory: {}", directory);
      Files.createDirectories(directory);
    }
  }

  /**
   * Saves a Spotify account after successful OAuth authentication.
   *
   * @param spotifyUserId The Spotify user ID
   * @param displayName The user's display name from Spotify
   * @param refreshToken The refresh token
   * @return The accountId (same as spotifyUserId for simplicity)
   * @throws IOException if the file cannot be written
   */
  public String saveAccount(String spotifyUserId, String displayName, String refreshToken)
      throws IOException {
    // Use Spotify user ID as the account ID
    String accountId = spotifyUserId;
    Path filePath = getAccountFilePath(accountId);
    ensureDirectoryExists(filePath);

    log.debug("Attempting to save Spotify account to: {}", filePath);

    SpotifyAccount account =
        new SpotifyAccount(spotifyUserId, displayName, refreshToken, OffsetDateTime.now());

    try {
      String jsonContent = objectMapper.writeValueAsString(account);
      Files.writeString(filePath, jsonContent);
      log.info("Successfully saved Spotify account for accountId: {} to {}", accountId, filePath);
      return accountId;
    } catch (Exception e) {
      log.error("Failed to save Spotify account to file {}: {}", filePath, e.getMessage());
      throw new IOException("Failed to save Spotify account file: " + filePath, e);
    }
  }

  /**
   * Retrieves a Spotify account by Spotify user ID.
   *
   * @param spotifyUserId The Spotify user ID
   * @return Optional containing the account if found
   */
  public Optional<SpotifyAccount> getAccountBySpotifyUserId(String spotifyUserId) {
    Path filePath = getAccountFilePath(spotifyUserId);

    log.debug("Attempting to load Spotify account from: {}", filePath);

    if (!Files.exists(filePath)) {
      log.debug("Spotify account file not found: {}", filePath);
      return Optional.empty();
    }

    try {
      String jsonContent = Files.readString(filePath);
      SpotifyAccount account = objectMapper.readValue(jsonContent, SpotifyAccount.class);
      log.info("Successfully loaded Spotify account for accountId: {}", spotifyUserId);
      return Optional.of(account);
    } catch (Exception e) {
      log.error("Failed to parse Spotify account file {}: {}", filePath, e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Checks if a Spotify account exists for the given user ID.
   *
   * @param spotifyUserId The Spotify user ID
   * @return true if the account exists, false otherwise
   */
  public boolean accountExists(String spotifyUserId) {
    Path filePath = getAccountFilePath(spotifyUserId);
    return Files.exists(filePath);
  }

  /**
   * Lists all stored Spotify accounts.
   *
   * @return List of all Spotify accounts, sorted by createdAt descending (newest first)
   * @throws IOException if the directory cannot be read
   */
  public List<SpotifyAccount> listAllAccounts() throws IOException {
    Path directory = Paths.get(dataDirectory);

    if (!Files.exists(directory)) {
      log.debug("Data directory does not exist: {}", directory);
      return List.of();
    }

    log.debug("Listing all Spotify accounts from directory: {}", directory);

    try (Stream<Path> files = Files.list(directory)) {
      List<SpotifyAccount> accounts =
          files
              .filter(path -> path.getFileName().toString().matches("spotify-account-.*\\.json"))
              .map(this::parseAccountFile)
              .filter(Optional::isPresent)
              .map(Optional::get)
              .sorted(Comparator.comparing(SpotifyAccount::createdAt).reversed())
              .toList();

      log.info("Found {} Spotify account(s)", accounts.size());
      return accounts;
    }
  }

  /**
   * Parses a Spotify account file.
   *
   * @param filePath The path to the account file
   * @return Optional containing the account if successfully parsed, empty otherwise
   */
  private Optional<SpotifyAccount> parseAccountFile(Path filePath) {
    try {
      String jsonContent = Files.readString(filePath);
      SpotifyAccount account = objectMapper.readValue(jsonContent, SpotifyAccount.class);
      return Optional.of(account);
    } catch (Exception e) {
      log.error("Failed to parse Spotify account file {}: {}", filePath, e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Record representing a stored Spotify account.
   *
   * @param spotifyUserId The Spotify user ID
   * @param displayName The user's display name from Spotify
   * @param refreshToken The refresh token
   * @param createdAt When the account was created
   */
  public record SpotifyAccount(
      String spotifyUserId, String displayName, String refreshToken, OffsetDateTime createdAt) {}
}
