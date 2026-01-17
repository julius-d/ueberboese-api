package com.github.juliusd.ueberboeseapi.preset;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PresetRepository extends CrudRepository<Preset, Long> {

  @Query("SELECT * FROM PRESET WHERE ACCOUNT_ID = :accountId AND DEVICE_ID = :deviceId")
  List<Preset> findByAccountIdAndDeviceId(String accountId, String deviceId);

  @Query(
      "SELECT * FROM PRESET WHERE ACCOUNT_ID = :accountId AND DEVICE_ID = :deviceId AND"
          + " BUTTON_NUMBER = :buttonNumber")
  Optional<Preset> findByAccountIdAndDeviceIdAndButtonNumber(
      String accountId, String deviceId, Integer buttonNumber);
}
