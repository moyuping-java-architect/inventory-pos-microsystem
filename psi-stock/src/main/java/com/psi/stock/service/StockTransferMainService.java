package com.psi.stock.service;

import com.psi.stock.dto.StockTransferMainDTO;
import com.psi.stock.dto.StockTransferQueryDTO;
import com.psi.stock.dto.StockTransferSaveDTO;
import com.psi.stock.entity.StockTransferMainEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface StockTransferMainService extends IService<StockTransferMainEntity> {

    CommonResult<StockTransferMainDTO> getById(Long id);

    PageResult<StockTransferMainDTO> list(StockTransferQueryDTO queryDTO);

    CommonResult<StockTransferMainDTO> save(StockTransferSaveDTO saveDTO);

    CommonResult<StockTransferMainDTO> update(Long id, StockTransferSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateStatus(Long id, Integer status);

    CommonResult<Void> audit(Long id, Integer auditStatus);
}