package com.gxt.common.dto;

public record TradeDto(
        String id,
        String entryDate,
        String exitDate,
        String side,
        double returnPct,
        String result
) {}
