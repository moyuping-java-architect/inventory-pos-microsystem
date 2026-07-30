package com.trademaster.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trademaster.stock.entity.Stock;
import com.trademaster.stock.entity.StockFlow;
import com.trademaster.stock.mapper.StockFlowMapper;
import com.trademaster.stock.mapper.StockMapper;
import com.trademaster.stock.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class StockServiceImpl implements StockService {

    private final StockMapper stockMapper;
    private final StockFlowMapper stockFlowMapper;

    public StockServiceImpl(StockMapper stockMapper, StockFlowMapper stockFlowMapper) {
        this.stockMapper = stockMapper;
        this.stockFlowMapper = stockFlowMapper;
    }

    @Override
    @Transactional
    public Stock increaseStock(String warehouseCode, String goodsCode, String skuCode,
                               BigDecimal quantity, BigDecimal unitPrice, String batchNo,
                               String sourceNo, String sourceType) {
        log.info("入库操作: warehouseCode={}, skuCode={}, quantity={}, batchNo={}", warehouseCode, skuCode, quantity, batchNo);

        Stock existing = stockMapper.selectByWarehouseAndSkuAndBatch(warehouseCode, skuCode, batchNo);

        BigDecimal beforeQuantity = BigDecimal.ZERO;
        if (existing != null) {
            beforeQuantity = existing.getQuantity();
            BigDecimal newQuantity = beforeQuantity.add(quantity);
            BigDecimal totalCost = existing.getTotalAmount().add(quantity.multiply(unitPrice));
            BigDecimal newAvgCost = totalCost.divide(newQuantity, 4, BigDecimal.ROUND_HALF_UP);

            existing.setQuantity(newQuantity);
            existing.setAvailableQuantity(existing.getAvailableQuantity().add(quantity));
            existing.setAvgCostPrice(newAvgCost);
            existing.setTotalAmount(totalCost);
            existing.setUpdateTime(LocalDateTime.now());
            stockMapper.updateById(existing);
            log.info("库存已更新: id={}, quantity={}", existing.getId(), newQuantity);
        } else {
            Stock stock = new Stock();
            stock.setWarehouseCode(warehouseCode);
            stock.setGoodsCode(goodsCode);
            stock.setSkuCode(skuCode);
            stock.setQuantity(quantity);
            stock.setLockedQuantity(BigDecimal.ZERO);
            stock.setAvailableQuantity(quantity);
            stock.setAvgCostPrice(unitPrice);
            stock.setTotalAmount(quantity.multiply(unitPrice));
            stock.setBatchNo(batchNo);
            stock.setStatus(1);
            stock.setDelFlag(0);
            stock.setCreateTime(LocalDateTime.now());
            stockMapper.insert(stock);
            log.info("库存已新增: skuCode={}, quantity={}", skuCode, quantity);
            existing = stock;
        }

        saveStockFlow(warehouseCode, goodsCode, skuCode, "IN", quantity, beforeQuantity,
                existing.getQuantity(), unitPrice, sourceNo, sourceType);

        return existing;
    }

    @Override
    @Transactional
    public Stock decreaseStock(String warehouseCode, String goodsCode, String skuCode,
                               BigDecimal quantity, String sourceNo, String sourceType) {
        log.info("出库操作: warehouseCode={}, skuCode={}, quantity={}", warehouseCode, skuCode, quantity);

        Stock stock = stockMapper.selectByWarehouseAndSku(warehouseCode, skuCode);
        if (stock == null) {
            throw new RuntimeException("库存不存在: warehouseCode=" + warehouseCode + ", skuCode=" + skuCode);
        }

        BigDecimal beforeQuantity = stock.getAvailableQuantity();
        if (beforeQuantity.compareTo(quantity) < 0) {
            throw new RuntimeException("库存不足: available=" + beforeQuantity + ", required=" + quantity);
        }

        BigDecimal newQuantity = stock.getQuantity().subtract(quantity);
        BigDecimal newAvailable = stock.getAvailableQuantity().subtract(quantity);
        BigDecimal newTotalAmount = stock.getTotalAmount().subtract(quantity.multiply(stock.getAvgCostPrice()));

        stock.setQuantity(newQuantity);
        stock.setAvailableQuantity(newAvailable);
        stock.setTotalAmount(newTotalAmount);
        stock.setUpdateTime(LocalDateTime.now());
        stockMapper.updateById(stock);

        saveStockFlow(warehouseCode, goodsCode, skuCode, "OUT", quantity.negate(), beforeQuantity,
                newAvailable, stock.getAvgCostPrice(), sourceNo, sourceType);

        return stock;
    }

    @Override
    @Transactional
    public Stock lockStock(String warehouseCode, String goodsCode, String skuCode,
                           BigDecimal quantity) {
        Stock stock = stockMapper.selectByWarehouseAndSku(warehouseCode, skuCode);
        if (stock == null) {
            throw new RuntimeException("库存不存在");
        }

        if (stock.getAvailableQuantity().compareTo(quantity) < 0) {
            throw new RuntimeException("可用库存不足");
        }

        stock.setLockedQuantity(stock.getLockedQuantity().add(quantity));
        stock.setAvailableQuantity(stock.getAvailableQuantity().subtract(quantity));
        stock.setUpdateTime(LocalDateTime.now());
        stockMapper.updateById(stock);

        return stock;
    }

    @Override
    @Transactional
    public Stock releaseStock(String warehouseCode, String goodsCode, String skuCode,
                              BigDecimal quantity) {
        Stock stock = stockMapper.selectByWarehouseAndSku(warehouseCode, skuCode);
        if (stock == null) {
            throw new RuntimeException("库存不存在");
        }

        if (stock.getLockedQuantity().compareTo(quantity) < 0) {
            throw new RuntimeException("锁定库存不足");
        }

        stock.setLockedQuantity(stock.getLockedQuantity().subtract(quantity));
        stock.setAvailableQuantity(stock.getAvailableQuantity().add(quantity));
        stock.setUpdateTime(LocalDateTime.now());
        stockMapper.updateById(stock);

        return stock;
    }

    @Override
    public Stock getStock(String warehouseCode, String skuCode) {
        return stockMapper.selectByWarehouseAndSku(warehouseCode, skuCode);
    }

    private void saveStockFlow(String warehouseCode, String goodsCode, String skuCode,
                               String flowType, BigDecimal quantity, BigDecimal beforeQuantity,
                               BigDecimal afterQuantity, BigDecimal avgCostPrice,
                               String sourceNo, String sourceType) {
        StockFlow flow = new StockFlow();
        flow.setWarehouseCode(warehouseCode);
        flow.setGoodsCode(goodsCode);
        flow.setSkuCode(skuCode);
        flow.setFlowType(flowType);
        flow.setQuantity(quantity);
        flow.setBeforeQuantity(beforeQuantity);
        flow.setAfterQuantity(afterQuantity);
        flow.setAvgCostPrice(avgCostPrice);
        flow.setAmount(quantity.multiply(avgCostPrice));
        flow.setSourceNo(sourceNo);
        flow.setSourceType(sourceType);
        flow.setCreateTime(LocalDateTime.now());
        stockFlowMapper.insert(flow);
    }
}
