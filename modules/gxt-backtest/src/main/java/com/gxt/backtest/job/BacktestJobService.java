package com.gxt.backtest.job;

import com.gxt.backtest.domain.BacktestJobEntity;
import com.gxt.backtest.dto.BacktestJobResponse;
import com.gxt.backtest.dto.SubmitBacktestResponse;
import com.gxt.backtest.repository.BacktestJobRepository;
import com.gxt.common.config.GxtProperties;
import com.gxt.common.exception.ResourceNotFoundException;
import com.gxt.common.exception.ValidationException;
import com.gxt.strategybuilder.domain.StrategySpecSnapshotEntity;
import com.gxt.strategybuilder.service.StrategyCrudService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BacktestJobService {
    private final BacktestJobRepository jobRepository;
    private final StrategyCrudService strategyCrudService;
    private final BacktestJobExecutor jobExecutor;
    private final GxtProperties properties;
    private final AtomicInteger activeJobs = new AtomicInteger(0);

    public BacktestJobService(
            BacktestJobRepository jobRepository,
            StrategyCrudService strategyCrudService,
            BacktestJobExecutor jobExecutor,
            GxtProperties properties) {
        this.jobRepository = jobRepository;
        this.strategyCrudService = strategyCrudService;
        this.jobExecutor = jobExecutor;
        this.properties = properties;
    }

    @Transactional
    public SubmitBacktestResponse submit(String strategyId) {
        if (activeJobs.get() >= properties.getMaxConcurrentJobs()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Backtest queue is full");
        }

        StrategySpecSnapshotEntity snapshot = strategyCrudService.latestSnapshot(strategyId);
        if (snapshot == null) {
            throw new ValidationException("Strategy has no spec snapshot to backtest", List.of());
        }

        Instant now = Instant.now();
        BacktestJobEntity job = new BacktestJobEntity();
        job.setId("job_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        job.setStrategyId(strategyId);
        job.setSnapshotId(snapshot.getId());
        job.setStatus("queued");
        job.setCreatedAt(now);
        job.setUpdatedAt(now);
        jobRepository.save(job);

        strategyCrudService.markBacktestQueued(strategyId);
        activeJobs.incrementAndGet();
        jobExecutor.executeAsync(job.getId());
        return new SubmitBacktestResponse(job.getId(), job.getStatus());
    }

    public BacktestJobResponse getJob(String jobId) {
        BacktestJobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Backtest job not found: " + jobId));
        if ("completed".equals(job.getStatus()) || "failed".equals(job.getStatus())) {
            activeJobs.updateAndGet(v -> Math.max(0, v - 1));
        }
        return new BacktestJobResponse(
                job.getId(),
                job.getStrategyId(),
                job.getStatus(),
                job.getReportId(),
                job.getErrorMessage());
    }
}
