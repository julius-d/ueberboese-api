package com.github.juliusd.ueberboeseapi.bmx.report;

import com.github.juliusd.ueberboeseapi.bmx.BmxProperties;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RadioReportStorageService {

  private final BmxProperties properties;

  private final LinkedHashMap<String, RadioSession> sessionsByListenId = new LinkedHashMap<>();
  private final LinkedHashMap<String, List<RadioReportEvent>> reportsByListenId =
      new LinkedHashMap<>();

  public synchronized void startSession(
      String listenId,
      String stationId,
      String stationName,
      String logoUrl,
      OffsetDateTime startedAt) {
    sessionsByListenId.put(
        listenId, new RadioSession(listenId, stationId, stationName, logoUrl, startedAt));
    evictOldestIfNeeded();
  }

  public synchronized void store(String listenId, RadioReportEvent event) {
    reportsByListenId.computeIfAbsent(listenId, k -> new ArrayList<>()).add(event);
    evictOldestIfNeeded();
  }

  public synchronized List<RadioSessionReport> getSessionReports() {
    var allListenIds = new LinkedHashSet<>(sessionsByListenId.keySet());
    allListenIds.addAll(reportsByListenId.keySet());
    return allListenIds.stream()
        .map(
            id ->
                new RadioSessionReport(
                    id,
                    sessionsByListenId.get(id),
                    List.copyOf(reportsByListenId.getOrDefault(id, List.of()))))
        .toList();
  }

  public synchronized Optional<RadioSession> getSession(String listenId) {
    return Optional.ofNullable(sessionsByListenId.get(listenId));
  }

  public synchronized void clearAll() {
    sessionsByListenId.clear();
    reportsByListenId.clear();
  }

  private void evictOldestIfNeeded() {
    reportsByListenId.keySet().removeIf(id -> !sessionsByListenId.containsKey(id));
    int limit = properties.maxReports();
    while (sessionsByListenId.size() > limit) {
      String oldest = sessionsByListenId.firstEntry().getKey();
      sessionsByListenId.remove(oldest);
      reportsByListenId.remove(oldest);
    }
  }
}
