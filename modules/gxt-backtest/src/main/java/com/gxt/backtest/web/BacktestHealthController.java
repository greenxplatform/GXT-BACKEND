package com.gxt.backtest.web;

import com.gxt.common.dto.ModuleHealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/backtest")
public class BacktestHealthController {

    @GetMapping("/health")
    public ModuleHealthResponse health() {
        return new ModuleHealthResponse(
                "gxt-backtest",
                "active",
                "M4-sub",
                "Deterministic backtest engine with async job queue");
    }
}
