package com.psi.stock.service;

import com.psi.stock.dto.StockOverMainDTO;
import com.psi.stock.dto.StockOverQueryDTO;
import com.psi.stock.dto.StockOverSaveDTO;
import com.psi.stock.entity.StockOverMainEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface StockOverMainService extends IService<StockOverMainEntity> {

    CommonResult<StockOverMainDTO> getById(Long id);

    PageResult<StockOverMainDTO> list(StockOverQueryDTO queryDTO);

    CommonResult<StockOverMainDTO> save(StockOverSaveDTO saveDTO);

    CommonResult<StockOverMainDTO> update(Long id, StockOverSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateStatus(Long id, Integer status);

    CommonResult<Void> audit(Long id, Integer auditStatus);
}