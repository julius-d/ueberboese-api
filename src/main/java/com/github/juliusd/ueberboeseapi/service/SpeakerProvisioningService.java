package com.github.juliusd.ueberboeseapi.service;

import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Automatically provisions never-paired speakers on power-on. When a speaker reports power_on with
 * an empty margeAccountUUID, this service calls POST /setMargeAccount on the speaker using its own
 * MAC address as the accountId. This allows factory-reset or never-paired devices to work with the
 * Überböse API without any manual intervention.
 *
 * <p>Discovery: POST http://speaker:8090/setMargeAccount with payload {@code
 * <PairDeviceWithAccount><accountId>MAC</accountId><userAuthToken>any</userAuthToken></PairDeviceWithAccount>}
 * sets the margeAccountUUID on the speaker at runtime, persisting across reboots.
 */
@Service
@Slf4j
public class SpeakerProvisioningService {

  private static final int SETTLE_DELAY_MS = 3000;

  private final RestClient restClient = RestClient.create();

  public void provisionIfNeeded(String deviceId, String ipAddress) {
    CompletableFuture.runAsync(
        () -> {
          try {
            Thread.sleep(SETTLE_DELAY_MS);
            if (hasMargeAccountUUID(ipAddress)) {
              return;
            }
            setMargeAccount(deviceId, ipAddress);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } catch (Exception e) {
            log.warn(
                "Failed to provision device {} at {}: {}", deviceId, ipAddress, e.getMessage());
          }
        });
  }

  private boolean hasMargeAccountUUID(String ipAddress) {
    try {
      String info =
          restClient.get().uri("http://" + ipAddress + ":8090/info").retrieve().body(String.class);
      boolean empty =
          info == null
              || info.contains("<margeAccountUUID></margeAccountUUID>")
              || info.contains("<margeAccountUUID/>");
      return !empty;
    } catch (Exception e) {
      log.debug("Could not read /info from {}: {}", ipAddress, e.getMessage());
      return true;
    }
  }

  private void setMargeAccount(String deviceId, String ipAddress) {
    String payload =
        "<PairDeviceWithAccount><accountId>"
            + deviceId
            + "</accountId><userAuthToken>SoundTouchHybrid</userAuthToken></PairDeviceWithAccount>";

    restClient
        .post()
        .uri("http://" + ipAddress + ":8090/setMargeAccount")
        .header("Content-Type", "application/xml")
        .body(payload)
        .retrieve()
        .toBodilessEntity();

    log.info("Provisioned margeAccountUUID='{}' on device at {}", deviceId, ipAddress);
  }
}
