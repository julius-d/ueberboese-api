package com.github.juliusd.ueberboeseapi.spotify;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpotifyAccountService {

  private final SpotifyAccountRepository repository;

  /**
   * Saves a Spotify account after successful OAuth authentication.
   *
   * <p>Note: This method performs an upsert (insert or update). If an account with the same
   * spotifyUserId already exists, it will be updated with the new values.
   *
   * @param spotifyUserId The Spotify user ID
   * @param displayName The user's display name from Spotify
   * @param refreshToken The refresh token
   * @return The accountId (same as spotifyUserId for simplicity)
   */
  public String saveAccount(String spotifyUserId, String displayName, String refreshToken) {
    log.debug("Attempting to save Spotify account for userId: {}", spotifyUserId);

    Optional<SpotifyAccount> existing = repository.findBySpotifyUserId(spotifyUserId);

    SpotifyAccount accountToSave;
    if (existing.isPresent()) {
      accountToSave =
          new SpotifyAccount(
              existing.get().id(),
              spotifyUserId,
              displayName,
              refreshToken,
              existing.get().createdAt(),
              OffsetDateTime.now(),
              existing.get().version());
    } else {
      accountToSave =
          new SpotifyAccount(
              null,
              spotifyUserId,
              displayName,
              refreshToken,
              OffsetDateTime.now(),
              OffsetDateTime.now(),
              null);
    }

    repository.save(accountToSave);

    log.info("Successfully saved Spotify account for accountId: {}", spotifyUserId);
    return spotifyUserId;
  }

  /**
   * Updates the refresh token and updatedAt timestamp for an existing Spotify account, but ONLY if
   * the refresh token has actually changed.
   *
   * @param spotifyUserId The Spotify user ID
   * @param newRefreshToken The new refresh token to store
   */
  public void updateRefreshToken(String spotifyUserId, String newRefreshToken) {
    log.debug("Checking refresh token update requirements for userId: {}", spotifyUserId);

    repository
        .findBySpotifyUserId(spotifyUserId)
        .ifPresentOrElse(
            existingAccount -> {
              // Check if the token has actually changed
              if (existingAccount.refreshToken().equals(newRefreshToken)) {
                log.debug(
                    "Refresh token for userId: {} has not changed. Skipping database write to optimize performance.",
                    spotifyUserId);
                return;
              }

              // If changed (or rotated by Spotify), persist the new token
              OffsetDateTime now = OffsetDateTime.now();
              SpotifyAccount updatedAccount =
                  new SpotifyAccount(
                      null,
                      existingAccount.spotifyUserId(),
                      existingAccount.displayName(),
                      newRefreshToken,
                      existingAccount.createdAt(),
                      now,
                      existingAccount.version());

              repository.save(updatedAccount);
              log.info(
                  "Refresh token rotated! Successfully updated Spotify refresh token in database for userId: {}",
                  spotifyUserId);
            },
            () ->
                log.warn(
                    "Key rotation skipped: No existing Spotify account found for userId: {}",
                    spotifyUserId));
  }

  /**
   * Retrieves a Spotify account by Spotify user ID.
   *
   * @param spotifyUserId The Spotify user ID
   * @return Optional containing the account if found
   */
  public Optional<SpotifyAccount> getAccountBySpotifyUserId(String spotifyUserId) {
    log.debug("Attempting to load Spotify account for userId: {}", spotifyUserId);

    Optional<SpotifyAccount> account = repository.findBySpotifyUserId(spotifyUserId);

    if (account.isPresent()) {
      log.debug("Successfully loaded Spotify account for accountId: {}", spotifyUserId);
    } else {
      log.debug("Spotify account not found for userId: {}", spotifyUserId);
    }

    return account;
  }

  /**
   * Checks if a Spotify account exists for the given user ID.
   *
   * @param spotifyUserId The Spotify user ID
   * @return true if the account exists, false otherwise
   */
  public boolean accountExists(String spotifyUserId) {
    return repository.existsBySpotifyUserId(spotifyUserId);
  }

  /**
   * Lists all stored Spotify accounts.
   *
   * @return List of all Spotify accounts, sorted by createdAt descending (newest first)
   */
  public List<SpotifyAccount> listAllAccounts() {
    log.debug("Listing all Spotify accounts from database");

    List<SpotifyAccount> accounts = repository.findAllByOrderByCreatedAtDesc();

    log.debug("Found {} Spotify account(s)", accounts.size());
    return accounts;
  }
}
