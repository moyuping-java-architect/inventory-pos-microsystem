package com.psi.stock.service;

import com.psi.stock.dto.StockLossMainDTO;
import com.psi.stock.dto.StockLossQueryDTO;
import com.psi.stock.dto.StockLossSaveDTO;
import com.psi.stock.entity.StockLossMainEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface StockLossMainService extends IService<StockLossMainEntity> {

    CommonResult<StockLossMainDTO> getById(Long id);

    PageResult<StockLossMainDTO> list(StockLossQueryDTO queryDTO);

    CommonResult<StockLossMainDTO> save(StockLossSaveDTO saveDTO);

    CommonResult<StockLossMainDTO> update(Long id, StockLossSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateStatus(Long id, Integer status);

    CommonResult<Void> audit(Long id, Integer auditStatus);
}