package com.trademaster.stock.service;

import com.trademaster.stock.entity.Stock;

import java.math.BigDecimal;

public interface StockService {

    Stock increaseStock(String warehouseCode, String goodsCode, String skuCode,
                        BigDecimal quantity, BigDecimal unitPrice, String batchNo,
                        String sourceNo, String sourceType);

    Stock decreaseStock(String warehouseCode, String goodsCode, String skuCode,
                        BigDecimal quantity, String sourceNo, String sourceType);

    Stock lockStock(String warehouseCode, String goodsCode, String skuCode,
                    BigDecimal quantity);

    Stock releaseStock(String warehouseCode, String goodsCode, String skuCode,
                       BigDecimal quantity);

    Stock getStock(String warehouseCode, String skuCode);
}
