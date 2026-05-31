package com.github.juliusd.ueberboeseapi.spotify;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.ueberboeseapi.TestBase;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SpotifyAccountRepositoryTest extends TestBase {

  @Autowired private SpotifyAccountRepository repository;

  @Test
  void save_shouldSaveAccount() {
    // Given
    OffsetDateTime now = OffsetDateTime.now();
    SpotifyAccount account =
        new SpotifyAccount(null, "user123", "Test User", "refresh_token", now, now, null);

    // When
    SpotifyAccount saved = repository.save(account);

    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.spotifyUserId()).isEqualTo("user123");
  }

  @Test
  void findById_shouldReturnAccountWhenExists() {
    // Given
    OffsetDateTime now = OffsetDateTime.now();
    SpotifyAccount account =
        new SpotifyAccount(null, "user456", "Test User 2", "refresh_token", now, now, null);
    repository.save(account);

    // When
    Optional<SpotifyAccount> found = repository.findBySpotifyUserId("user456");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().spotifyUserId()).isEqualTo("user456");
    assertThat(found.get().displayName()).isEqualTo("Test User 2");
  }

  @Test
  void findById_shouldReturnEmptyWhenNotExists() {
    // When
    Optional<SpotifyAccount> found = repository.findBySpotifyUserId("nonexistent");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  void existsBySpotifyUserId_shouldReturnTrueWhenExists() {
    // Given
    OffsetDateTime now = OffsetDateTime.now();
    SpotifyAccount account =
        new SpotifyAccount(null, "user789", "Test User 3", "refresh_token", now, now, null);
    repository.save(account);

    // When
    boolean exists = repository.existsBySpotifyUserId("user789");

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  void existsBySpotifyUserId_shouldReturnFalseWhenNotExists() {
    // When
    boolean exists = repository.existsBySpotifyUserId("nonexistent");

    // Then
    assertThat(exists).isFalse();
  }

  @Test
  void findAllByOrderByCreatedAtDesc_shouldReturnAccountsInCorrectOrder() throws Exception {
    // Given
    OffsetDateTime now = OffsetDateTime.now();
    SpotifyAccount account1 = new SpotifyAccount(null, "user1", "User 1", "token1", now, now, null);
    repository.save(account1);

    OffsetDateTime now2 = now.plusSeconds(10);
    SpotifyAccount account2 =
        new SpotifyAccount(null, "user2", "User 2", "token2", now2, now2, null);
    repository.save(account2);

    OffsetDateTime now3 = now2.plusSeconds(10);
    SpotifyAccount account3 =
        new SpotifyAccount(null, "user3", "User 3", "token3", now3, now3, null);
    repository.save(account3);

    // When
    List<SpotifyAccount> accounts = repository.findAllByOrderByCreatedAtDesc();

    // Then
    assertThat(accounts).hasSize(3);
    // Should be ordered by createdAt descending (newest first)
    assertThat(accounts.get(0).spotifyUserId()).isEqualTo("user3");
    assertThat(accounts.get(1).spotifyUserId()).isEqualTo("user2");
    assertThat(accounts.get(2).spotifyUserId()).isEqualTo("user1");
  }

  @Test
  void save_shouldUpdateExistingAccount() {
    // Given
    OffsetDateTime now = OffsetDateTime.now();
    SpotifyAccount original =
        new SpotifyAccount(null, "user_update", "Original Name", "token1", now, now, null);
    SpotifyAccount saved = repository.save(original);

    // When - save with same ID but different data
    OffsetDateTime now2 = OffsetDateTime.now();
    SpotifyAccount updated =
        new SpotifyAccount(
            null, "user_update", "Updated Name", "token2", now, now2, saved.version());
    repository.save(updated);

    // Then
    Optional<SpotifyAccount> found = repository.findBySpotifyUserId("user_update");
    assertThat(found).isPresent();
    assertThat(found.get().displayName()).isEqualTo("Updated Name");
    assertThat(found.get().refreshToken()).isEqualTo("token2");
  }
}
