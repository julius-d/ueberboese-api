package com.github.juliusd.ueberboeseapi.device;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

@Table("DEVICE")
@Builder(toBuilder = true)
public record Device(
    @NonNull @Id String deviceId,
    String name,
    String ipAddress,
    String margeAccountId,
    String firmwareVersion,
    String deviceSerialNumber,
    String productCode,
    String productType,
    String productSerialNumber,
    OffsetDateTime firstSeen,
    OffsetDateTime lastSeen,
    OffsetDateTime updatedOn,
    @Version Long version) {}
