package com.github.juliusd.ueberboeseapi.service;

import com.github.juliusd.ueberboeseapi.device.Device;
import com.github.juliusd.ueberboeseapi.device.DeviceRepository;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for tracking devices that report to the /streaming/support/power_on endpoint. Stores
 * device information in the H2 database including IP address and timestamps.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceTrackingService {

  private final DeviceRepository deviceRepository;

  /**
   * Records a device power-on event. If this is the first time the device is seen, creates a new
   * entry with firstSeen timestamp. Otherwise updates the lastSeen timestamp.
   *
   * @param deviceId The device identifier from the power_on request
   * @param ipAddress The IP address of the device
   */
  public void recordDevicePowerOn(String deviceId, String ipAddress) {
    log.debug("Recording power_on for device: {} at IP: {}", deviceId, ipAddress);

    deviceRepository
        .findById(deviceId)
        .ifPresentOrElse(
            existingDevice -> {
              // Update existing device
              OffsetDateTime now = OffsetDateTime.now();
              log.debug(
                  "Updating device: {} at IP: {} (last seen: {}, previous IP: {})",
                  deviceId,
                  ipAddress,
                  now,
                  existingDevice.ipAddress());

              var updatedDeviceBuilder = existingDevice.toBuilder().lastSeen(now);
              if (!Objects.equals(existingDevice.ipAddress(), ipAddress)) {
                updatedDeviceBuilder.ipAddress(ipAddress).updatedOn(now);
              }
              deviceRepository.save(updatedDeviceBuilder.build());
            },
            () -> {
              // First time seeing this device
              OffsetDateTime now = OffsetDateTime.now();
              log.info(
                  "New device registered: {} at IP: {} (first seen: {})", deviceId, ipAddress, now);
              Device newDevice =
                  Device.builder()
                      .deviceId(deviceId)
                      .name(null)
                      .ipAddress(ipAddress)
                      .firstSeen(now)
                      .lastSeen(now)
                      .updatedOn(now)
                      .version(null)
                      .build();
              deviceRepository.save(newDevice);
            });
  }

  /**
   * Returns all tracked devices.
   *
   * @return Collection of DeviceInfo objects for all devices that have reported to power_on
   */
  public Collection<DeviceInfo> getAllDevices() {
    var devices = deviceRepository.findAllByOrderByLastSeenDesc();
    log.debug("Retrieving all tracked devices (count: {})", devices.size());
    return devices.stream()
        .map(
            device ->
                new DeviceInfo(
                    device.deviceId(), device.ipAddress(), device.firstSeen(), device.lastSeen()))
        .toList();
  }

  /** Data class representing information about a tracked device. */
  @Data
  @AllArgsConstructor
  public static class DeviceInfo {
    private String deviceId;
    private String ipAddress;
    private OffsetDateTime firstSeen;
    private OffsetDateTime lastSeen;
  }
}
