package com.psi.flow.strategy;

import com.psi.order.constant.DocTypeConstant.DocType;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 报溢/盘点/库存调整工作流策略
 * 适用于报溢单、盘点单、库存初始化单、调价单
 */
@Component
public class StockOverflowDocWorkflowStrategy implements DocWorkflowStrategy {

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            DocType.STOCK_OVERFLOW.getCode(),
            DocType.STOCK_CHECK.getCode(),
            DocType.INVENTORY_INIT.getCode(),
            DocType.ADJUST_PRICE.getCode()
    );

    @Override
    public boolean supports(String docType) {
        return SUPPORTED_TYPES.contains(docType);
    }

    @Override
    public String getProcessKey() {
        return "STOCK_OVERFLOW";
    }
}