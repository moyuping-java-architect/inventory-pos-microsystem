package com.psi.flow.strategy;

import com.psi.order.constant.DocTypeConstant.BizType;
import com.psi.order.strategy.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 流程完成MQ策略工厂单元测试
 */
class ProcessCompletedMqStrategyFactoryTest {

    private final CommonProcessCompletedMqStrategy commonStrategy = new CommonProcessCompletedMqStrategy();
    private final PurchaseProcessCompletedMqStrategy purchaseStrategy = new PurchaseProcessCompletedMqStrategy();
    private final SaleProcessCompletedMqStrategy saleStrategy = new SaleProcessCompletedMqStrategy();
    private final StockProcessCompletedMqStrategy stockStrategy = new StockProcessCompletedMqStrategy();
    private final CheckProcessCompletedMqStrategy checkStrategy = new CheckProcessCompletedMqStrategy();
    private final LossProcessCompletedMqStrategy lossStrategy = new LossProcessCompletedMqStrategy();
    private final OverflowProcessCompletedMqStrategy overflowStrategy = new OverflowProcessCompletedMqStrategy();
    private final GoodsProcessCompletedMqStrategy goodsStrategy = new GoodsProcessCompletedMqStrategy();

    private final ProcessCompletedMqStrategyFactory factory = new ProcessCompletedMqStrategyFactory(
            List.of(commonStrategy, purchaseStrategy, saleStrategy, stockStrategy,
                    checkStrategy, lossStrategy, overflowStrategy, goodsStrategy)
    );

    @Test
    void getStrategy_shouldMatchBizTypeDirectly() {
        assertEquals(purchaseStrategy, factory.getStrategy(BizType.PURCHASE.getCode()));
        assertEquals(saleStrategy, factory.getStrategy(BizType.SALE.getCode()));
        assertEquals(stockStrategy, factory.getStrategy(BizType.STOCK.getCode()));
        assertEquals(commonStrategy, factory.getStrategy(BizType.COMMON.getCode()));
    }

    @Test
    void getStrategy_shouldMapDocTypeToBizType() {
        assertEquals(purchaseStrategy, factory.getStrategy("PURCHASE_ORDER"));
        assertEquals(purchaseStrategy, factory.getStrategy("PURCHASE_IN"));
        assertEquals(purchaseStrategy, factory.getStrategy("PURCHASE_RETURN"));
        assertEquals(saleStrategy, factory.getStrategy("SALE_ORDER"));
        assertEquals(saleStrategy, factory.getStrategy("SALE_OUT"));
        assertEquals(saleStrategy, factory.getStrategy("SALE_RETURN"));
        assertEquals(checkStrategy, factory.getStrategy("STOCK_CHECK"));
        assertEquals(stockStrategy, factory.getStrategy("STOCK_TRANSFER"));
        assertEquals(lossStrategy, factory.getStrategy("STOCK_LOSS"));
        assertEquals(overflowStrategy, factory.getStrategy("STOCK_OVERFLOW"));
        assertEquals(stockStrategy, factory.getStrategy("INVENTORY_INIT"));
        assertEquals(stockStrategy, factory.getStrategy("ADJUST_PRICE"));
    }

    @Test
    void getStrategy_shouldReturnDefaultForUnknown() {
        assertEquals(commonStrategy, factory.getStrategy("UNKNOWN"));
        assertEquals(commonStrategy, factory.getStrategy((String) null));
        assertEquals(commonStrategy, factory.getStrategy(""));
    }

    @Test
    void getStrategy_shouldBeCaseInsensitive() {
        assertEquals(purchaseStrategy, factory.getStrategy("purchase_order"));
        assertEquals(saleStrategy, factory.getStrategy("sale_out"));
        assertEquals(checkStrategy, factory.getStrategy("stock_check"));
    }
}
