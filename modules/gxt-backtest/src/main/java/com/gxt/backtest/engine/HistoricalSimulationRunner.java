package com.gxt.backtest.engine;

import com.gxt.common.dto.MarketBarDto;
import com.gxt.common.dto.StrategySpecDto;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HistoricalSimulationRunner {
    private static final double INITIAL_CAPITAL = 100_000.0;

    public SimulationResult simulate(StrategySpecDto spec, List<MarketBarDto> bars) {
        List<SimulatedTrade> trades = new ArrayList<>();
        boolean inPosition = false;
        LocalDate entryDate = null;
        double entryPrice = 0;

        String entryRule = spec.entryRules().getFirst().toLowerCase();
        String exitRule = spec.exitRules().getFirst().toLowerCase();
        double stopPct = spec.stopLoss().value();

        for (int i = 1; i < bars.size(); i++) {
            MarketBarDto prev = bars.get(i - 1);
            MarketBarDto cur = bars.get(i);
            if (cur.close() == null || prev.close() == null) {
                continue;
            }

            if (!inPosition && shouldEnter(entryRule, prev, cur)) {
                inPosition = true;
                entryDate = LocalDate.parse(cur.date());
                entryPrice = cur.close();
            } else if (inPosition) {
                double stopPrice = entryPrice * (1 - stopPct / 100.0);
                boolean stopHit = cur.low() != null && cur.low() <= stopPrice;
                boolean exitHit = shouldExit(exitRule, prev, cur, entryPrice);

                if (stopHit || exitHit) {
                    double exitPrice = stopHit ? stopPrice : cur.close();
                    double returnPct = ((exitPrice - entryPrice) / entryPrice) * 100.0;
                    trades.add(new SimulatedTrade(
                            entryDate,
                            LocalDate.parse(cur.date()),
                            "long",
                            round(returnPct),
                            returnPct >= 0 ? "win" : "loss"));
                    inPosition = false;
                }
            }
        }

        List<EquityPoint> equityCurve = buildEquityCurve(trades, bars);
        return new SimulationResult(trades, equityCurve, INITIAL_CAPITAL);
    }

    private boolean shouldEnter(String rule, MarketBarDto prev, MarketBarDto cur) {
        if (rule.contains("sma_20")) {
            return crossedAbove(prev.close(), cur.close(), prev.sma20(), cur.sma20());
        }
        if (rule.contains("rsi_14")) {
            return prev.rsi14() != null && cur.rsi14() != null && prev.rsi14() <= 30 && cur.rsi14() > 30;
        }
        return false;
    }

    private boolean shouldExit(String rule, MarketBarDto prev, MarketBarDto cur, double entryPrice) {
        if (rule.contains("sma_20")) {
            return crossedBelow(prev.close(), cur.close(), prev.sma20(), cur.sma20());
        }
        if (rule.contains("take profit")) {
            double target = entryPrice * 1.03;
            return cur.high() != null && cur.high() >= target;
        }
        if (rule.contains("crosses below sma_20")) {
            return crossedBelow(prev.close(), cur.close(), prev.sma20(), cur.sma20());
        }
        return false;
    }

    private boolean crossedAbove(Double prevClose, Double curClose, Double prevInd, Double curInd) {
        return prevClose != null && curClose != null && prevInd != null && curInd != null
                && prevClose <= prevInd && curClose > curInd;
    }

    private boolean crossedBelow(Double prevClose, Double curClose, Double prevInd, Double curInd) {
        return prevClose != null && curClose != null && prevInd != null && curInd != null
                && prevClose >= prevInd && curClose < curInd;
    }

    private List<EquityPoint> buildEquityCurve(List<SimulatedTrade> trades, List<MarketBarDto> bars) {
        double equity = INITIAL_CAPITAL;
        List<EquityPoint> curve = new ArrayList<>();
        int tradeIdx = 0;

        for (MarketBarDto bar : bars) {
            LocalDate date = LocalDate.parse(bar.date());
            while (tradeIdx < trades.size() && !trades.get(tradeIdx).exitDate().isAfter(date)) {
                equity *= (1 + trades.get(tradeIdx).returnPct() / 100.0);
                tradeIdx++;
            }
            curve.add(new EquityPoint(bar.date(), round(equity)));
        }
        return curve;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public record SimulatedTrade(
            LocalDate entryDate,
            LocalDate exitDate,
            String side,
            double returnPct,
            String result
    ) {}

    public record EquityPoint(String date, double equity) {}

    public record SimulationResult(List<SimulatedTrade> trades, List<EquityPoint> equityCurve, double initialCapital) {}
}
