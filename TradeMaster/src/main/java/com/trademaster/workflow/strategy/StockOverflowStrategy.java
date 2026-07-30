package com.trademaster.workflow.strategy;

import com.trademaster.stock.service.StockOverflowMainService;
import com.trademaster.workflow.entity.WfProcessInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class StockOverflowStrategy implements ProcessCompletedStrategy {

    private static final Set<String> SUPPORTED_TYPES = Set.of("STOCK_OVERFLOW");

    private final StockOverflowMainService stockOverflowMainService;

    public StockOverflowStrategy(StockOverflowMainService stockOverflowMainService) {
        this.stockOverflowMainService = stockOverflowMainService;
    }

    @Override
    public boolean supports(String bizType) {
        return SUPPORTED_TYPES.contains(bizType);
    }

    @Override
    public void handleProcessCompleted(WfProcessInstance processInstance, String bizId) {
        log.info("报溢单审批通过，开始生成正式数据: processInstanceId={}, bizId={}", processInstance.getId(), bizId);
        try {
            Long id = Long.parseLong(bizId);
            stockOverflowMainService.audit(id, 2);
            log.info("报溢单审核成功: bizId={}", bizId);
        } catch (Exception e) {
            log.error("报溢单审核失败: bizId={}, error={}", bizId, e.getMessage(), e);
            throw new RuntimeException("报溢单审核失败: " + e.getMessage(), e);
        }
    }
}
