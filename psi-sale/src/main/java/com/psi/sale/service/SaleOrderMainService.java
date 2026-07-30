package com.psi.sale.service;

import com.psi.sale.dto.SaleOrderMainDTO;
import com.psi.sale.dto.SaleOrderQueryDTO;
import com.psi.sale.dto.SaleOrderSaveDTO;
import com.psi.sale.entity.SaleOrderMainEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SaleOrderMainService extends IService<SaleOrderMainEntity> {

    CommonResult<SaleOrderMainDTO> getById(Long id);

    PageResult<SaleOrderMainDTO> list(SaleOrderQueryDTO queryDTO);

    CommonResult<SaleOrderMainDTO> save(SaleOrderSaveDTO saveDTO);

    CommonResult<SaleOrderMainDTO> update(Long id, SaleOrderSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateStatus(Long id, Integer status);

    CommonResult<Void> audit(Long id, Integer auditStatus);
}