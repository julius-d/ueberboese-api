package com.github.juliusd.ueberboeseapi.recent;

import com.github.juliusd.ueberboeseapi.generated.dtos.CredentialApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentItemApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentItemRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceApiDto;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecentService {
  private static final int MAX_RECENTS_PER_ACCOUNT = 50;

  private final RecentRepository recentRepository;

  @Transactional
  public Recent addOrUpdateRecent(
      String accountId, String deviceId, RecentItemRequestApiDto request) {
    // Check if recent exists (by accountId, location, sourceId)
    Optional<Recent> existing =
        recentRepository.findByAccountIdAndLocationAndSourceId(
            accountId, request.getLocation(), request.getSourceid());
    var now = OffsetDateTime.now().withNano(0);

    Recent saved;
    if (existing.isPresent()) {
      // Update existing recent
      Recent current = existing.get();
      Recent updated =
          Recent.builder()
              .id(current.id())
              .accountId(accountId)
              .name(request.getName())
              .location(request.getLocation())
              .sourceId(request.getSourceid())
              .contentItemType(request.getContentItemType())
              .deviceId(deviceId) // Update to latest device
              .lastPlayedAt(request.getLastplayedat())
              .createdOn(current.createdOn()) // Keep original createdOn
              .updatedOn(now)
              .version(current.version())
              .build();
      saved = recentRepository.save(updated);
      log.info("Updated existing recent id={} for account={}", saved.id(), accountId);
    } else {
      // Create new recent
      Recent recent =
          Recent.builder()
              .id(null) // Auto-generated
              .accountId(accountId)
              .name(request.getName())
              .location(request.getLocation())
              .sourceId(request.getSourceid())
              .contentItemType(request.getContentItemType())
              .deviceId(deviceId)
              .lastPlayedAt(request.getLastplayedat())
              .createdOn(now)
              .updatedOn(now)
              .version(null) // Let Spring Data JDBC manage the version
              .build();
      saved = recentRepository.save(recent);
      log.info("Created new recent id={} for account={}", saved.id(), accountId);

      // Check if we exceeded max recents per account and cleanup if needed
      cleanupOldRecents(accountId);
    }

    return saved;
  }

  private void cleanupOldRecents(String accountId) {
    // Get all recents ordered by lastPlayedAt DESC
    List<Recent> allRecents = recentRepository.findAllByAccountId(accountId);

    // Keep only the first MAX_RECENTS_PER_ACCOUNT (50) items, delete the rest
    if (allRecents.size() > MAX_RECENTS_PER_ACCOUNT) {
      int deleted = 0;
      for (int i = MAX_RECENTS_PER_ACCOUNT; i < allRecents.size(); i++) {
        recentRepository.deleteByAccountIdAndId(accountId, allRecents.get(i).id());
        deleted++;
      }
      log.info("Cleaned up old recents for account={}, deleted={}", accountId, deleted);
    }
  }

  public List<Recent> getRecents(String accountId) {
    List<Recent> allRecents = recentRepository.findAllByAccountId(accountId);
    // Return at most 50 recents
    return allRecents.stream().limit(MAX_RECENTS_PER_ACCOUNT).toList();
  }

  public List<RecentItemApiDto> convertToApiDtos(List<Recent> recents) {
    return recents.stream().map(this::convertToApiDto).toList();
  }

  private RecentItemApiDto convertToApiDto(Recent recent) {
    // Create mock credential and source based on sourceId
    CredentialApiDto credential = new CredentialApiDto();
    SourceApiDto source = new SourceApiDto();
    source.setId(recent.sourceId());
    source.setType("Audio");
    source.setCreatedOn(OffsetDateTime.parse("2018-08-11T08:55:28.000+00:00"));
    source.setUpdatedOn(OffsetDateTime.parse("2019-07-20T17:48:31.000+00:00"));

    // Set source-specific mock data based on sourceId
    if ("19989643".equals(recent.sourceId())) {
      // Spotify source (user1namespot)
      credential.setType("token_version_3");
      credential.setValue("mockTokenUser2");
      source.setCredential(credential);
      source.setName("user1namespot");
      source.setSourceproviderid("15");
      source.setSourcename("user1@example.org");
      source.setUsername("user1namespot");
    } else if ("19989342".equals(recent.sourceId())) {
      // TuneIn source
      credential.setType("token");
      credential.setValue("eyJduTune=");
      source.setCredential(credential);
      source.setName("");
      source.setSourceproviderid("25");
      source.setSourcename("");
      source.setUsername("");
    } else {
      // Default source
      credential.setType("token");
      credential.setValue("eyDu=");
      source.setCredential(credential);
      source.setName("");
      source.setSourceproviderid("25");
      source.setSourcename("");
      source.setUsername("");
    }

    // Create recent item
    RecentItemApiDto recentItem = new RecentItemApiDto();
    recentItem.setId(String.valueOf(recent.id()));
    recentItem.setContentItemType(recent.contentItemType());
    recentItem.setCreatedOn(recent.createdOn());
    recentItem.setLastplayedat(recent.lastPlayedAt());
    recentItem.setLocation(recent.location());
    recentItem.setName(recent.name());
    recentItem.setSource(source);
    recentItem.setSourceid(recent.sourceId());
    recentItem.setUpdatedOn(recent.updatedOn());

    return recentItem;
  }
}
