package com.psi.flow.strategy;

import com.psi.order.constant.DocTypeConstant.DocType;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 报损工作流策略
 */
@Component
public class StockLossDocWorkflowStrategy implements DocWorkflowStrategy {

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            DocType.STOCK_LOSS.getCode()
    );

    @Override
    public boolean supports(String docType) {
        return SUPPORTED_TYPES.contains(docType);
    }

    @Override
    public String getProcessKey() {
        return "STOCK_LOSS";
    }
}