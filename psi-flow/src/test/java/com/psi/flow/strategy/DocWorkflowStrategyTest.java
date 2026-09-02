package com.psi.flow.strategy;

import com.psi.order.constant.DocTypeConstant.DocType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 单据工作流策略单元测试
 */
class DocWorkflowStrategyTest {

    @Test
    void stockCheckStrategy_shouldSupportStockCheck() {
        StockCheckDocWorkflowStrategy strategy = new StockCheckDocWorkflowStrategy();
        assertTrue(strategy.supports(DocType.STOCK_CHECK.getCode()));
        assertFalse(strategy.supports(DocType.SALE_ORDER.getCode()));
        assertEquals("STOCK_CHECK", strategy.getProcessKey());
    }

    @Test
    void inventoryInitStrategy_shouldSupportInventoryInit() {
        InventoryInitDocWorkflowStrategy strategy = new InventoryInitDocWorkflowStrategy();
        assertTrue(strategy.supports(DocType.INVENTORY_INIT.getCode()));
        assertFalse(strategy.supports(DocType.PURCHASE_ORDER.getCode()));
        assertEquals("INVENTORY_INIT", strategy.getProcessKey());
    }

    @Test
    void adjustPriceStrategy_shouldSupportAdjustPrice() {
        AdjustPriceDocWorkflowStrategy strategy = new AdjustPriceDocWorkflowStrategy();
        assertTrue(strategy.supports(DocType.ADJUST_PRICE.getCode()));
        assertFalse(strategy.supports(DocType.PURCHASE_IN.getCode()));
        assertEquals("ADJUST_PRICE", strategy.getProcessKey());
    }
}
