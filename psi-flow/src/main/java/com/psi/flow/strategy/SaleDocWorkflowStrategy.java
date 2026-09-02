package com.psi.flow.strategy;

import com.psi.order.constant.DocTypeConstant.DocType;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 销售审批工作流策略
 * 适用于销售订单、销售出库单
 */
@Component
public class SaleDocWorkflowStrategy implements DocWorkflowStrategy {

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            DocType.SALE_ORDER.getCode(),
            DocType.SALE_OUT.getCode()
    );

    @Override
    public boolean supports(String docType) {
        return SUPPORTED_TYPES.contains(docType);
    }

    @Override
    public String getProcessKey() {
        return "SALE_APPROVAL";
    }
}