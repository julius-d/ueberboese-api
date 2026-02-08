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

  private static final String UN_PAIRED = "UN_PAIRED";

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
}
