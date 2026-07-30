package com.trademaster.workflow.strategy;

import com.trademaster.purchase.service.PurchaseInMainService;
import com.trademaster.workflow.entity.WfProcessInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class PurchaseInStrategy implements ProcessCompletedStrategy {

    private static final Set<String> SUPPORTED_TYPES = Set.of("PURCHASE_IN");

    private final PurchaseInMainService purchaseInMainService;

    public PurchaseInStrategy(PurchaseInMainService purchaseInMainService) {
        this.purchaseInMainService = purchaseInMainService;
    }

    @Override
    public boolean supports(String bizType) {
        return SUPPORTED_TYPES.contains(bizType);
    }

    @Override
    public void handleProcessCompleted(WfProcessInstance processInstance, String bizId) {
        log.info("采购入库审批通过，开始生成正式数据: processInstanceId={}, bizId={}", processInstance.getId(), bizId);
        try {
            Long id = Long.parseLong(bizId);
            purchaseInMainService.audit(id, 2);
            log.info("采购入库单审核成功: bizId={}", bizId);
        } catch (Exception e) {
            log.error("采购入库单审核失败: bizId={}, error={}", bizId, e.getMessage(), e);
            throw new RuntimeException("采购入库单审核失败: " + e.getMessage(), e);
        }
    }
}
