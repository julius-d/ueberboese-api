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
    Device device =
        Device.builder()
            .deviceId("device123")
            .ipAddress("192.168.1.1")
            .firstSeen(now)
            .lastSeen(now)
            .version(null)
            .build();

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
    Device device =
        Device.builder()
            .deviceId("device456")
            .ipAddress("192.168.1.2")
            .firstSeen(now)
            .lastSeen(now)
            .version(null)
            .build();
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
    Device device1 =
        Device.builder()
            .deviceId("device1")
            .ipAddress("192.168.1.1")
            .firstSeen(now)
            .lastSeen(now)
            .version(null)
            .build();
    repository.save(device1);

    OffsetDateTime now2 = now.plusSeconds(10);
    Device device2 =
        Device.builder()
            .deviceId("device2")
            .ipAddress("192.168.1.2")
            .firstSeen(now2)
            .lastSeen(now2)
            .version(null)
            .build();
    repository.save(device2);

    OffsetDateTime now3 = now2.plusSeconds(10);
    Device device3 =
        Device.builder()
            .deviceId("device3")
            .ipAddress("192.168.1.3")
            .firstSeen(now3)
            .lastSeen(now3)
            .version(null)
            .build();
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
    Device original =
        Device.builder()
            .deviceId("device_update")
            .ipAddress("192.168.1.1")
            .firstSeen(now)
            .lastSeen(now)
            .version(null)
            .build();
    Device saved = repository.save(original);

    // When - save with same ID but different data
    OffsetDateTime now2 = OffsetDateTime.now();
    Device updated = saved.toBuilder().ipAddress("192.168.1.2").lastSeen(now2).build();
    repository.save(updated);

    // Then
    Optional<Device> found = repository.findById("device_update");
    assertThat(found).isPresent();
    assertThat(found.get().ipAddress()).isEqualTo("192.168.1.2");
    assertThat(found.get().lastSeen()).isAfter(now);
  }

  @Test
  void save_shouldPreserveFirstSeenWhenUpdating() {
    // Given - use fixed timestamps to match DB precision (microseconds)
    OffsetDateTime firstSeen = OffsetDateTime.parse("2025-01-01T10:00:00.123456Z");
    OffsetDateTime lastSeen = OffsetDateTime.parse("2025-01-02T12:00:00.654321Z");
    Device original =
        Device.builder()
            .deviceId("device_preserve")
            .ipAddress("192.168.1.1")
            .firstSeen(firstSeen)
            .lastSeen(lastSeen)
            .version(null)
            .build();
    Device saved = repository.save(original);

    // When - update with new lastSeen
    OffsetDateTime newLastSeen = OffsetDateTime.parse("2025-01-03T14:00:00.999999Z");
    Device updated = saved.toBuilder().ipAddress("192.168.1.2").lastSeen(newLastSeen).build();
    repository.save(updated);

    // Then
    Optional<Device> found = repository.findById("device_preserve");
    assertThat(found).isPresent();
    assertThat(found.get().firstSeen()).isEqualTo(firstSeen);
    assertThat(found.get().lastSeen()).isEqualTo(newLastSeen);
  }
}
