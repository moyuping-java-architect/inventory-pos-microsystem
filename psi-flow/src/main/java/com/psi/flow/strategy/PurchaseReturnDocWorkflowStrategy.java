package com.psi.flow.strategy;

import com.psi.order.constant.DocTypeConstant.DocType;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 采购退货工作流策略
 */
@Component
public class PurchaseReturnDocWorkflowStrategy implements DocWorkflowStrategy {

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            DocType.PURCHASE_RETURN.getCode()
    );

    @Override
    public boolean supports(String docType) {
        return SUPPORTED_TYPES.contains(docType);
    }

    @Override
    public String getProcessKey() {
        return "PURCHASE_RETURN";
    }
}