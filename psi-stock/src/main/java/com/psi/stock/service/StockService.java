package com.psi.stock.service;

import com.psi.stock.dto.StockBatchOperateItemDTO;
import com.psi.stock.dto.StockDTO;
import com.psi.stock.dto.StockQueryDTO;
import com.psi.stock.entity.StockEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

public interface StockService extends IService<StockEntity> {

    CommonResult<StockDTO> getById(Long id);

    PageResult<StockDTO> list(StockQueryDTO queryDTO);

    CommonResult<Void> updateStatus(Long id, Integer status);

    CommonResult<StockDTO> getStock(String warehouseCode, String skuCode);

    CommonResult<Void> increaseStock(String warehouseCode, String goodsCode, String skuCode, BigDecimal quantity, BigDecimal costPrice, String sourceNo, String sourceType);

    CommonResult<Void> decreaseStock(String warehouseCode, String skuCode, BigDecimal quantity, String sourceNo, String sourceType);

    CommonResult<Void> lockStock(String warehouseCode, String skuCode, BigDecimal quantity, String sourceNo, String sourceType);

    CommonResult<Void> releaseStock(String warehouseCode, String skuCode, BigDecimal quantity, String sourceNo, String sourceType);

    CommonResult<Void> confirmStock(String warehouseCode, String skuCode, BigDecimal quantity, String sourceNo, String sourceType);

    /**
     * 批量预占库存（一条 SQL）
     */
    CommonResult<Void> batchLockStock(List<StockBatchOperateItemDTO> items, String sourceNo, String sourceType);

    /**
     * 批量扣减实际库存（一条 SQL）
     */
    CommonResult<Void> batchDecreaseStock(List<StockBatchOperateItemDTO> items, String sourceNo, String sourceType);

    /**
     * 批量释放预占库存（一条 SQL）
     */
    CommonResult<Void> batchReleaseStock(List<StockBatchOperateItemDTO> items, String sourceNo, String sourceType);

    /**
     * 批量确认出库：实际库存和锁定库存同时扣减（一条 SQL）
     */
    CommonResult<Void> batchConfirmStock(List<StockBatchOperateItemDTO> items, String sourceNo, String sourceType);
}