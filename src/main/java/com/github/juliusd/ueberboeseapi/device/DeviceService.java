package com.github.juliusd.ueberboeseapi.device;

import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceService {

  static final String UN_PAIRED = "UN_PAIRED";

  private final DeviceRepository deviceRepository;

  /**
   * Removes a device from an account by setting margeAccountId to UN_PAIRED.
   *
   * @param deviceId The device ID to unpair
   * @return true if the device was found and unpaired, false elsewise
   */
  @Transactional
  public boolean unpairDevice(String deviceId) {
    log.info("Unpairing device {}", deviceId);
    var now = OffsetDateTime.now().withNano(0);

    Optional<Device> deviceOpt = deviceRepository.findById(deviceId);

    if (deviceOpt.isEmpty()) {
      log.warn("Device {} does not exist", deviceId);
      return false;
    }

    Device device = deviceOpt.get();

    // Check if already unpaired
    if (UN_PAIRED.equals(device.margeAccountId())) {
      log.info("Device {} is already unpaired (margeAccountId is UN_PAIRED)", deviceId);
      return false;
    }

    // Update the device to set margeAccountId to UN_PAIRED
    Device updatedDevice = device.toBuilder().margeAccountId(UN_PAIRED).updatedOn(now).build();

    deviceRepository.save(updatedDevice);

    log.info("Successfully unpaired device {}, set margeAccountId to UN_PAIRED", deviceId);
    return true;
  }

  @Transactional
  public Device pairDevice(String accountId, String deviceId, String name) {
    log.info("Pairing device {} to account {}", deviceId, accountId);
    var now = OffsetDateTime.now().withNano(0);

    Optional<Device> deviceOpt = deviceRepository.findById(deviceId);

    Device device =
        deviceOpt
            .map(
                existingDevice -> {
                  log.info(
                      "Updating existing device {} with new account {} and name {}",
                      deviceId,
                      accountId,
                      name);
                  return existingDevice.toBuilder()
                      .margeAccountId(accountId)
                      .name(name)
                      .updatedOn(now)
                      .build();
                })
            .orElseGet(
                () -> {
                  log.info("Creating new device {} for account {}", deviceId, accountId);
                  return Device.builder()
                      .deviceId(deviceId)
                      .name(name)
                      .margeAccountId(accountId)
                      .ipAddress("") // Empty IP address - not known at registration time
                      .firstSeen(now)
                      .lastSeen(now)
                      .updatedOn(now)
                      .build();
                });

    Device savedDevice = deviceRepository.save(device);
    log.info("Successfully paired device {} to account {}", deviceId, accountId);
    return savedDevice;
  }
}
