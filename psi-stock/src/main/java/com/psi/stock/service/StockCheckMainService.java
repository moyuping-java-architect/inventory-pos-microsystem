package com.psi.stock.service;

import com.psi.stock.dto.StockCheckMainDTO;
import com.psi.stock.dto.StockCheckQueryDTO;
import com.psi.stock.dto.StockCheckSaveDTO;
import com.psi.stock.entity.StockCheckMainEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface StockCheckMainService extends IService<StockCheckMainEntity> {

    CommonResult<StockCheckMainDTO> getById(Long id);

    PageResult<StockCheckMainDTO> list(StockCheckQueryDTO queryDTO);

    CommonResult<StockCheckMainDTO> save(StockCheckSaveDTO saveDTO);

    CommonResult<StockCheckMainDTO> update(Long id, StockCheckSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateStatus(Long id, Integer status);

    CommonResult<Void> audit(Long id, Integer auditStatus);
}