package com.github.juliusd.ueberboeseapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.ueberboeseapi.TestBase;
import java.time.OffsetDateTime;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DeviceTrackingServiceTest extends TestBase {

  @Autowired private DeviceTrackingService deviceTrackingService;

  @Test
  void recordDevicePowerOn_shouldRegisterNewDevice() {
    // Given
    String deviceId = "587A628A4042";
    String ipAddress = "192.168.178.23";

    // When
    deviceTrackingService.recordDevicePowerOn(deviceId, ipAddress);

    // Then
    Collection<DeviceTrackingService.DeviceInfo> devices = deviceTrackingService.getAllDevices();
    assertThat(devices).hasSize(1);

    DeviceTrackingService.DeviceInfo deviceInfo = devices.iterator().next();
    assertThat(deviceInfo.getDeviceId()).isEqualTo(deviceId);
    assertThat(deviceInfo.getIpAddress()).isEqualTo(ipAddress);
    assertThat(deviceInfo.getFirstSeen()).isNotNull();
    assertThat(deviceInfo.getLastSeen()).isNotNull();
    assertThat(deviceInfo.getFirstSeen()).isEqualTo(deviceInfo.getLastSeen());
  }

  @Test
  void recordDevicePowerOn_shouldUpdateExistingDevice() throws InterruptedException {
    // Given
    String deviceId = "587A628A4042";
    String initialIpAddress = "192.168.178.23";
    String updatedIpAddress = "192.168.178.27";

    // Record first power on
    deviceTrackingService.recordDevicePowerOn(deviceId, initialIpAddress);
    Collection<DeviceTrackingService.DeviceInfo> devicesAfterFirst =
        deviceTrackingService.getAllDevices();
    DeviceTrackingService.DeviceInfo firstInfo = devicesAfterFirst.iterator().next();
    OffsetDateTime firstSeenTime = firstInfo.getFirstSeen();

    // Wait a bit to ensure timestamp difference
    Thread.sleep(10);

    // When - Record second power on with different IP
    deviceTrackingService.recordDevicePowerOn(deviceId, updatedIpAddress);

    // Then
    Collection<DeviceTrackingService.DeviceInfo> devices = deviceTrackingService.getAllDevices();
    assertThat(devices).hasSize(1); // Still only one device

    DeviceTrackingService.DeviceInfo deviceInfo = devices.iterator().next();
    assertThat(deviceInfo.getDeviceId()).isEqualTo(deviceId);
    assertThat(deviceInfo.getIpAddress()).isEqualTo(updatedIpAddress); // IP updated
    assertThat(deviceInfo.getFirstSeen()).isEqualTo(firstSeenTime); // First seen unchanged
    assertThat(deviceInfo.getLastSeen()).isAfter(firstSeenTime); // Last seen updated
  }

  @Test
  void recordDevicePowerOn_shouldTrackMultipleDevices() {
    // Given
    String device1Id = "587A628A4099";
    String device1Ip = "192.168.178.26";
    String device2Id = "587A628A4042";
    String device2Ip = "192.168.178.50";

    // When
    deviceTrackingService.recordDevicePowerOn(device1Id, device1Ip);
    deviceTrackingService.recordDevicePowerOn(device2Id, device2Ip);

    // Then
    Collection<DeviceTrackingService.DeviceInfo> devices = deviceTrackingService.getAllDevices();
    assertThat(devices).hasSize(2);

    assertThat(devices)
        .extracting(DeviceTrackingService.DeviceInfo::getDeviceId)
        .containsExactlyInAnyOrder(device1Id, device2Id);

    assertThat(devices)
        .extracting(DeviceTrackingService.DeviceInfo::getIpAddress)
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
    deviceTrackingService.recordDevicePowerOn("device1", "192.168.1.1");
    deviceTrackingService.recordDevicePowerOn("device2", "192.168.1.2");
    deviceTrackingService.recordDevicePowerOn("device3", "192.168.1.3");

    // When
    Collection<DeviceTrackingService.DeviceInfo> devices = deviceTrackingService.getAllDevices();

    // Then
    assertThat(devices).hasSize(3);
    assertThat(devices)
        .extracting(DeviceTrackingService.DeviceInfo::getDeviceId)
        .containsExactlyInAnyOrder("device1", "device2", "device3");
  }

  @Test
  void recordDevicePowerOn_shouldKeepFirstSeenUnchangedAcrossMultipleUpdates()
      throws InterruptedException {
    // Given
    String deviceId = "587A628A4042";

    // When - Record multiple power on events
    deviceTrackingService.recordDevicePowerOn(deviceId, "192.168.1.1");
    OffsetDateTime firstSeenTime =
        deviceTrackingService.getAllDevices().iterator().next().getFirstSeen();

    Thread.sleep(10);
    deviceTrackingService.recordDevicePowerOn(deviceId, "192.168.1.2");

    Thread.sleep(10);
    deviceTrackingService.recordDevicePowerOn(deviceId, "192.168.1.3");

    // Then
    DeviceTrackingService.DeviceInfo finalDeviceInfo =
        deviceTrackingService.getAllDevices().iterator().next();
    assertThat(finalDeviceInfo.getFirstSeen()).isEqualTo(firstSeenTime);
    assertThat(finalDeviceInfo.getLastSeen()).isAfter(firstSeenTime);
    assertThat(finalDeviceInfo.getIpAddress()).isEqualTo("192.168.1.3");
  }
}
