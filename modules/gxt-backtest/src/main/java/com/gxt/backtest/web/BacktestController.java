package com.gxt.backtest.web;

import com.gxt.backtest.dto.BacktestJobResponse;
import com.gxt.backtest.dto.SubmitBacktestResponse;
import com.gxt.backtest.engine.ReportCardGenerator;
import com.gxt.backtest.job.BacktestJobService;
import com.gxt.backtest.repository.BacktestReportRepository;
import com.gxt.common.dto.BacktestReportDto;
import com.gxt.common.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BacktestController {
    private final BacktestJobService backtestJobService;
    private final BacktestReportRepository reportRepository;
    private final ReportCardGenerator reportCardGenerator;

    public BacktestController(
            BacktestJobService backtestJobService,
            BacktestReportRepository reportRepository,
            ReportCardGenerator reportCardGenerator) {
        this.backtestJobService = backtestJobService;
        this.reportRepository = reportRepository;
        this.reportCardGenerator = reportCardGenerator;
    }

    @PostMapping("/strategies/{strategyId}/backtest")
    public ResponseEntity<SubmitBacktestResponse> submit(@PathVariable String strategyId) {
        SubmitBacktestResponse response = backtestJobService.submit(strategyId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/backtests/jobs/{jobId}")
    public BacktestJobResponse jobStatus(@PathVariable String jobId) {
        return backtestJobService.getJob(jobId);
    }

    @GetMapping("/backtests/{reportId}")
    public BacktestReportDto getReport(@PathVariable String reportId) {
        return reportRepository.findById(reportId)
                .map(reportCardGenerator::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Backtest report not found: " + reportId));
    }

    @GetMapping("/strategies/{strategyId}/backtest/latest")
    public BacktestReportDto latestReport(@PathVariable String strategyId) {
        return reportRepository.findTopByStrategyIdOrderByCreatedAtDesc(strategyId)
                .map(reportCardGenerator::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("No backtest report for strategy: " + strategyId));
    }
}
