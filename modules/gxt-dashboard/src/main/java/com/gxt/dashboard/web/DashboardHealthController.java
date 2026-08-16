package com.gxt.dashboard.web;

import com.gxt.common.dto.ModuleHealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardHealthController {

    @GetMapping("/health")
    public ModuleHealthResponse health() {
        return new ModuleHealthResponse(
                "gxt-dashboard",
                "skeleton",
                "M3",
                "Dashboard shell — module unlock rules and navigation metadata");
    }
}
