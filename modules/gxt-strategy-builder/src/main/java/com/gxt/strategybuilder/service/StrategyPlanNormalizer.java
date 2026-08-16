package com.gxt.strategybuilder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gxt.common.dto.StrategySpecDto;
import com.gxt.common.exception.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class StrategyPlanNormalizer {
    private static final Set<String> INSTRUMENTS = Set.of(
            "NIFTY50", "SENSEX", "RELIANCE", "TCS", "INFY", "HDFCBANK", "ICICIBANK", "SBIN");
    private static final Set<String> TIMEFRAMES = Set.of("Daily");

    private final ObjectMapper objectMapper;

    public StrategyPlanNormalizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public NormalizationResult normalize(JsonNode rawNode) {
        List<String> errors = new ArrayList<>();

        String instrument = text(rawNode, "instrument", errors);
        String timeframe = text(rawNode, "timeframe", errors);
        String direction = text(rawNode, "direction", errors);
        List<String> entryRules = stringList(rawNode, "entryRules", errors);
        List<String> exitRules = stringList(rawNode, "exitRules", errors);

        StrategySpecDto.StopLossDto stopLoss = parseStopLoss(rawNode.path("stopLoss"), errors);
        StrategySpecDto.BacktestWindowDto window = parseWindow(rawNode.path("backtestWindow"), errors);

        if (instrument != null && !INSTRUMENTS.contains(instrument)) {
            errors.add("Unsupported instrument: " + instrument);
        }
        if (timeframe != null && !TIMEFRAMES.contains(timeframe)) {
            errors.add("Unsupported timeframe: " + timeframe);
        }
        validateRules(entryRules, errors, "entry");
        validateRules(exitRules, errors, "exit");

        if (!errors.isEmpty()) {
            return NormalizationResult.invalid(errors);
        }

        StrategySpecDto spec = new StrategySpecDto(
                instrument,
                timeframe,
                direction != null ? direction : "long",
                entryRules,
                exitRules,
                stopLoss,
                window);
        return NormalizationResult.valid(spec);
    }

    public StrategySpecDto parseStored(String specJson) {
        try {
            JsonNode node = objectMapper.readTree(specJson);
            NormalizationResult result = normalize(node);
            if (!result.valid()) {
                throw new ValidationException("Stored strategy spec is invalid", result.errors());
            }
            return result.spec();
        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ValidationException("Invalid stored strategy spec", List.of(ex.getMessage()));
        }
    }

    private void validateRules(List<String> rules, List<String> errors, String label) {
        if (rules == null || rules.isEmpty()) {
            errors.add("Missing " + label + " rules");
            return;
        }
        for (String rule : rules) {
            if (!isSupportedRule(rule)) {
                errors.add("Unsupported " + label + " rule: " + rule);
            }
        }
    }

    private boolean isSupportedRule(String rule) {
        String normalized = rule.toLowerCase();
        return normalized.contains("sma_20")
                || normalized.contains("rsi_14")
                || normalized.contains("take profit")
                || normalized.contains("stop");
    }

    private String text(JsonNode node, String field, List<String> errors) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.asText().isBlank()) {
            errors.add("Missing field: " + field);
            return null;
        }
        return value.asText();
    }

    private List<String> stringList(JsonNode node, String field, List<String> errors) {
        JsonNode value = node.path(field);
        if (!value.isArray() || value.isEmpty()) {
            errors.add("Missing field: " + field);
            return List.of();
        }
        List<String> out = new ArrayList<>();
        value.forEach(item -> out.add(item.asText()));
        return out;
    }

    private StrategySpecDto.StopLossDto parseStopLoss(JsonNode node, List<String> errors) {
        if (node.isMissingNode()) {
            errors.add("Missing field: stopLoss");
            return new StrategySpecDto.StopLossDto("percent", 2.0);
        }
        String type = node.path("type").asText("percent");
        double value = node.path("value").asDouble(2.0);
        return new StrategySpecDto.StopLossDto(type, value);
    }

    private StrategySpecDto.BacktestWindowDto parseWindow(JsonNode node, List<String> errors) {
        if (node.isMissingNode()) {
            errors.add("Missing field: backtestWindow");
            return new StrategySpecDto.BacktestWindowDto(LocalDate.now().minusYears(2), LocalDate.now());
        }
        try {
            LocalDate from = LocalDate.parse(node.path("from").asText());
            LocalDate to = LocalDate.parse(node.path("to").asText());
            return new StrategySpecDto.BacktestWindowDto(from, to);
        } catch (Exception ex) {
            errors.add("Invalid backtestWindow dates");
            return new StrategySpecDto.BacktestWindowDto(LocalDate.now().minusYears(2), LocalDate.now());
        }
    }

    public record NormalizationResult(boolean valid, StrategySpecDto spec, List<String> errors) {
        public static NormalizationResult valid(StrategySpecDto spec) {
            return new NormalizationResult(true, spec, List.of());
        }

        public static NormalizationResult invalid(List<String> errors) {
            return new NormalizationResult(false, null, errors);
        }
    }
}
