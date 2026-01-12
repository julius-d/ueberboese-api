package com.github.juliusd.ueberboeseapi.recent;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.ueberboeseapi.TestBase;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentItemRequestApiDto;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RecentServiceTest extends TestBase {

  @Autowired private RecentService recentService;

  @Test
  void addOrUpdateRecent_shouldCreateNewRecent() {
    // Given
    String accountId = "test-account";
    String deviceId = "test-device";
    RecentItemRequestApiDto request = new RecentItemRequestApiDto();
    request.setName("Test Song");
    request.setLocation("/playback/test/123");
    request.setSourceid("19989621");
    request.setContentItemType("tracklisturl");
    request.setLastplayedat(OffsetDateTime.now());

    // When
    Recent result = recentService.addOrUpdateRecent(accountId, deviceId, request);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.id()).isNotNull();
    assertThat(result.accountId()).isEqualTo(accountId);
    assertThat(result.name()).isEqualTo("Test Song");
    assertThat(result.location()).isEqualTo("/playback/test/123");
    assertThat(result.sourceId()).isEqualTo("19989621");
    assertThat(result.deviceId()).isEqualTo(deviceId);
    assertThat(result.createdOn()).isAfter(OffsetDateTime.parse("2026-01-01T10:15:30+01:00"));
    assertThat(result.lastPlayedAt()).isAfter(OffsetDateTime.parse("2026-01-01T10:15:30+01:00"));
  }

  @Test
  void addOrUpdateRecent_shouldUpdateExistingRecent() {
    // Given
    String accountId = "test-account";
    String deviceId = "device-1";
    String location = "/playback/test/123";
    String sourceId = "19989621";

    // Create initial recent
    RecentItemRequestApiDto initialRequest = new RecentItemRequestApiDto();
    initialRequest.setName("Initial Song");
    initialRequest.setLocation(location);
    initialRequest.setSourceid(sourceId);
    initialRequest.setContentItemType("tracklisturl");
    initialRequest.setLastplayedat(OffsetDateTime.now().minusDays(1));

    Recent initial = recentService.addOrUpdateRecent(accountId, deviceId, initialRequest);
    Long initialId = initial.id();
    OffsetDateTime initialCreatedOn = initial.createdOn();

    // When - update with same location and sourceId
    RecentItemRequestApiDto updateRequest = new RecentItemRequestApiDto();
    updateRequest.setName("Updated Song");
    updateRequest.setLocation(location);
    updateRequest.setSourceid(sourceId);
    updateRequest.setContentItemType("tracklisturl");
    updateRequest.setLastplayedat(OffsetDateTime.now());

    String newDeviceId = "device-2";
    Recent updated = recentService.addOrUpdateRecent(accountId, newDeviceId, updateRequest);

    // Then
    assertThat(updated.id()).isEqualTo(initialId); // Same ID
    assertThat(updated.name()).isEqualTo("Updated Song");
    assertThat(updated.deviceId()).isEqualTo(newDeviceId); // Updated to new device
    assertThat(updated.createdOn()).isEqualTo(initialCreatedOn); // Original createdOn preserved
    assertThat(updated.lastPlayedAt()).isAfter(initial.lastPlayedAt());

    // Verify only one recent exists
    List<Recent> recents = recentService.getRecents(accountId);
    assertThat(recents).hasSize(1);
  }

  @Test
  void addOrUpdateRecent_shouldKeepMaximum50Recents() {
    // Given
    String accountId = "test-account-max";
    String deviceId = "test-device";

    // Add 50 recents
    for (int i = 0; i < 50; i++) {
      RecentItemRequestApiDto request = new RecentItemRequestApiDto();
      request.setName("Song " + i);
      request.setLocation("/playback/test/" + i);
      request.setSourceid("source-" + i);
      request.setContentItemType("tracklisturl");
      request.setLastplayedat(OffsetDateTime.now().minusHours(50 - i)); // Older first
      recentService.addOrUpdateRecent(accountId, deviceId, request);
    }

    // Verify we have 50 recents
    List<Recent> recents = recentService.getRecents(accountId);
    assertThat(recents).hasSize(50);
    long countBefore = recentRepository.findAllByAccountId(accountId).size();
    assertThat(countBefore).isEqualTo(50);

    // When - add 51st recent
    RecentItemRequestApiDto request51 = new RecentItemRequestApiDto();
    request51.setName("Song 51");
    request51.setLocation("/playback/test/51");
    request51.setSourceid("source-51");
    request51.setContentItemType("tracklisturl");
    request51.setLastplayedat(OffsetDateTime.now()); // Most recent

    recentService.addOrUpdateRecent(accountId, deviceId, request51);

    // Then - should still have exactly 50 recents IN THE DATABASE
    long countAfter = recentRepository.findAllByAccountId(accountId).size();
    assertThat(countAfter)
        .as("Database should contain exactly 50 recents after adding the 51st")
        .isEqualTo(50);

    List<Recent> recentsAfter = recentService.getRecents(accountId);
    assertThat(recentsAfter).hasSize(50);

    // Verify the oldest one was deleted (Song 0 with oldest lastPlayedAt)
    assertThat(recentsAfter)
        .extracting(Recent::name)
        .doesNotContain("Song 0") // Oldest should be deleted
        .contains("Song 51"); // Newest should be present

    // Verify they are ordered by lastPlayedAt DESC (most recent first)
    assertThat(recentsAfter.get(0).name()).isEqualTo("Song 51");
  }

  @Test
  void addOrUpdateRecent_shouldDeleteMultipleOldRecentsWhenExceeded() {
    // Given
    String accountId = "test-account-multi";
    String deviceId = "test-device";

    // Add 50 recents
    for (int i = 0; i < 50; i++) {
      RecentItemRequestApiDto request = new RecentItemRequestApiDto();
      request.setName("Song " + i);
      request.setLocation("/playback/test/" + i);
      request.setSourceid("source-" + i);
      request.setContentItemType("tracklisturl");
      request.setLastplayedat(OffsetDateTime.now().minusHours(100 - i));
      recentService.addOrUpdateRecent(accountId, deviceId, request);
    }

    // When - add 5 more recents
    for (int i = 51; i <= 55; i++) {
      RecentItemRequestApiDto request = new RecentItemRequestApiDto();
      request.setName("Song " + i);
      request.setLocation("/playback/test/" + i);
      request.setSourceid("source-" + i);
      request.setContentItemType("tracklisturl");
      request.setLastplayedat(OffsetDateTime.now().plusMinutes(i - 50)); // Most recent
      recentService.addOrUpdateRecent(accountId, deviceId, request);
    }

    // Then - should have exactly 50 recents
    List<Recent> recents = recentService.getRecents(accountId);
    assertThat(recents).hasSize(50);

    // The 5 oldest should be deleted (Song 0-4)
    assertThat(recents)
        .extracting(Recent::name)
        .doesNotContain("Song 0", "Song 1", "Song 2", "Song 3", "Song 4")
        .contains("Song 51", "Song 52", "Song 53", "Song 54", "Song 55");
  }

  @Test
  void getRecents_shouldReturnRecentsOrderedByLastPlayedAtDesc() {
    // Given
    String accountId = "test-account-order";
    String deviceId = "test-device";

    // Add recents in random order
    addRecent(accountId, deviceId, "Song A", "/loc/a", "src-a", OffsetDateTime.now().minusDays(3));
    addRecent(accountId, deviceId, "Song B", "/loc/b", "src-b", OffsetDateTime.now().minusDays(1));
    addRecent(accountId, deviceId, "Song C", "/loc/c", "src-c", OffsetDateTime.now().minusDays(5));
    addRecent(accountId, deviceId, "Song D", "/loc/d", "src-d", OffsetDateTime.now());

    // When
    List<Recent> recents = recentService.getRecents(accountId);

    // Then - should be ordered by lastPlayedAt DESC
    assertThat(recents)
        .extracting(Recent::name)
        .containsExactly("Song D", "Song B", "Song A", "Song C");
  }

  @Test
  void getRecents_shouldReturnEmptyListForUnknownAccount() {
    // When
    List<Recent> recents = recentService.getRecents("unknown-account");

    // Then
    assertThat(recents).isEmpty();
  }

  @Test
  void addOrUpdateRecent_shouldNotDeleteRecentsForDifferentAccount() {
    // Given
    String accountId1 = "account-1";
    String accountId2 = "account-2";
    String deviceId = "test-device";

    // Add 50 recents to account 1
    for (int i = 0; i < 50; i++) {
      addRecent(
          accountId1,
          deviceId,
          "Song " + i,
          "/loc/" + i,
          "src-" + i,
          OffsetDateTime.now().minusHours(50 - i));
    }

    // Add 50 recents to account 2
    for (int i = 0; i < 50; i++) {
      addRecent(
          accountId2,
          deviceId,
          "Song " + i,
          "/loc/" + i,
          "src-" + i,
          OffsetDateTime.now().minusHours(50 - i));
    }

    // When - add 51st recent to account 1
    addRecent(accountId1, deviceId, "Song 51", "/loc/51", "src-51", OffsetDateTime.now());

    // Then - account 1 should have 50 recents
    assertThat(recentService.getRecents(accountId1)).hasSize(50);

    // And account 2 should still have 50 recents (unchanged)
    assertThat(recentService.getRecents(accountId2)).hasSize(50);
  }

  @Test
  void addOrUpdateRecent_uniqueConstraintTest() {
    // Given
    String accountId = "test-unique";
    String deviceId = "device-1";
    String location = "/playback/test/unique";
    String sourceId = "unique-source";

    // Add first recent
    addRecent(accountId, deviceId, "First", location, sourceId, OffsetDateTime.now().minusDays(1));

    long countBefore = recentRepository.findAllByAccountId(accountId).size();
    assertThat(countBefore).isEqualTo(1);

    // When - add with same location and sourceId but different name
    addRecent(accountId, "device-2", "Second", location, sourceId, OffsetDateTime.now());

    // Then - should still have only 1 recent (update, not insert)
    long countAfter = recentRepository.findAllByAccountId(accountId).size();
    assertThat(countAfter).isEqualTo(1);

    Optional<Recent> recent =
        recentRepository.findByAccountIdAndLocationAndSourceId(accountId, location, sourceId);
    assertThat(recent).isPresent();
    assertThat(recent.get().name()).isEqualTo("Second");
    assertThat(recent.get().deviceId()).isEqualTo("device-2");
  }

  private void addRecent(
      String accountId,
      String deviceId,
      String name,
      String location,
      String sourceId,
      OffsetDateTime lastPlayedAt) {
    RecentItemRequestApiDto request = new RecentItemRequestApiDto();
    request.setName(name);
    request.setLocation(location);
    request.setSourceid(sourceId);
    request.setContentItemType("tracklisturl");
    request.setLastplayedat(lastPlayedAt);
    recentService.addOrUpdateRecent(accountId, deviceId, request);
  }
}
