package com.gxt.strategybuilder.repository;

import com.gxt.strategybuilder.domain.StrategySpecSnapshotEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StrategySpecSnapshotRepository extends JpaRepository<StrategySpecSnapshotEntity, String> {
    List<StrategySpecSnapshotEntity> findByStrategyIdOrderByRevisionDesc(String strategyId);

    Optional<StrategySpecSnapshotEntity> findTopByStrategyIdOrderByRevisionDesc(String strategyId);
}
