package com.trademaster.workflow.strategy;

import com.trademaster.sale.service.SaleReturnMainService;
import com.trademaster.workflow.entity.WfProcessInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class SaleReturnStrategy implements ProcessCompletedStrategy {

    private static final Set<String> SUPPORTED_TYPES = Set.of("SALE_RETURN");

    private final SaleReturnMainService saleReturnMainService;

    public SaleReturnStrategy(SaleReturnMainService saleReturnMainService) {
        this.saleReturnMainService = saleReturnMainService;
    }

    @Override
    public boolean supports(String bizType) {
        return SUPPORTED_TYPES.contains(bizType);
    }

    @Override
    public void handleProcessCompleted(WfProcessInstance processInstance, String bizId) {
        log.info("销售退货审批通过，开始生成正式数据: processInstanceId={}, bizId={}", processInstance.getId(), bizId);
        try {
            Long id = Long.parseLong(bizId);
            saleReturnMainService.audit(id, 2);
            log.info("销售退货单审核成功: bizId={}", bizId);
        } catch (Exception e) {
            log.error("销售退货单审核失败: bizId={}, error={}", bizId, e.getMessage(), e);
            throw new RuntimeException("销售退货单审核失败: " + e.getMessage(), e);
        }
    }
}
