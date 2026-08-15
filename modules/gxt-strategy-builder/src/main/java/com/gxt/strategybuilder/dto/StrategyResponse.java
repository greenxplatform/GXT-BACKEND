package com.gxt.strategybuilder.dto;

import com.gxt.common.dto.StrategySpecDto;
import com.gxt.common.dto.StrategyStatus;
import java.time.Instant;

public record StrategyResponse(
        String id,
        String name,
        StrategyStatus status,
        String instrument,
        String timeframe,
        String entry,
        String exit,
        String stopLoss,
        String preferredMode,
        String summary,
        String backtestId,
        StrategySpecDto spec,
        Instant createdAt,
        Instant updatedAt
) {}
