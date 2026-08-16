package com.gxt.backtest.dto;

public record BacktestJobResponse(
        String jobId,
        String strategyId,
        String status,
        String reportId,
        String errorMessage
) {}
