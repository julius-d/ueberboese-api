package com.github.juliusd.ueberboeseapi.group;

public class DeviceAlreadyInGroupException extends RuntimeException {
  private final String deviceId;

  public DeviceAlreadyInGroupException(String deviceId) {
    super("Device " + deviceId + " already belongs to a group");
    this.deviceId = deviceId;
  }

  public String getDeviceId() {
    return deviceId;
  }
}
