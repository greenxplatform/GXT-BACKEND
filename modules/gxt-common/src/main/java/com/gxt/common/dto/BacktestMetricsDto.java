package com.gxt.common.dto;

public record BacktestMetricsDto(
        double winRate,
        int totalTrades,
        double avgReturnPct,
        double maxDrawdownPct,
        double cagrPct,
        double profitFactor,
        double sharpe
) {}
