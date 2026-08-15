package com.gxt.strategybuilder.repository;

import com.gxt.strategybuilder.domain.StrategyEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StrategyRepository extends JpaRepository<StrategyEntity, String> {
    List<StrategyEntity> findByUserIdOrderByUpdatedAtDesc(String userId);
}
