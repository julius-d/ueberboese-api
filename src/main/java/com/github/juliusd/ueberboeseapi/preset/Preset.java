package com.github.juliusd.ueberboeseapi.preset;

import java.time.OffsetDateTime;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Table("PRESET")
public record Preset(
    @Id Long id,
    String accountId,
    String deviceId,
    Integer buttonNumber,
    String containerArt,
    String contentItemType,
    OffsetDateTime createdOn,
    String location,
    String name,
    OffsetDateTime updatedOn,
    String sourceId,
    @Version Long version) {}
