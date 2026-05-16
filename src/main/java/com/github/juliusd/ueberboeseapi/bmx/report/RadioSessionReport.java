package com.github.juliusd.ueberboeseapi.bmx.report;

import java.util.List;
import lombok.Builder;
import lombok.NonNull;

@Builder(toBuilder = true)
public record RadioSessionReport(
    @NonNull String listenId, RadioSession session, @NonNull List<RadioReportEvent> events) {}
