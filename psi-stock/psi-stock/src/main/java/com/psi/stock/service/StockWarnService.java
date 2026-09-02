package com.psi.stock.service;

import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.stock.dto.StockWarnDTO;
import com.psi.stock.dto.StockWarnQueryDTO;
import com.psi.stock.entity.StockWarnEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface StockWarnService extends IService<StockWarnEntity> {

    PageResult<StockWarnDTO> page(StockWarnQueryDTO queryDTO);

    CommonResult<Void> add(StockWarnDTO dto);

    CommonResult<Void> update(StockWarnDTO dto);

    CommonResult<Void> delete(Long id);

    CommonResult<List<StockWarnDTO>> getLowStockList(String warehouseCode);

    CommonResult<List<StockWarnDTO>> getHighStockList(String warehouseCode);

    CommonResult<Integer> getLowStockCount(String warehouseCode);

    CommonResult<Integer> getHighStockCount(String warehouseCode);

    void checkAndGenerateWarn(String warehouseCode, String skuCode);
}
