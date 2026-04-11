package com.github.juliusd.ueberboeseapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.ueberboeseapi.TestBase;
import com.github.juliusd.ueberboeseapi.device.Device;
import com.github.juliusd.ueberboeseapi.device.DeviceRepository;
import java.time.OffsetDateTime;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DeviceTrackingServiceTest extends TestBase {

  @Autowired private DeviceTrackingService deviceTrackingService;
  @Autowired private DeviceRepository deviceRepository;

  private static DeviceTrackingService.PowerOnData powerOnData(String deviceId, String ipAddress) {
    return DeviceTrackingService.PowerOnData.builder()
        .deviceId(deviceId)
        .ipAddress(ipAddress)
        .build();
  }

  @Test
  void recordDevicePowerOn_shouldRegisterNewDevice() {
    // Given
    String deviceId = "587A628A4042";
    String ipAddress = "192.168.178.23";

    // When
    deviceTrackingService.recordDevicePowerOn(powerOnData(deviceId, ipAddress));

    // Then
    Collection<DeviceTrackingService.DeviceInfo> devices = deviceTrackingService.getAllDevices();
    assertThat(devices).hasSize(1);

    DeviceTrackingService.DeviceInfo deviceInfo = devices.iterator().next();
    assertThat(deviceInfo.deviceId()).isEqualTo(deviceId);
    assertThat(deviceInfo.ipAddress()).isEqualTo(ipAddress);
    assertThat(deviceInfo.firstSeen()).isNotNull();
    assertThat(deviceInfo.lastSeen()).isNotNull();
    assertThat(deviceInfo.firstSeen()).isEqualTo(deviceInfo.lastSeen());
  }

  @Test
  void recordDevicePowerOn_shouldStoreNewDeviceFields() {
    // Given
    String deviceId = "587A628A4073";
    var data =
        DeviceTrackingService.PowerOnData.builder()
            .deviceId(deviceId)
            .ipAddress("192.168.178.26")
            .firmwareVersion("27.0.6.46330.5043500 epdbuild.trunk.hepdswbld04.2022-08-04T11:20:29")
            .deviceSerialNumber("PTEST0000000000000000001")
            .productCode("SoundTouch 10 sm2")
            .productType("5")
            .productSerialNumber("TEST000000000000002")
            .build();

    // When
    deviceTrackingService.recordDevicePowerOn(data);

    // Then
    Device stored = deviceRepository.findById(deviceId).orElseThrow();
    assertThat(stored.firmwareVersion())
        .isEqualTo("27.0.6.46330.5043500 epdbuild.trunk.hepdswbld04.2022-08-04T11:20:29");
    assertThat(stored.deviceSerialNumber()).isEqualTo("PTEST0000000000000000001");
    assertThat(stored.productCode()).isEqualTo("SoundTouch 10 sm2");
    assertThat(stored.productType()).isEqualTo("5");
    assertThat(stored.productSerialNumber()).isEqualTo("TEST000000000000002");
  }

  @Test
  void recordDevicePowerOn_shouldUpdateExistingDevice() throws InterruptedException {
    // Given
    String deviceId = "587A628A4042";
    String initialIpAddress = "192.168.178.23";
    String updatedIpAddress = "192.168.178.27";

    // Record first power on
    deviceTrackingService.recordDevicePowerOn(powerOnData(deviceId, initialIpAddress));
    Collection<DeviceTrackingService.DeviceInfo> devicesAfterFirst =
        deviceTrackingService.getAllDevices();
    DeviceTrackingService.DeviceInfo firstInfo = devicesAfterFirst.iterator().next();
    OffsetDateTime firstSeenTime = firstInfo.firstSeen();

    // Wait a bit to ensure timestamp difference
    Thread.sleep(10);

    // When - Record second power on with different IP
    deviceTrackingService.recordDevicePowerOn(powerOnData(deviceId, updatedIpAddress));

    // Then
    Collection<DeviceTrackingService.DeviceInfo> devices = deviceTrackingService.getAllDevices();
    assertThat(devices).hasSize(1); // Still only one device

    DeviceTrackingService.DeviceInfo deviceInfo = devices.iterator().next();
    assertThat(deviceInfo.deviceId()).isEqualTo(deviceId);
    assertThat(deviceInfo.ipAddress()).isEqualTo(updatedIpAddress); // IP updated
    assertThat(deviceInfo.firstSeen()).isEqualTo(firstSeenTime); // First seen unchanged
    assertThat(deviceInfo.lastSeen()).isAfter(firstSeenTime); // Last seen updated
  }

  @Test
  void recordDevicePowerOn_shouldUpdateFirmwareVersion() throws InterruptedException {
    // Given
    String deviceId = "587A628A4073";
    var firstData =
        DeviceTrackingService.PowerOnData.builder()
            .deviceId(deviceId)
            .ipAddress("192.168.178.26")
            .firmwareVersion("27.0.6.46330")
            .build();
    deviceTrackingService.recordDevicePowerOn(firstData);
    OffsetDateTime updatedOnAfterFirst =
        deviceRepository.findById(deviceId).orElseThrow().updatedOn();

    Thread.sleep(10);

    // When
    var secondData =
        DeviceTrackingService.PowerOnData.builder()
            .deviceId(deviceId)
            .ipAddress("192.168.178.26")
            .firmwareVersion("28.0.1.00001")
            .build();
    deviceTrackingService.recordDevicePowerOn(secondData);

    // Then
    Device stored = deviceRepository.findById(deviceId).orElseThrow();
    assertThat(stored.firmwareVersion()).isEqualTo("28.0.1.00001");
    assertThat(stored.updatedOn()).isAfter(updatedOnAfterFirst);
  }

  @Test
  void recordDevicePowerOn_shouldNotOverwriteExistingFieldsWithNull() throws InterruptedException {
    // Given
    String deviceId = "587A628A4073";
    var firstData =
        DeviceTrackingService.PowerOnData.builder()
            .deviceId(deviceId)
            .ipAddress("192.168.178.26")
            .firmwareVersion("27.0.6.46330")
            .deviceSerialNumber("P123")
            .productCode("SoundTouch 10 sm2")
            .productType("5")
            .productSerialNumber("069")
            .build();
    deviceTrackingService.recordDevicePowerOn(firstData);
    OffsetDateTime updatedOnAfterFirst =
        deviceRepository.findById(deviceId).orElseThrow().updatedOn();

    Thread.sleep(10);

    // When - second call has null for all optional fields
    var secondData =
        DeviceTrackingService.PowerOnData.builder()
            .deviceId(deviceId)
            .ipAddress("192.168.178.26")
            .build();
    deviceTrackingService.recordDevicePowerOn(secondData);

    // Then - existing values preserved, updatedOn unchanged
    Device stored = deviceRepository.findById(deviceId).orElseThrow();
    assertThat(stored.firmwareVersion()).isEqualTo("27.0.6.46330");
    assertThat(stored.deviceSerialNumber()).isEqualTo("P123");
    assertThat(stored.productCode()).isEqualTo("SoundTouch 10 sm2");
    assertThat(stored.productType()).isEqualTo("5");
    assertThat(stored.productSerialNumber()).isEqualTo("069");
    assertThat(stored.updatedOn()).isEqualTo(updatedOnAfterFirst);
  }

  @Test
  void recordDevicePowerOn_shouldNotUpdateWhenAllFieldsUnchanged() throws InterruptedException {
    // Given
    String deviceId = "587A628A4073";
    var data =
        DeviceTrackingService.PowerOnData.builder()
            .deviceId(deviceId)
            .ipAddress("192.168.178.26")
            .firmwareVersion("27.0.6.46330")
            .deviceSerialNumber("P123")
            .productCode("SoundTouch 10 sm2")
            .productType("5")
            .productSerialNumber("069")
            .build();
    deviceTrackingService.recordDevicePowerOn(data);
    OffsetDateTime updatedOnAfterFirst =
        deviceRepository.findById(deviceId).orElseThrow().updatedOn();

    Thread.sleep(10);

    // When - same data again
    deviceTrackingService.recordDevicePowerOn(data);

    // Then
    Device stored = deviceRepository.findById(deviceId).orElseThrow();
    assertThat(stored.updatedOn()).isEqualTo(updatedOnAfterFirst);
  }

  @Test
  void recordDevicePowerOn_shouldTrackMultipleDevices() {
    // Given
    String device1Id = "587A628A4099";
    String device1Ip = "192.168.178.26";
    String device2Id = "587A628A4042";
    String device2Ip = "192.168.178.50";

    // When
    deviceTrackingService.recordDevicePowerOn(powerOnData(device1Id, device1Ip));
    deviceTrackingService.recordDevicePowerOn(powerOnData(device2Id, device2Ip));

    // Then
    Collection<DeviceTrackingService.DeviceInfo> devices = deviceTrackingService.getAllDevices();
    assertThat(devices).hasSize(2);

    assertThat(devices)
        .extracting(DeviceTrackingService.DeviceInfo::deviceId)
        .containsExactlyInAnyOrder(device1Id, device2Id);

    assertThat(devices)
        .extracting(DeviceTrackingService.DeviceInfo::ipAddress)
        .containsExactlyInAnyOrder(device1Ip, device2Ip);
  }

  @Test
  void getAllDevices_shouldReturnEmptyCollectionWhenNoDevices() {
    // When
    Collection<DeviceTrackingService.DeviceInfo> devices = deviceTrackingService.getAllDevices();

    // Then
    assertThat(devices).isEmpty();
  }

  @Test
  void getAllDevices_shouldReturnAllTrackedDevices() {
    // Given
    deviceTrackingService.recordDevicePowerOn(powerOnData("device1", "192.168.1.1"));
    deviceTrackingService.recordDevicePowerOn(powerOnData("device2", "192.168.1.2"));
    deviceTrackingService.recordDevicePowerOn(powerOnData("device3", "192.168.1.3"));

    // When
    Collection<DeviceTrackingService.DeviceInfo> devices = deviceTrackingService.getAllDevices();

    // Then
    assertThat(devices).hasSize(3);
    assertThat(devices)
        .extracting(DeviceTrackingService.DeviceInfo::deviceId)
        .containsExactlyInAnyOrder("device1", "device2", "device3");
  }

  @Test
  void recordDevicePowerOn_shouldKeepFirstSeenUnchangedAcrossMultipleUpdates()
      throws InterruptedException {
    // Given
    String deviceId = "587A628A4042";

    // When - Record multiple power on events
    deviceTrackingService.recordDevicePowerOn(powerOnData(deviceId, "192.168.1.1"));
    OffsetDateTime firstSeenTime =
        deviceTrackingService.getAllDevices().iterator().next().firstSeen();

    Thread.sleep(10);
    deviceTrackingService.recordDevicePowerOn(powerOnData(deviceId, "192.168.1.2"));

    Thread.sleep(10);
    deviceTrackingService.recordDevicePowerOn(powerOnData(deviceId, "192.168.1.3"));

    // Then
    DeviceTrackingService.DeviceInfo finalDeviceInfo =
        deviceTrackingService.getAllDevices().iterator().next();
    assertThat(finalDeviceInfo.firstSeen()).isEqualTo(firstSeenTime);
    assertThat(finalDeviceInfo.lastSeen()).isAfter(firstSeenTime);
    assertThat(finalDeviceInfo.ipAddress()).isEqualTo("192.168.1.3");
  }
}
