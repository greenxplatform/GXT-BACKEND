package com.gxt.strategybuilder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gxt.common.dto.StrategySpecDto;
import com.gxt.common.dto.StrategyStatus;
import com.gxt.common.exception.ResourceNotFoundException;
import com.gxt.strategybuilder.domain.StrategyEntity;
import com.gxt.strategybuilder.domain.StrategySpecSnapshotEntity;
import com.gxt.strategybuilder.dto.CreateStrategyRequest;
import com.gxt.strategybuilder.dto.StrategyResponse;
import com.gxt.strategybuilder.dto.UpdateStrategyRequest;
import com.gxt.strategybuilder.repository.StrategyRepository;
import com.gxt.strategybuilder.repository.StrategySpecSnapshotRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StrategyCrudService {
    private final StrategyRepository strategyRepository;
    private final StrategySpecSnapshotRepository snapshotRepository;
    private final StrategyPlanNormalizer normalizer;
    private final ObjectMapper objectMapper;

    public StrategyCrudService(
            StrategyRepository strategyRepository,
            StrategySpecSnapshotRepository snapshotRepository,
            StrategyPlanNormalizer normalizer,
            ObjectMapper objectMapper) {
        this.strategyRepository = strategyRepository;
        this.snapshotRepository = snapshotRepository;
        this.normalizer = normalizer;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public StrategyResponse create(String userId, CreateStrategyRequest request) {
        Instant now = Instant.now();
        StrategyEntity entity = new StrategyEntity();
        entity.setId("str_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        entity.setUserId(userId);
        entity.setName(request.name() != null && !request.name().isBlank() ? request.name() : "Untitled Strategy");
        entity.setStatus(StrategyStatus.DRAFT);
        entity.setPreferredMode(request.preferredMode());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        strategyRepository.save(entity);
        return toResponse(entity);
    }

    public StrategyEntity getEntity(String id) {
        return strategyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found: " + id));
    }

    public StrategyResponse get(String id) {
        return toResponse(getEntity(id));
    }

    public List<StrategyResponse> listByUser(String userId) {
        return strategyRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StrategyResponse update(String id, UpdateStrategyRequest request) {
        StrategyEntity entity = getEntity(id);
        if (request.name() != null && !request.name().isBlank()) {
            entity.setName(request.name());
        }
        if (request.preferredMode() != null) {
            entity.setPreferredMode(request.preferredMode());
        }
        if (request.summary() != null) {
            entity.setSummary(request.summary());
        }
        entity.setUpdatedAt(Instant.now());
        return toResponse(strategyRepository.save(entity));
    }

    @Transactional
    public StrategySpecSnapshotEntity saveSpecSnapshot(StrategyEntity entity, StrategySpecDto spec) {
        try {
            String json = objectMapper.writeValueAsString(spec);
            entity.setSpecJson(json);
            entity.setInstrument(spec.instrument());
            entity.setTimeframe(spec.timeframe());
            entity.setDirection(spec.direction());
            entity.setEntryText(String.join("; ", spec.entryRules()));
            entity.setExitText(String.join("; ", spec.exitRules()));
            entity.setStopLossText(spec.stopLoss().value() + "% stop-loss");
            entity.setSummary(buildSummary(spec));
            entity.setUpdatedAt(Instant.now());
            strategyRepository.save(entity);

            int revision = snapshotRepository.findByStrategyIdOrderByRevisionDesc(entity.getId()).size() + 1;
            StrategySpecSnapshotEntity snapshot = new StrategySpecSnapshotEntity();
            snapshot.setId("snap_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
            snapshot.setStrategyId(entity.getId());
            snapshot.setSpecJson(json);
            snapshot.setRevision(revision);
            snapshot.setCreatedAt(Instant.now());
            return snapshotRepository.save(snapshot);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to persist strategy spec", ex);
        }
    }

    @Transactional
    public void markBacktestQueued(String strategyId) {
        StrategyEntity entity = getEntity(strategyId);
        entity.setStatus(StrategyStatus.BACKTEST_QUEUED);
        entity.setUpdatedAt(Instant.now());
        strategyRepository.save(entity);
    }

    @Transactional
    public void markBacktestRunning(String strategyId) {
        StrategyEntity entity = getEntity(strategyId);
        entity.setStatus(StrategyStatus.BACKTEST_RUNNING);
        entity.setUpdatedAt(Instant.now());
        strategyRepository.save(entity);
    }

    @Transactional
    public void markBacktested(String strategyId, String backtestId) {
        StrategyEntity entity = getEntity(strategyId);
        entity.setStatus(StrategyStatus.BACKTESTED);
        entity.setBacktestId(backtestId);
        entity.setUpdatedAt(Instant.now());
        strategyRepository.save(entity);
    }

    public StrategySpecDto latestSpec(String strategyId) {
        StrategyEntity entity = getEntity(strategyId);
        if (entity.getSpecJson() == null) {
            throw new ResourceNotFoundException("No strategy spec for: " + strategyId);
        }
        return normalizer.parseStored(entity.getSpecJson());
    }

    public StrategySpecSnapshotEntity latestSnapshot(String strategyId) {
        return snapshotRepository.findTopByStrategyIdOrderByRevisionDesc(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("No spec snapshot for: " + strategyId));
    }

    private String buildSummary(StrategySpecDto spec) {
        return "Rule-based " + spec.direction() + " strategy on "
                + spec.instrument() + " (" + spec.timeframe() + ").";
    }

    private StrategyResponse toResponse(StrategyEntity entity) {
        StrategySpecDto spec = null;
        if (entity.getSpecJson() != null) {
            try {
                spec = normalizer.parseStored(entity.getSpecJson());
            } catch (Exception ignored) {
                // card may be partially built during chat
            }
        }
        return new StrategyResponse(
                entity.getId(),
                entity.getName(),
                entity.getStatus(),
                entity.getInstrument(),
                entity.getTimeframe(),
                entity.getEntryText(),
                entity.getExitText(),
                entity.getStopLossText(),
                entity.getPreferredMode(),
                entity.getSummary(),
                entity.getBacktestId(),
                spec,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
