package com.gxt.strategybuilder.web;

import com.gxt.common.dto.ModuleHealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/strategy-builder")
public class StrategyBuilderHealthController {

    @GetMapping("/health")
    public ModuleHealthResponse health() {
        return new ModuleHealthResponse(
                "gxt-strategy-builder",
                "active",
                "M4",
                "AI Strategy chat, strategy CRUD, and live strategy card");
    }
}
