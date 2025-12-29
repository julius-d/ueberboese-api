package com.github.juliusd.ueberboeseapi.device;

import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends CrudRepository<Device, String> {

  @Query("SELECT * FROM DEVICE ORDER BY LAST_SEEN DESC")
  List<Device> findAllByOrderByLastSeenDesc();
}
