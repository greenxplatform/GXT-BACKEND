package com.gxt.backtest.repository;

import com.gxt.backtest.domain.BacktestJobEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BacktestJobRepository extends JpaRepository<BacktestJobEntity, String> {
    List<BacktestJobEntity> findByStrategyIdOrderByCreatedAtDesc(String strategyId);
}
