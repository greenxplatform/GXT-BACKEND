package com.gxt.strategybuilder.dto;

import com.gxt.common.dto.StrategySpecDto;
import java.util.List;

public record ChatResponse(
        String assistantMessage,
        StrategyResponse strategyCard,
        List<String> validationErrors
) {}
