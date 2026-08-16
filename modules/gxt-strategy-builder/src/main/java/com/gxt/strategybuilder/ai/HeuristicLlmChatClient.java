package com.gxt.strategybuilder.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service("heuristicLlmChatClient")
public class HeuristicLlmChatClient implements LlmChatClient {
    private final ObjectMapper objectMapper;

    public HeuristicLlmChatClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public LlmChatResult chat(String systemPrompt, String userPrompt, String conversationContext) {
        String text = userPrompt.toLowerCase();
        ObjectNode plan = objectMapper.createObjectNode();
        plan.put("instrument", text.contains("reliance") ? "RELIANCE" : text.contains("sensex") ? "SENSEX" : "NIFTY50");
        plan.put("timeframe", "Daily");
        plan.put("direction", "long");

        if (text.contains("rsi")) {
            plan.putArray("entryRules").add("RSI_14 crosses above 30");
            plan.putArray("exitRules").add("Take profit at +3% from entry");
            ObjectNode stop = plan.putObject("stopLoss");
            stop.put("type", "percent");
            stop.put("value", 3);
        } else {
            plan.putArray("entryRules").add("Close crosses above SMA_20");
            plan.putArray("exitRules").add("Close crosses below SMA_20");
            ObjectNode stop = plan.putObject("stopLoss");
            stop.put("type", "percent");
            stop.put("value", text.contains("3%") ? 3 : 2);
        }

        ObjectNode window = plan.putObject("backtestWindow");
        window.put("from", LocalDate.now().minusYears(2).toString());
        window.put("to", LocalDate.now().toString());

        String assistant = "I translated your idea into a rule-based draft strategy card. Review the rules and backtest when ready.";
        try {
            return new LlmChatResult(assistant, objectMapper.writeValueAsString(plan));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to build heuristic plan", ex);
        }
    }
}
