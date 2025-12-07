package com.github.juliusd.ueberboeseapi.service;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.juliusd.ueberboeseapi.DataDirectoryProperties;
import com.github.juliusd.ueberboeseapi.XmlMessageConverterConfig;
import com.github.juliusd.ueberboeseapi.generated.dtos.FullAccountResponseApiDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AccountDataServiceTest {

  @TempDir Path tempDir;

  private AccountDataService accountDataService;
  private XmlMapper xmlMapper;

  @BeforeEach
  void setUp() {
    XmlMessageConverterConfig config = new XmlMessageConverterConfig();
    xmlMapper = config.xmlMapper();
    DataDirectoryProperties properties = new DataDirectoryProperties(tempDir.toString());
    accountDataService = new AccountDataService(xmlMapper, properties);
  }

  @Test
  void loadFullAccountData_shouldSuccessfullyLoadExistingFile() throws IOException {
    // Given
    String accountId = "6921042";
    String xmlContent =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <account id="6921042">
          <accountStatus>ACTIVE</accountStatus>
          <mode>global</mode>
          <preferredLanguage>en</preferredLanguage>
        </account>
        """;

    String filename = String.format("streaming-account-full-%s.xml", accountId);
    Path filePath = tempDir.resolve(filename);
    Files.writeString(filePath, xmlContent);

    // When
    FullAccountResponseApiDto result = accountDataService.loadFullAccountData(accountId);

    // Then
    assertNotNull(result);
    assertEquals(accountId, result.getId());
    assertEquals("ACTIVE", result.getAccountStatus());
    assertEquals("global", result.getMode());
    assertEquals("en", result.getPreferredLanguage());
  }

  @Test
  void loadFullAccountData_shouldThrowIOExceptionWhenFileDoesNotExist() {
    // Given
    String accountId = "nonexistent";

    // When & Then
    IOException exception =
        assertThrows(
            IOException.class,
            () -> {
              accountDataService.loadFullAccountData(accountId);
            });

    assertTrue(exception.getMessage().contains("Account data file not found"));
    assertTrue(exception.getMessage().contains("streaming-account-full-nonexistent.xml"));
  }

  @Test
  void loadFullAccountData_shouldThrowIOExceptionWhenFileIsNotReadable() throws IOException {
    // Given
    String accountId = "unreadable";
    String filename = String.format("streaming-account-full-%s.xml", accountId);
    Path filePath = tempDir.resolve(filename);

    // Create file but make it unreadable (this test might not work on all systems)
    Files.writeString(filePath, "<account></account>");
    filePath.toFile().setReadable(false);

    // When & Then
    try {
      IOException exception =
          assertThrows(
              IOException.class,
              () -> {
                accountDataService.loadFullAccountData(accountId);
              });

      assertTrue(
          exception.getMessage().contains("not readable")
              || exception.getMessage().contains("Failed to parse"));
    } finally {
      // Cleanup - restore readability for deletion
      filePath.toFile().setReadable(true);
    }
  }

  @Test
  void loadFullAccountData_shouldThrowIOExceptionWhenXmlIsInvalid() throws IOException {
    // Given
    String accountId = "invalid";
    String invalidXml = "<invalid><unclosed-tag></invalid>";

    String filename = String.format("streaming-account-full-%s.xml", accountId);
    Path filePath = tempDir.resolve(filename);
    Files.writeString(filePath, invalidXml);

    // When & Then
    IOException exception =
        assertThrows(
            IOException.class,
            () -> {
              accountDataService.loadFullAccountData(accountId);
            });

    assertTrue(exception.getMessage().contains("Failed to parse account data file"));
  }

  @Test
  void hasAccountData_shouldReturnTrueWhenFileExists() throws IOException {
    // Given
    String accountId = "existing";
    String filename = String.format("streaming-account-full-%s.xml", accountId);
    Path filePath = tempDir.resolve(filename);
    Files.writeString(filePath, "<account></account>");

    // When
    boolean result = accountDataService.hasAccountData(accountId);

    // Then
    assertTrue(result);
  }

  @Test
  void hasAccountData_shouldReturnFalseWhenFileDoesNotExist() {
    // Given
    String accountId = "nonexistent";

    // When
    boolean result = accountDataService.hasAccountData(accountId);

    // Then
    assertFalse(result);
  }

  @Test
  void loadFullAccountData_shouldHandleComplexXmlStructure() throws IOException {
    // Given
    String accountId = "complex";
    String complexXml =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <account id="complex">
          <accountStatus>CHANGE_PASSWORD</accountStatus>
          <mode>global</mode>
          <preferredLanguage>de</preferredLanguage>
          <devices>
            <device deviceid="TEST123">
              <name>Test Device</name>
              <ipaddress>192.168.1.100</ipaddress>
              <presets>
                <preset buttonNumber="1">
                  <name>Test Preset</name>
                  <location>/test/location</location>
                </preset>
              </presets>
            </device>
          </devices>
          <sources>
            <source id="123" type="Audio">
              <name>Test Source</name>
              <sourceproviderid>1</sourceproviderid>
            </source>
          </sources>
        </account>
        """;

    String filename = String.format("streaming-account-full-%s.xml", accountId);
    Path filePath = tempDir.resolve(filename);
    Files.writeString(filePath, complexXml);

    // When
    FullAccountResponseApiDto result = accountDataService.loadFullAccountData(accountId);

    // Then
    assertNotNull(result);
    assertEquals("complex", result.getId());
    assertEquals("CHANGE_PASSWORD", result.getAccountStatus());
    assertEquals("global", result.getMode());
    assertEquals("de", result.getPreferredLanguage());

    // Check devices
    assertNotNull(result.getDevices());
    assertNotNull(result.getDevices().getDevice());
    assertEquals(1, result.getDevices().getDevice().size());
    assertEquals("TEST123", result.getDevices().getDevice().get(0).getDeviceid());
    assertEquals("Test Device", result.getDevices().getDevice().get(0).getName());

    // Check sources
    assertNotNull(result.getSources());
    assertNotNull(result.getSources().getSource());
    assertEquals(1, result.getSources().getSource().size());
    assertEquals("123", result.getSources().getSource().get(0).getId());
    assertEquals("Test Source", result.getSources().getSource().get(0).getName());
  }

  @Test
  void saveFullAccountData_shouldSuccessfullySaveAccountData() throws IOException {
    // Given
    String accountId = "save-test";
    FullAccountResponseApiDto accountData = new FullAccountResponseApiDto();
    accountData.setId(accountId);
    accountData.setAccountStatus("ACTIVE");
    accountData.setMode("global");
    accountData.setPreferredLanguage("en");

    // When
    accountDataService.saveFullAccountData(accountId, accountData);

    // Then
    String filename = String.format("streaming-account-full-%s.xml", accountId);
    Path filePath = tempDir.resolve(filename);

    assertTrue(Files.exists(filePath), "File should be created");
    String savedContent = Files.readString(filePath);
    assertTrue(savedContent.contains("<account id=\"" + accountId + "\">"));
    assertTrue(savedContent.contains("<accountStatus>ACTIVE</accountStatus>"));
    assertTrue(savedContent.contains("<mode>global</mode>"));
    assertTrue(savedContent.contains("<preferredLanguage>en</preferredLanguage>"));
  }

  @Test
  void saveFullAccountData_shouldCreateDirectoryIfNotExists() throws IOException {
    // Given
    String accountId = "subdir-test";
    Path subDir = tempDir.resolve("nested/subdirectory");
    DataDirectoryProperties nestedProperties = new DataDirectoryProperties(subDir.toString());
    AccountDataService nestedService = new AccountDataService(xmlMapper, nestedProperties);

    FullAccountResponseApiDto accountData = new FullAccountResponseApiDto();
    accountData.setId(accountId);
    accountData.setAccountStatus("ACTIVE");
    accountData.setMode("global");
    accountData.setPreferredLanguage("en");

    // When
    nestedService.saveFullAccountData(accountId, accountData);

    // Then
    String filename = String.format("streaming-account-full-%s.xml", accountId);
    Path filePath = subDir.resolve(filename);

    assertTrue(Files.exists(subDir), "Directory should be created");
    assertTrue(Files.exists(filePath), "File should be created");
  }

  @Test
  void saveFullAccountDataRaw_shouldSaveRawXmlContent() throws IOException {
    // Given
    String accountId = "raw-save";
    String rawXml =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <account id="raw-save">
          <accountStatus>ACTIVE</accountStatus>
          <mode>global</mode>
          <preferredLanguage>de</preferredLanguage>
        </account>
        """;

    // When
    accountDataService.saveFullAccountDataRaw(accountId, rawXml);

    // Then
    String filename = String.format("streaming-account-full-%s.xml", accountId);
    Path filePath = tempDir.resolve(filename);

    assertTrue(Files.exists(filePath), "File should be created");
    String savedContent = Files.readString(filePath);
    assertEquals(rawXml, savedContent);
  }

  @Test
  void saveAndLoadRoundTrip_shouldPreserveData() throws IOException {
    // Given
    String accountId = "roundtrip";
    FullAccountResponseApiDto originalData = new FullAccountResponseApiDto();
    originalData.setId(accountId);
    originalData.setAccountStatus("CHANGE_PASSWORD");
    originalData.setMode("regional");
    originalData.setPreferredLanguage("fr");

    // When - save and then load
    accountDataService.saveFullAccountData(accountId, originalData);
    FullAccountResponseApiDto loadedData = accountDataService.loadFullAccountData(accountId);

    // Then
    assertNotNull(loadedData);
    assertEquals(originalData.getId(), loadedData.getId());
    assertEquals(originalData.getAccountStatus(), loadedData.getAccountStatus());
    assertEquals(originalData.getMode(), loadedData.getMode());
    assertEquals(originalData.getPreferredLanguage(), loadedData.getPreferredLanguage());
  }

  @Test
  void saveFullAccountDataRaw_shouldOverwriteExistingFile() throws IOException {
    // Given
    String accountId = "overwrite";
    String firstContent =
        """
        <?xml version="1.0"?>
        <account id="overwrite">
          <accountStatus>FIRST</accountStatus>
        </account>
        """;
    String secondContent =
        """
        <?xml version="1.0"?>
        <account id="overwrite">
          <accountStatus>SECOND</accountStatus>
        </account>
        """;

    // When
    accountDataService.saveFullAccountDataRaw(accountId, firstContent);
    accountDataService.saveFullAccountDataRaw(accountId, secondContent);

    // Then
    String filename = String.format("streaming-account-full-%s.xml", accountId);
    Path filePath = tempDir.resolve(filename);
    String savedContent = Files.readString(filePath);

    assertTrue(savedContent.contains("SECOND"));
    assertFalse(savedContent.contains("FIRST"));
  }
}
