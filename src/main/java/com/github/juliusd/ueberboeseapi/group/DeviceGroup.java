package com.github.juliusd.ueberboeseapi.group;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

@Table("DEVICE_GROUP")
@Builder(toBuilder = true)
public record DeviceGroup(
    @Id Long id,
    @NonNull String accountId,
    @NonNull String masterDeviceId,
    @NonNull String name,
    @NonNull String leftDeviceId,
    @NonNull String rightDeviceId,
    @NonNull OffsetDateTime createdOn,
    @NonNull OffsetDateTime updatedOn,
    @Version Long version) {}
