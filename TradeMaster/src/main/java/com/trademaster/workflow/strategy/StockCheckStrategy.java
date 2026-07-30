package com.trademaster.workflow.strategy;

import com.trademaster.stock.service.StockCheckMainService;
import com.trademaster.workflow.entity.WfProcessInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class StockCheckStrategy implements ProcessCompletedStrategy {

    private static final Set<String> SUPPORTED_TYPES = Set.of("STOCK_CHECK");

    private final StockCheckMainService stockCheckMainService;

    public StockCheckStrategy(StockCheckMainService stockCheckMainService) {
        this.stockCheckMainService = stockCheckMainService;
    }

    @Override
    public boolean supports(String bizType) {
        return SUPPORTED_TYPES.contains(bizType);
    }

    @Override
    public void handleProcessCompleted(WfProcessInstance processInstance, String bizId) {
        log.info("盘点单审批通过，开始生成正式数据: processInstanceId={}, bizId={}", processInstance.getId(), bizId);
        try {
            Long id = Long.parseLong(bizId);
            stockCheckMainService.audit(id, 2);
            log.info("盘点单审核成功: bizId={}", bizId);
        } catch (Exception e) {
            log.error("盘点单审核失败: bizId={}, error={}", bizId, e.getMessage(), e);
            throw new RuntimeException("盘点单审核失败: " + e.getMessage(), e);
        }
    }
}
