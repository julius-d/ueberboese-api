package com.github.juliusd.ueberboeseapi.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.juliusd.ueberboeseapi.DataDirectoryProperties;
import com.github.juliusd.ueberboeseapi.generated.dtos.FullAccountResponseApiDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountDataService {
  private static final String ACCOUNT_FILE_PATTERN = "streaming-account-full-%s.xml";

  private final XmlMapper xmlMapper;
  private final String dataDirectory;

  public AccountDataService(XmlMapper xmlMapper, DataDirectoryProperties properties) {
    this.xmlMapper = xmlMapper;
    this.dataDirectory = properties.dataDirectory();
    log.info("AccountDataService initialized with data directory: {}", dataDirectory);
  }

  /**
   * Validates that an account ID contains only safe characters.
   *
   * @param accountId The account ID to validate
   * @throws IllegalArgumentException if the account ID is null, empty, or contains invalid
   *     characters
   */
  private static void validateAccountId(String accountId) {
    if (accountId == null || accountId.isEmpty()) {
      throw new IllegalArgumentException("Account ID must not be null or empty");
    }

    if (!accountId.matches("^[0-9a-zA-Z-]+$")) {
      throw new IllegalArgumentException(
          "Account ID contains invalid characters. Only alphanumeric characters and hyphens are allowed: "
              + accountId);
    }
  }

  /**
   * Constructs the file path for an account data file.
   *
   * @param accountId The account ID
   * @return The complete file path
   */
  private Path getAccountFilePath(String accountId) {
    String filename = ACCOUNT_FILE_PATTERN.formatted(accountId);
    return Path.of(dataDirectory, filename);
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
   * Loads the full account data from an XML file for the given account ID.
   *
   * @param accountId The account ID to load data for
   * @return The parsed FullAccountResponseApiDto object
   * @throws IOException if the file doesn't exist or cannot be parsed
   */
  public FullAccountResponseApiDto loadFullAccountData(String accountId) throws IOException {
    validateAccountId(accountId);
    Path filePath = getAccountFilePath(accountId);

    log.debug("Attempting to load account data from: {}", filePath);

    if (!Files.exists(filePath)) {
      throw new IOException("Account data file not found: " + filePath);
    }

    if (!Files.isReadable(filePath)) {
      throw new IOException("Account data file is not readable: " + filePath);
    }

    try {
      String xmlContent = Files.readString(filePath);
      log.debug("Successfully read {} bytes from {}", xmlContent.length(), filePath);

      FullAccountResponseApiDto accountData =
          xmlMapper.readValue(xmlContent, FullAccountResponseApiDto.class);
      log.info("Successfully parsed account data for accountId: {}", accountId);

      return accountData;
    } catch (Exception e) {
      log.error("Failed to parse XML file {}: {}", filePath, e.getMessage());
      throw new IOException("Failed to parse account data file: " + filePath, e);
    }
  }

  /**
   * Checks if account data file exists for the given account ID.
   *
   * @param accountId The account ID to check
   * @return true if the file exists, false otherwise
   */
  public boolean hasAccountData(String accountId) {
    validateAccountId(accountId);
    Path filePath = getAccountFilePath(accountId);
    return Files.exists(filePath);
  }

  /**
   * Saves the full account data to an XML file for the given account ID.
   *
   * @param accountId The account ID to save data for
   * @param accountData The account data to save
   * @throws IOException if the file cannot be written
   */
  public void saveFullAccountData(String accountId, FullAccountResponseApiDto accountData)
      throws IOException {
    validateAccountId(accountId);
    Path filePath = getAccountFilePath(accountId);
    ensureDirectoryExists(filePath);

    log.debug("Attempting to save account data to: {}", filePath);

    try {
      String xmlContent = xmlMapper.writeValueAsString(accountData);
      Files.writeString(filePath, xmlContent);
      log.info("Successfully saved account data for accountId: {} to {}", accountId, filePath);
    } catch (Exception e) {
      log.error("Failed to save account data to file {}: {}", filePath, e.getMessage());
      throw new IOException("Failed to save account data file: " + filePath, e);
    }
  }

  /**
   * Saves raw XML content to a file for the given account ID.
   *
   * @param accountId The account ID to save data for
   * @param xmlContent The raw XML content to save
   * @throws IOException if the file cannot be written
   */
  public void saveFullAccountDataRaw(String accountId, String xmlContent) throws IOException {
    validateAccountId(accountId);
    Path filePath = getAccountFilePath(accountId);
    ensureDirectoryExists(filePath);

    log.debug("Attempting to save raw XML content to: {}", filePath);

    try {
      Files.writeString(filePath, xmlContent);
      log.info("Successfully saved raw XML content for accountId: {} to {}", accountId, filePath);
    } catch (Exception e) {
      log.error("Failed to save raw XML content to file {}: {}", filePath, e.getMessage());
      throw new IOException("Failed to save account data file: " + filePath, e);
    }
  }
}
