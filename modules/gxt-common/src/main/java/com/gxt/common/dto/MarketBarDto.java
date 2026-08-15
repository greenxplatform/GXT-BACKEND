package com.gxt.common.dto;

public record MarketBarDto(
        String date,
        Double open,
        Double high,
        Double low,
        Double close,
        Long volume,
        Double sma20,
        Double sma50,
        Double ema12,
        Double rsi14,
        Double macd,
        Double dailyReturn
) {}
