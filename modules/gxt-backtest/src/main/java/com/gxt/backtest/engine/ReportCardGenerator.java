package com.gxt.backtest.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gxt.backtest.domain.BacktestReportEntity;
import com.gxt.backtest.domain.BacktestTradeEntity;
import com.gxt.common.dto.BacktestMetricsDto;
import com.gxt.common.dto.BacktestReportDto;
import com.gxt.common.dto.EquityPointDto;
import com.gxt.common.dto.StrategySpecDto;
import com.gxt.common.dto.TradeDto;
import com.gxt.backtest.repository.BacktestTradeRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ReportCardGenerator {
    private final ObjectMapper objectMapper;
    private final BacktestTradeRepository tradeRepository;

    public ReportCardGenerator(ObjectMapper objectMapper, BacktestTradeRepository tradeRepository) {
        this.objectMapper = objectMapper;
        this.tradeRepository = tradeRepository;
    }

    public BacktestReportEntity buildEntity(
            String strategyId,
            StrategySpecDto spec,
            BacktestMetricsDto metrics,
            HistoricalSimulationRunner.SimulationResult simulation) {
        try {
            BacktestReportEntity entity = new BacktestReportEntity();
            entity.setId("bt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
            entity.setStrategyId(strategyId);
            entity.setInstrument(spec.instrument());
            entity.setPeriodFrom(spec.backtestWindow().from());
            entity.setPeriodTo(spec.backtestWindow().to());
            entity.setMetricsJson(objectMapper.writeValueAsString(metrics));
            entity.setEquityJson(objectMapper.writeValueAsString(
                    simulation.equityCurve().stream()
                            .map(p -> new EquityPointDto(p.date(), p.equity()))
                            .toList()));
            entity.setCreatedAt(Instant.now());
            return entity;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to build report entity", ex);
        }
    }

    public List<BacktestTradeEntity> buildTrades(
            String reportId,
            HistoricalSimulationRunner.SimulationResult simulation) {
        return simulation.trades().stream()
                .map(trade -> {
                    BacktestTradeEntity entity = new BacktestTradeEntity();
                    entity.setId("t_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
                    entity.setReportId(reportId);
                    entity.setEntryDate(trade.entryDate());
                    entity.setExitDate(trade.exitDate());
                    entity.setSide(trade.side());
                    entity.setReturnPct(trade.returnPct());
                    entity.setResult(trade.result());
                    return entity;
                })
                .toList();
    }

    public BacktestReportDto toDto(BacktestReportEntity entity) {
        try {
            BacktestMetricsDto metrics = objectMapper.readValue(entity.getMetricsJson(), BacktestMetricsDto.class);
            List<EquityPointDto> equity = objectMapper.readValue(
                    entity.getEquityJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, EquityPointDto.class));
            List<TradeDto> trades = tradeRepository.findByReportIdOrderByEntryDateAsc(entity.getId()).stream()
                    .map(t -> new TradeDto(
                            t.getId(),
                            t.getEntryDate().toString(),
                            t.getExitDate().toString(),
                            t.getSide(),
                            t.getReturnPct(),
                            t.getResult()))
                    .toList();

            return new BacktestReportDto(
                    entity.getId(),
                    entity.getStrategyId(),
                    entity.getInstrument(),
                    new BacktestReportDto.BacktestWindow(
                            entity.getPeriodFrom().toString(),
                            entity.getPeriodTo().toString()),
                    metrics,
                    equity,
                    trades);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to map report dto", ex);
        }
    }
}
