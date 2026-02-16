package com.github.juliusd.ueberboeseapi.device;

import static com.github.juliusd.ueberboeseapi.device.DeviceService.UN_PAIRED;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.ueberboeseapi.TestBase;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DeviceServiceTest extends TestBase {

  @Autowired private DeviceService deviceService;

  @Test
  void unpairDevice_success() {
    // Given: A device exists with a margeAccountId
    var now = OffsetDateTime.now().withNano(0);
    var device =
        Device.builder()
            .deviceId("TEST_DEVICE_001")
            .name("Test Device")
            .ipAddress("192.168.1.100")
            .margeAccountId("6921042")
            .firstSeen(now.minusDays(10))
            .lastSeen(now.minusHours(1))
            .updatedOn(now.minusHours(1))
            .build();
    deviceRepository.save(device);

    // When: unpairDevice is called
    boolean result = deviceService.unpairDevice("TEST_DEVICE_001");

    // Then: The method returns true and the device's margeAccountId is set to UN_PAIRED
    assertThat(result).isTrue();

    var updatedDevice = deviceRepository.findById("TEST_DEVICE_001");
    assertThat(updatedDevice).isPresent();
    assertThat(updatedDevice.get().margeAccountId()).isEqualTo("UN_PAIRED");
    assertThat(updatedDevice.get().updatedOn()).isAfterOrEqualTo(now);
  }

  @Test
  void unpairDevice_deviceDoesNotExist() {
    boolean result = deviceService.unpairDevice("NON_EXISTENT_DEVICE");

    assertThat(result).isFalse();
  }

  @Test
  void unpairDevice_alreadyUnpaired() {
    // Given: A device exists but is already unpaired
    var now = OffsetDateTime.now().withNano(0);
    var device =
        Device.builder()
            .deviceId("TEST_DEVICE_002")
            .name("Already Unpaired Device")
            .ipAddress("192.168.1.101")
            .margeAccountId("UN_PAIRED")
            .firstSeen(now.minusDays(10))
            .lastSeen(now.minusHours(1))
            .updatedOn(now.minusHours(1))
            .build();
    deviceRepository.save(device);

    // When: unpairDevice is called
    boolean result = deviceService.unpairDevice("TEST_DEVICE_002");

    // Then: The method returns false and the device remains unchanged
    assertThat(result).isFalse();

    var unchangedDevice = deviceRepository.findById("TEST_DEVICE_002");
    assertThat(unchangedDevice).isPresent();
    assertThat(unchangedDevice.get().margeAccountId()).isEqualTo("UN_PAIRED");
    // updatedOn should not have changed
    assertThat(unchangedDevice.get().updatedOn()).isEqualTo(now.minusHours(1));
  }

  @Test
  void unpairDevice_withNullMargeAccountId() {
    // Given: A device exists with null margeAccountId
    var now = OffsetDateTime.now().withNano(0);
    var device =
        Device.builder()
            .deviceId("TEST_DEVICE_003")
            .name("Device Without Account")
            .ipAddress("192.168.1.102")
            .margeAccountId(null)
            .firstSeen(now.minusDays(10))
            .lastSeen(now.minusHours(1))
            .updatedOn(now.minusHours(1))
            .build();
    deviceRepository.save(device);

    // When: unpairDevice is called
    boolean result = deviceService.unpairDevice("TEST_DEVICE_003");

    // Then: The method returns true and sets margeAccountId to UN_PAIRED
    assertThat(result).isTrue();

    var updatedDevice = deviceRepository.findById("TEST_DEVICE_003");
    assertThat(updatedDevice).isPresent();
    assertThat(updatedDevice.get().margeAccountId()).isEqualTo("UN_PAIRED");
    assertThat(updatedDevice.get().updatedOn()).isAfterOrEqualTo(now);
  }

  @Test
  void unpairDevice_preservesOtherFields() {
    // Given: A device with all fields populated
    var now = OffsetDateTime.now().withNano(0);
    var originalFirstSeen = now.minusDays(30);
    var originalLastSeen = now.minusHours(2);
    var device =
        Device.builder()
            .deviceId("TEST_DEVICE_004")
            .name("Full Device")
            .ipAddress("192.168.1.103")
            .margeAccountId("6921042")
            .firstSeen(originalFirstSeen)
            .lastSeen(originalLastSeen)
            .updatedOn(now.minusHours(2))
            .build();
    deviceRepository.save(device);

    // When: unpairDevice is called
    boolean result = deviceService.unpairDevice("TEST_DEVICE_004");

    // Then: Only margeAccountId and updatedOn are changed
    assertThat(result).isTrue();

    var updatedDevice = deviceRepository.findById("TEST_DEVICE_004");
    assertThat(updatedDevice).isPresent();
    assertThat(updatedDevice.get().deviceId()).isEqualTo("TEST_DEVICE_004");
    assertThat(updatedDevice.get().name()).isEqualTo("Full Device");
    assertThat(updatedDevice.get().ipAddress()).isEqualTo("192.168.1.103");
    assertThat(updatedDevice.get().margeAccountId()).isEqualTo("UN_PAIRED");
    assertThat(updatedDevice.get().firstSeen()).isEqualTo(originalFirstSeen);
    assertThat(updatedDevice.get().lastSeen()).isEqualTo(originalLastSeen);
    assertThat(updatedDevice.get().updatedOn()).isAfterOrEqualTo(now);
  }

  @Test
  void pairDevice_newDevice() {
    var now = OffsetDateTime.now().withNano(0);

    // When: pairDevice is called
    var result = deviceService.pairDevice("6921042", "587A628A4042", "Kitchen");

    // Then: A new device is created with correct accountId, name, and timestamps
    assertThat(result).isNotNull();
    assertThat(result.deviceId()).isEqualTo("587A628A4042");
    assertThat(result.name()).isEqualTo("Kitchen");
    assertThat(result.margeAccountId()).isEqualTo("6921042");
    assertThat(result.ipAddress()).isEmpty();
    assertThat(result.firstSeen()).isAfterOrEqualTo(now);
    assertThat(result.lastSeen()).isAfterOrEqualTo(now);
    assertThat(result.updatedOn()).isAfterOrEqualTo(now);

    // Verify it's persisted in the database
    var savedDevice = deviceRepository.findById("587A628A4042");
    assertThat(savedDevice).isPresent();
    assertThat(savedDevice.get().margeAccountId()).isEqualTo("6921042");
  }

  @Test
  void pairDevice_existingUnpairedDevice() {
    // Given: A device exists with margeAccountId="UN_PAIRED"
    var now = OffsetDateTime.now().withNano(0);
    var device =
        Device.builder()
            .deviceId("TEST_DEVICE_005")
            .name("Old Name")
            .ipAddress("192.168.1.104")
            .margeAccountId(UN_PAIRED)
            .firstSeen(now.minusDays(10))
            .lastSeen(now.minusHours(1))
            .updatedOn(now.minusHours(1))
            .build();
    deviceRepository.save(device);

    // When: pairDevice is called with a new accountId
    var result = deviceService.pairDevice("6921042", "TEST_DEVICE_005", "New Name");

    // Then: margeAccountId is updated to new accountId
    assertThat(result).isNotNull();
    assertThat(result.deviceId()).isEqualTo("TEST_DEVICE_005");
    assertThat(result.name()).isEqualTo("New Name");
    assertThat(result.margeAccountId()).isEqualTo("6921042");
    assertThat(result.updatedOn()).isAfterOrEqualTo(now);
  }

  @Test
  void pairDevice_existingPairedDevice() {
    // Given: A device exists with margeAccountId="6921042"
    var now = OffsetDateTime.now().withNano(0);
    var device =
        Device.builder()
            .deviceId("TEST_DEVICE_006")
            .name("Old Name")
            .ipAddress("192.168.1.105")
            .margeAccountId("6921042")
            .firstSeen(now.minusDays(10))
            .lastSeen(now.minusHours(1))
            .updatedOn(now.minusHours(1))
            .build();
    deviceRepository.save(device);

    // When: pairDevice is called with a different accountId
    var result = deviceService.pairDevice("6921043", "TEST_DEVICE_006", "Updated Name");

    // Then: margeAccountId is updated to the new accountId
    assertThat(result).isNotNull();
    assertThat(result.deviceId()).isEqualTo("TEST_DEVICE_006");
    assertThat(result.name()).isEqualTo("Updated Name");
    assertThat(result.margeAccountId()).isEqualTo("6921043");
    assertThat(result.updatedOn()).isAfterOrEqualTo(now);
  }

  @Test
  void pairDevice_updatesName() {
    // Given: A device exists with name="Old Name"
    var now = OffsetDateTime.now().withNano(0);
    var device =
        Device.builder()
            .deviceId("TEST_DEVICE_007")
            .name("Old Name")
            .ipAddress("192.168.1.106")
            .margeAccountId("6921042")
            .firstSeen(now.minusDays(10))
            .lastSeen(now.minusHours(1))
            .updatedOn(now.minusHours(1))
            .build();
    deviceRepository.save(device);

    // When: pairDevice is called with a new name
    var result = deviceService.pairDevice("6921042", "TEST_DEVICE_007", "New Name");

    // Then: name is updated
    assertThat(result).isNotNull();
    assertThat(result.deviceId()).isEqualTo("TEST_DEVICE_007");
    assertThat(result.name()).isEqualTo("New Name");
    assertThat(result.margeAccountId()).isEqualTo("6921042");
    assertThat(result.updatedOn()).isAfterOrEqualTo(now);

    // Verify persisted
    var savedDevice = deviceRepository.findById("TEST_DEVICE_007");
    assertThat(savedDevice).isPresent();
    assertThat(savedDevice.get().name()).isEqualTo("New Name");
  }
}
