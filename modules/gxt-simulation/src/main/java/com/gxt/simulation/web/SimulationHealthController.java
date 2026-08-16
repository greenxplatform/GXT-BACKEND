package com.gxt.simulation.web;

import com.gxt.common.dto.ModuleHealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simulation")
public class SimulationHealthController {

    @GetMapping("/health")
    public ModuleHealthResponse health() {
        return new ModuleHealthResponse(
                "gxt-simulation",
                "skeleton",
                "M6",
                "Live paper-trading terminal with OMS and capital allocator");
    }
}
