package com.gxt.strategybuilder.web;

import com.gxt.common.web.UserContext;
import com.gxt.strategybuilder.dto.CreateStrategyRequest;
import com.gxt.strategybuilder.dto.StrategyResponse;
import com.gxt.strategybuilder.dto.UpdateStrategyRequest;
import com.gxt.strategybuilder.service.StrategyCrudService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/strategies")
public class StrategyController {
    private final StrategyCrudService strategyCrudService;
    private final UserContext userContext;

    public StrategyController(StrategyCrudService strategyCrudService, UserContext userContext) {
        this.strategyCrudService = strategyCrudService;
        this.userContext = userContext;
    }

    @PostMapping
    public StrategyResponse create(@Valid @RequestBody CreateStrategyRequest request, HttpServletRequest httpRequest) {
        return strategyCrudService.create(userContext.resolveUserId(httpRequest), request);
    }

    @GetMapping
    public List<StrategyResponse> list(HttpServletRequest httpRequest) {
        return strategyCrudService.listByUser(userContext.resolveUserId(httpRequest));
    }

    @GetMapping("/{id}")
    public StrategyResponse get(@PathVariable String id) {
        return strategyCrudService.get(id);
    }

    @PatchMapping("/{id}")
    public StrategyResponse update(@PathVariable String id, @RequestBody UpdateStrategyRequest request) {
        return strategyCrudService.update(id, request);
    }
}
