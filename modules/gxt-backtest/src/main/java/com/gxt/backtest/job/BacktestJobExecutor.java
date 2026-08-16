package com.gxt.backtest.job;

import com.gxt.backtest.domain.BacktestJobEntity;
import com.gxt.backtest.domain.BacktestReportEntity;
import com.gxt.backtest.engine.HistoricalSimulationRunner;
import com.gxt.backtest.engine.MetricsAnalyzer;
import com.gxt.backtest.engine.ReportCardGenerator;
import com.gxt.backtest.engine.StrategySpecValidator;
import com.gxt.backtest.repository.BacktestJobRepository;
import com.gxt.backtest.repository.BacktestReportRepository;
import com.gxt.backtest.repository.BacktestTradeRepository;
import com.gxt.common.dto.MarketBarDto;
import com.gxt.common.dto.StrategySpecDto;
import com.gxt.common.exception.ResourceNotFoundException;
import com.gxt.common.market.MarketSeriesPort;
import com.gxt.strategybuilder.repository.StrategySpecSnapshotRepository;
import com.gxt.strategybuilder.service.StrategyCrudService;
import com.gxt.strategybuilder.service.StrategyPlanNormalizer;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BacktestJobExecutor {
    private static final Logger log = LoggerFactory.getLogger(BacktestJobExecutor.class);

    private final BacktestJobRepository jobRepository;
    private final BacktestReportRepository reportRepository;
    private final BacktestTradeRepository tradeRepository;
    private final StrategySpecSnapshotRepository snapshotRepository;
    private final StrategyCrudService strategyCrudService;
    private final StrategyPlanNormalizer normalizer;
    private final StrategySpecValidator validator;
    private final MarketSeriesPort marketSeriesService;
    private final HistoricalSimulationRunner simulationRunner;
    private final MetricsAnalyzer metricsAnalyzer;
    private final ReportCardGenerator reportCardGenerator;

    public BacktestJobExecutor(
            BacktestJobRepository jobRepository,
            BacktestReportRepository reportRepository,
            BacktestTradeRepository tradeRepository,
            StrategySpecSnapshotRepository snapshotRepository,
            StrategyCrudService strategyCrudService,
            StrategyPlanNormalizer normalizer,
            StrategySpecValidator validator,
            MarketSeriesPort marketSeriesService,
            HistoricalSimulationRunner simulationRunner,
            MetricsAnalyzer metricsAnalyzer,
            ReportCardGenerator reportCardGenerator) {
        this.jobRepository = jobRepository;
        this.reportRepository = reportRepository;
        this.tradeRepository = tradeRepository;
        this.snapshotRepository = snapshotRepository;
        this.strategyCrudService = strategyCrudService;
        this.normalizer = normalizer;
        this.validator = validator;
        this.marketSeriesService = marketSeriesService;
        this.simulationRunner = simulationRunner;
        this.metricsAnalyzer = metricsAnalyzer;
        this.reportCardGenerator = reportCardGenerator;
    }

    @Async("gxtTaskExecutor")
    public void executeAsync(String jobId) {
        BacktestJobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Backtest job not found: " + jobId));
        try {
            updateJobStatus(job, "running");
            strategyCrudService.markBacktestRunning(job.getStrategyId());

            var snapshot = snapshotRepository.findById(job.getSnapshotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Spec snapshot not found: " + job.getSnapshotId()));
            StrategySpecDto spec = normalizer.parseStored(snapshot.getSpecJson());
            validator.validate(spec);

            List<MarketBarDto> bars = marketSeriesService.getBars(
                    spec.instrument(),
                    spec.backtestWindow().from(),
                    spec.backtestWindow().to());

            HistoricalSimulationRunner.SimulationResult simulation = simulationRunner.simulate(spec, bars);
            var metrics = metricsAnalyzer.analyze(simulation);
            BacktestReportEntity report = reportCardGenerator.buildEntity(job.getStrategyId(), spec, metrics, simulation);
            reportRepository.save(report);
            tradeRepository.saveAll(reportCardGenerator.buildTrades(report.getId(), simulation));

            job.setStatus("completed");
            job.setReportId(report.getId());
            job.setUpdatedAt(Instant.now());
            jobRepository.save(job);
            strategyCrudService.markBacktested(job.getStrategyId(), report.getId());
        } catch (Exception ex) {
            log.error("Backtest job failed: {}", jobId, ex);
            job.setStatus("failed");
            job.setErrorMessage(ex.getMessage());
            job.setUpdatedAt(Instant.now());
            jobRepository.save(job);
        }
    }

    @Transactional
    protected void updateJobStatus(BacktestJobEntity job, String status) {
        job.setStatus(status);
        job.setUpdatedAt(Instant.now());
        jobRepository.save(job);
    }
}
