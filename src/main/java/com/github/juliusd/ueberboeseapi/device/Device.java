package com.github.juliusd.ueberboeseapi.device;

import java.time.OffsetDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

@Table("DEVICE")
public record Device(
    @Id String deviceId,
    String ipAddress,
    OffsetDateTime firstSeen,
    OffsetDateTime lastSeen,
    @Version Long version) {}
