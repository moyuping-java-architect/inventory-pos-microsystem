package com.trademaster.workflow.strategy;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcessCompletedStrategyFactory {

    private final List<ProcessCompletedStrategy> strategies;
    private final ProcessCompletedStrategy defaultStrategy;

    public ProcessCompletedStrategyFactory(List<ProcessCompletedStrategy> strategies) {
        this.strategies = strategies;
        this.defaultStrategy = new CommonProcessCompletedStrategy();
    }

    public ProcessCompletedStrategy getStrategy(String bizType) {
        if (bizType == null || bizType.isEmpty()) {
            return defaultStrategy;
        }
        for (ProcessCompletedStrategy strategy : strategies) {
            if (strategy.supports(bizType)) {
                return strategy;
            }
        }
        return defaultStrategy;
    }
}
