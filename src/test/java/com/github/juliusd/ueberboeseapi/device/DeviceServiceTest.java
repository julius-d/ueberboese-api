package com.github.juliusd.ueberboeseapi.device;

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
}
