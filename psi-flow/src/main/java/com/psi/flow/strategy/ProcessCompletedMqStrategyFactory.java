package com.psi.flow.strategy;

import com.psi.common.constant.DocBizMappingEnum;
import com.psi.order.strategy.ProcessCompletedMqStrategy;
import com.psi.order.constant.DocTypeConstant.BizType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程完成MQ发送策略工厂
 * 负责策略的注册、获取和管理
 *
 * <p>策略查找逻辑：
 * <ol>
 *   <li>精确匹配：bizType 直接作为 key 查找（如 "PURCHASE" → PurchaseProcessCompletedMqStrategy）</li>
 *   <li>映射匹配：通过 {@link DocBizMappingEnum} 将单据类型映射为业务类型再查找
 *       （如 "PURCHASE_ORDER" → "PURCHASE" → PurchaseProcessCompletedMqStrategy）</li>
 *   <li>兜底策略：返回 {@link com.psi.order.strategy.CommonProcessCompletedMqStrategy}</li>
 * </ol>
 */
@Component
public class ProcessCompletedMqStrategyFactory {

    private final Map<String, ProcessCompletedMqStrategy> strategyMap = new ConcurrentHashMap<>();

    private final ProcessCompletedMqStrategy defaultStrategy;

    public ProcessCompletedMqStrategyFactory(List<ProcessCompletedMqStrategy> strategies) {
        if (strategies != null && !strategies.isEmpty()) {
            for (ProcessCompletedMqStrategy strategy : strategies) {
                try {
                    String bizType = strategy.getBizType();
                    if (bizType != null) {
                        strategyMap.put(bizType.toUpperCase(), strategy);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        ProcessCompletedMqStrategy def = null;
        try {
            def = strategyMap.getOrDefault(BizType.COMMON.getCode(), null);
        } catch (Exception ignored) {
        }
        if (def == null && !strategyMap.isEmpty()) {
            def = strategyMap.values().iterator().next();
        }
        this.defaultStrategy = def;
    }

    /**
     * 根据业务类型或单据类型获取MQ策略
     *
     * <p>支持两种输入：
     * <ul>
     *   <li>业务类型：如 "PURCHASE"、"SALE"</li>
     *   <li>单据类型：如 "PURCHASE_ORDER"、"SALE_OUT"</li>
     * </ul>
     */
    public ProcessCompletedMqStrategy getStrategy(String bizType) {
        if (bizType == null || bizType.trim().isEmpty()) {
            return defaultStrategy;
        }
        String upper = bizType.toUpperCase().trim();
        ProcessCompletedMqStrategy s = strategyMap.get(upper);
        if (s != null) {
            return s;
        }
        String mappedBizType = DocBizMappingEnum.getBizTypeCode(upper);
        if (!DocBizMappingEnum.BizTypeEnum.COMMON.getCode().equals(mappedBizType)) {
            s = strategyMap.get(mappedBizType);
            if (s != null) {
                return s;
            }
        }
        return defaultStrategy;
    }

    public ProcessCompletedMqStrategy getStrategy(BizType bizType) {
        if (bizType == null) {
            return defaultStrategy;
        }
        ProcessCompletedMqStrategy s = strategyMap.get(bizType.getCode());
        return s != null ? s : defaultStrategy;
    }

    public String getExchange(String bizType) {
        ProcessCompletedMqStrategy s = getStrategy(bizType);
        return s != null ? s.getExchange() : "";
    }

    public String getExchange(BizType bizType) {
        ProcessCompletedMqStrategy s = getStrategy(bizType);
        return s != null ? s.getExchange() : "";
    }

    public String getRoutingKey(String bizType) {
        ProcessCompletedMqStrategy s = getStrategy(bizType);
        return s != null ? s.getRoutingKey() : "";
    }

    public String getRoutingKey(BizType bizType) {
        ProcessCompletedMqStrategy s = getStrategy(bizType);
        return s != null ? s.getRoutingKey() : "";
    }

    public boolean hasStrategy() {
        return defaultStrategy != null;
    }
}