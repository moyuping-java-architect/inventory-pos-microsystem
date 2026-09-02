package com.psi.stock.service;

import com.psi.stock.dto.StockFlowDTO;
import com.psi.stock.dto.StockFlowQueryDTO;
import com.psi.stock.entity.StockFlowEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

public interface StockFlowService extends IService<StockFlowEntity> {

    CommonResult<StockFlowDTO> getById(Long id);

    PageResult<StockFlowDTO> list(StockFlowQueryDTO queryDTO);

    CommonResult<Void> addFlow(String warehouseCode, String warehouseName, String goodsCode, String skuCode, String goodsName,
                               String goodsSpec, String unit, Integer flowType, BigDecimal inQuantity,
                               BigDecimal outQuantity, BigDecimal beforeQuantity, BigDecimal afterQuantity,
                               BigDecimal costPrice, BigDecimal amount, String sourceNo, String sourceType, String remark);
}