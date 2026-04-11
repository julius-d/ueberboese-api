package com.github.juliusd.ueberboeseapi.service;

import com.github.juliusd.ueberboeseapi.device.Device;
import com.github.juliusd.ueberboeseapi.device.DeviceRepository;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Objects;
import lombok.Builder;
import lombok.NonNull;
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

  /** Data class carrying all fields reported during a device power-on event. */
  @Builder
  public record PowerOnData(
      @NonNull String deviceId,
      @NonNull String ipAddress,
      String firmwareVersion,
      String deviceSerialNumber,
      String productCode,
      String productType,
      String productSerialNumber) {}

  /**
   * Records a device power-on event. If this is the first time the device is seen, creates a new
   * entry with firstSeen timestamp. Otherwise, updates the lastSeen timestamp and any changed
   * fields.
   *
   * @param data The power-on data from the power_on request
   */
  public void recordDevicePowerOn(PowerOnData data) {
    var now = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);

    deviceRepository
        .findById(data.deviceId())
        .ifPresentOrElse(
            existingDevice -> {
              log.debug(
                  "Updating device: {} at IP: {} (previous IP: {})",
                  data.deviceId(),
                  data.ipAddress(),
                  existingDevice.ipAddress());

              var updatedDeviceBuilder = existingDevice.toBuilder().lastSeen(now);

              if (data.ipAddress() != null
                  && !Objects.equals(existingDevice.ipAddress(), data.ipAddress())) {
                updatedDeviceBuilder.ipAddress(data.ipAddress()).updatedOn(now);
              }
              if (data.firmwareVersion() != null
                  && !Objects.equals(existingDevice.firmwareVersion(), data.firmwareVersion())) {
                updatedDeviceBuilder.firmwareVersion(data.firmwareVersion()).updatedOn(now);
              }
              if (data.deviceSerialNumber() != null
                  && !Objects.equals(
                      existingDevice.deviceSerialNumber(), data.deviceSerialNumber())) {
                updatedDeviceBuilder.deviceSerialNumber(data.deviceSerialNumber()).updatedOn(now);
              }
              if (data.productCode() != null
                  && !Objects.equals(existingDevice.productCode(), data.productCode())) {
                updatedDeviceBuilder.productCode(data.productCode()).updatedOn(now);
              }
              if (data.productType() != null
                  && !Objects.equals(existingDevice.productType(), data.productType())) {
                updatedDeviceBuilder.productType(data.productType()).updatedOn(now);
              }
              if (data.productSerialNumber() != null
                  && !Objects.equals(
                      existingDevice.productSerialNumber(), data.productSerialNumber())) {
                updatedDeviceBuilder.productSerialNumber(data.productSerialNumber()).updatedOn(now);
              }

              deviceRepository.save(updatedDeviceBuilder.build());
            },
            () -> {
              log.info("New device registered: {} at IP: {}", data.deviceId(), data.ipAddress());
              var newDevice =
                  Device.builder()
                      .deviceId(data.deviceId())
                      .name(null)
                      .ipAddress(data.ipAddress())
                      .firmwareVersion(data.firmwareVersion())
                      .deviceSerialNumber(data.deviceSerialNumber())
                      .productCode(data.productCode())
                      .productType(data.productType())
                      .productSerialNumber(data.productSerialNumber())
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
  public record DeviceInfo(
      String deviceId, String ipAddress, OffsetDateTime firstSeen, OffsetDateTime lastSeen) {}
}
