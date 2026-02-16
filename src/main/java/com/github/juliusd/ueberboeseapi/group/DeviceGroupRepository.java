package com.github.juliusd.ueberboeseapi.group;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceGroupRepository extends CrudRepository<DeviceGroup, Long> {

  @Query(
      """
      SELECT *
      FROM DEVICE_GROUP
      WHERE ACCOUNT_ID = :accountId
        AND (LEFT_DEVICE_ID = :deviceId OR RIGHT_DEVICE_ID = :deviceId)
      """)
  Optional<DeviceGroup> findByAccountIdAndDeviceId(String accountId, String deviceId);

  @Query(
      """
      SELECT *
      FROM DEVICE_GROUP
      WHERE LEFT_DEVICE_ID = :deviceId OR RIGHT_DEVICE_ID = :deviceId
      """)
  Optional<DeviceGroup> findByDeviceId(String deviceId);

  @Query("SELECT * FROM DEVICE_GROUP WHERE ACCOUNT_ID = :accountId")
  List<DeviceGroup> findByAccountId(String accountId);

  @Query("SELECT * FROM DEVICE_GROUP WHERE ID = :groupId AND ACCOUNT_ID = :accountId")
  Optional<DeviceGroup> findByIdAndAccountId(Long groupId, String accountId);
}
