package com.psi.flow.strategy;

import com.psi.order.constant.DocTypeConstant.DocType;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 采购审批工作流策略
 * 适用于采购订单、采购入库单
 */
@Component
public class PurchaseDocWorkflowStrategy implements DocWorkflowStrategy {

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            DocType.PURCHASE_ORDER.getCode(),
            DocType.PURCHASE_IN.getCode()
    );

    @Override
    public boolean supports(String docType) {
        return SUPPORTED_TYPES.contains(docType);
    }

    @Override
    public String getProcessKey() {
        return "PURCHASE_APPROVAL";
    }
}