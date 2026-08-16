package com.gxt.backtest.engine;

import com.gxt.backtest.engine.HistoricalSimulationRunner.SimulationResult;
import com.gxt.backtest.engine.HistoricalSimulationRunner.SimulatedTrade;
import com.gxt.common.dto.BacktestMetricsDto;
import org.springframework.stereotype.Component;

@Component
public class MetricsAnalyzer {
    public BacktestMetricsDto analyze(SimulationResult result) {
        var trades = result.trades();
        if (trades.isEmpty()) {
            return new BacktestMetricsDto(0, 0, 0, 0, 0, 0, 0);
        }

        int wins = (int) trades.stream().filter(t -> "win".equals(t.result())).count();
        double winRate = (wins * 100.0) / trades.size();
        double avgReturn = trades.stream().mapToDouble(SimulatedTrade::returnPct).average().orElse(0);

        double grossProfit = trades.stream().filter(t -> t.returnPct() > 0).mapToDouble(SimulatedTrade::returnPct).sum();
        double grossLoss = Math.abs(trades.stream().filter(t -> t.returnPct() < 0).mapToDouble(SimulatedTrade::returnPct).sum());
        double profitFactor = grossLoss == 0 ? grossProfit : grossProfit / grossLoss;

        double maxDrawdown = computeMaxDrawdown(result);
        double cagr = computeCagr(result);
        double sharpe = computeSharpe(trades);

        return new BacktestMetricsDto(
                round(winRate),
                trades.size(),
                round(avgReturn),
                round(maxDrawdown),
                round(cagr),
                round(profitFactor),
                round(sharpe));
    }

    private double computeMaxDrawdown(SimulationResult result) {
        double peak = result.initialCapital();
        double maxDd = 0;
        double equity = result.initialCapital();
        for (var point : result.equityCurve()) {
            equity = point.equity();
            peak = Math.max(peak, equity);
            if (peak > 0) {
                maxDd = Math.max(maxDd, ((peak - equity) / peak) * 100.0);
            }
        }
        return maxDd;
    }

    private double computeCagr(SimulationResult result) {
        if (result.equityCurve().isEmpty()) {
            return 0;
        }
        double start = result.initialCapital();
        double end = result.equityCurve().getLast().equity();
        if (start <= 0) {
            return 0;
        }
        return ((end / start) - 1) * 100.0;
    }

    private double computeSharpe(java.util.List<SimulatedTrade> trades) {
        if (trades.size() < 2) {
            return 0;
        }
        double mean = trades.stream().mapToDouble(SimulatedTrade::returnPct).average().orElse(0);
        double variance = trades.stream()
                .mapToDouble(t -> Math.pow(t.returnPct() - mean, 2))
                .average()
                .orElse(0);
        double std = Math.sqrt(variance);
        return std == 0 ? 0 : mean / std;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
