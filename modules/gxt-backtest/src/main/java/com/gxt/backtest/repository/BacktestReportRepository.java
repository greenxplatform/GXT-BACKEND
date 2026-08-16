package com.gxt.backtest.repository;

import com.gxt.backtest.domain.BacktestReportEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BacktestReportRepository extends JpaRepository<BacktestReportEntity, String> {
    Optional<BacktestReportEntity> findTopByStrategyIdOrderByCreatedAtDesc(String strategyId);
}
