package com.gxt.backtest.engine;

import com.gxt.common.dto.StrategySpecDto;
import com.gxt.common.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StrategySpecValidator {
    public void validate(StrategySpecDto spec) {
        List<String> errors = new ArrayList<>();
        if (spec.instrument() == null || spec.instrument().isBlank()) {
            errors.add("instrument is required");
        }
        if (spec.timeframe() == null || spec.timeframe().isBlank()) {
            errors.add("timeframe is required");
        }
        if (spec.entryRules() == null || spec.entryRules().isEmpty()) {
            errors.add("entryRules are required");
        }
        if (spec.exitRules() == null || spec.exitRules().isEmpty()) {
            errors.add("exitRules are required");
        }
        if (spec.stopLoss() == null) {
            errors.add("stopLoss is required");
        }
        if (spec.backtestWindow() == null) {
            errors.add("backtestWindow is required");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Invalid strategy spec for backtest", errors);
        }
    }
}
