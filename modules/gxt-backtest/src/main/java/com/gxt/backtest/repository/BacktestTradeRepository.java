package com.gxt.backtest.repository;

import com.gxt.backtest.domain.BacktestTradeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BacktestTradeRepository extends JpaRepository<BacktestTradeEntity, String> {
    List<BacktestTradeEntity> findByReportIdOrderByEntryDateAsc(String reportId);
}
