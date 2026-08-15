package com.gxt.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record StrategySpecDto(
        @NotBlank String instrument,
        @NotBlank String timeframe,
        @NotBlank String direction,
        @NotNull List<String> entryRules,
        @NotNull List<String> exitRules,
        @NotNull StopLossDto stopLoss,
        @NotNull BacktestWindowDto backtestWindow
) {
    public record StopLossDto(@NotBlank String type, @NotNull Double value) {}

    public record BacktestWindowDto(@NotNull LocalDate from, @NotNull LocalDate to) {}
}
