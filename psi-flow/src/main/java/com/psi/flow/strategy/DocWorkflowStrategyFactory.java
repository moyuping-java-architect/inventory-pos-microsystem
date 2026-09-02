package com.psi.flow.strategy;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 单据工作流策略工厂
 * 自动注册所有策略，根据单据类型匹配对应的工作流
 */
@Component
public class DocWorkflowStrategyFactory {

    private final List<DocWorkflowStrategy> strategies;

    private final DocWorkflowStrategy defaultStrategy;

    public DocWorkflowStrategyFactory(List<DocWorkflowStrategy> strategies) {
        this.strategies = strategies;
        this.defaultStrategy = new CommonDocWorkflowStrategy();
    }

    /**
     * 根据单据类型获取对应的工作流策略
     */
    public DocWorkflowStrategy getStrategy(String docType) {
        if (docType == null || docType.isEmpty()) {
            return defaultStrategy;
        }
        for (DocWorkflowStrategy strategy : strategies) {
            if (strategy.supports(docType)) {
                return strategy;
            }
        }
        return defaultStrategy;
    }

    /**
     * 根据单据类型获取工作流 processKey
     */
    public String getProcessKey(String docType) {
        return getStrategy(docType).getProcessKey();
    }
}