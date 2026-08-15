package com.gxt.common.dto;

import java.util.List;

public record BacktestReportDto(
        String id,
        String strategyId,
        String instrument,
        BacktestWindow period,
        BacktestMetricsDto metrics,
        List<EquityPointDto> equityCurve,
        List<TradeDto> trades
) {
    public record BacktestWindow(String from, String to) {}
}
