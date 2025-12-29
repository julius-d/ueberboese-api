package com.github.juliusd.ueberboeseapi.device;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.ueberboeseapi.TestBase;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DeviceRepositoryTest extends TestBase {

  @Autowired private DeviceRepository repository;

  @Test
  void save_shouldSaveDevice() {
    // Given
    OffsetDateTime now = OffsetDateTime.now();
    Device device = new Device("device123", "192.168.1.1", now, now, null);

    // When
    Device saved = repository.save(device);

    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.deviceId()).isEqualTo("device123");
  }

  @Test
  void findById_shouldReturnDeviceWhenExists() {
    // Given
    OffsetDateTime now = OffsetDateTime.now();
    Device device = new Device("device456", "192.168.1.2", now, now, null);
    repository.save(device);

    // When
    Optional<Device> found = repository.findById("device456");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().deviceId()).isEqualTo("device456");
    assertThat(found.get().ipAddress()).isEqualTo("192.168.1.2");
  }

  @Test
  void findById_shouldReturnEmptyWhenNotExists() {
    // When
    Optional<Device> found = repository.findById("nonexistent");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  void findAllByOrderByLastSeenDesc_shouldReturnDevicesInCorrectOrder() throws Exception {
    // Given
    OffsetDateTime now = OffsetDateTime.now();
    Device device1 = new Device("device1", "192.168.1.1", now, now, null);
    repository.save(device1);

    OffsetDateTime now2 = now.plusSeconds(10);
    Device device2 = new Device("device2", "192.168.1.2", now2, now2, null);
    repository.save(device2);

    OffsetDateTime now3 = now2.plusSeconds(10);
    Device device3 = new Device("device3", "192.168.1.3", now3, now3, null);
    repository.save(device3);

    // When
    List<Device> devices = repository.findAllByOrderByLastSeenDesc();

    // Then
    assertThat(devices).hasSize(3);
    // Should be ordered by lastSeen descending (newest first)
    assertThat(devices.get(0).deviceId()).isEqualTo("device3");
    assertThat(devices.get(1).deviceId()).isEqualTo("device2");
    assertThat(devices.get(2).deviceId()).isEqualTo("device1");
  }

  @Test
  void save_shouldUpdateExistingDevice() {
    // Given
    OffsetDateTime now = OffsetDateTime.now();
    Device original = new Device("device_update", "192.168.1.1", now, now, null);
    Device saved = repository.save(original);

    // When - save with same ID but different data
    OffsetDateTime now2 = OffsetDateTime.now();
    Device updated = new Device("device_update", "192.168.1.2", now, now2, saved.version());
    repository.save(updated);

    // Then
    Optional<Device> found = repository.findById("device_update");
    assertThat(found).isPresent();
    assertThat(found.get().ipAddress()).isEqualTo("192.168.1.2");
    assertThat(found.get().lastSeen()).isAfter(now);
  }

  @Test
  void save_shouldPreserveFirstSeenWhenUpdating() {
    // Given
    OffsetDateTime firstSeen = OffsetDateTime.now().minusDays(1);
    OffsetDateTime lastSeen = OffsetDateTime.now();
    Device original = new Device("device_preserve", "192.168.1.1", firstSeen, lastSeen, null);
    Device saved = repository.save(original);

    // When - update with new lastSeen
    OffsetDateTime newLastSeen = OffsetDateTime.now().plusHours(1);
    Device updated =
        new Device("device_preserve", "192.168.1.2", firstSeen, newLastSeen, saved.version());
    repository.save(updated);

    // Then
    Optional<Device> found = repository.findById("device_preserve");
    assertThat(found).isPresent();
    assertThat(found.get().firstSeen()).isEqualTo(firstSeen);
    assertThat(found.get().lastSeen()).isEqualTo(newLastSeen);
  }
}
