package com.psi.stock.service;

import com.psi.stock.dto.StockBatchDTO;
import com.psi.stock.dto.StockBatchQueryDTO;
import com.psi.stock.entity.StockBatchEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

public interface StockBatchService extends IService<StockBatchEntity> {

    CommonResult<StockBatchDTO> getById(Long id);

    PageResult<StockBatchDTO> list(StockBatchQueryDTO queryDTO);

    CommonResult<Void> updateStatus(Long id, Integer status);

    CommonResult<Void> increaseBatch(String warehouseCode, String warehouseName, String goodsCode, String goodsName,
                                      String goodsSpec, String unit, String batchNo, String productionDate,
                                      String expireDate, BigDecimal quantity, BigDecimal costPrice,
                                      String supplierCode, String supplierName);

    CommonResult<Void> decreaseBatch(String warehouseCode, String goodsCode, String batchNo, BigDecimal quantity);
}