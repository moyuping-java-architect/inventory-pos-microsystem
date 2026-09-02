package com.psi.sale.service;

import com.psi.sale.dto.SaleReturnMainDTO;
import com.psi.sale.dto.SaleReturnQueryDTO;
import com.psi.sale.dto.SaleReturnSaveDTO;
import com.psi.sale.entity.SaleReturnMainEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SaleReturnMainService extends IService<SaleReturnMainEntity> {

    CommonResult<SaleReturnMainDTO> getById(Long id);

    PageResult<SaleReturnMainDTO> list(SaleReturnQueryDTO queryDTO);

    CommonResult<SaleReturnMainDTO> save(SaleReturnSaveDTO saveDTO);

    CommonResult<SaleReturnMainDTO> update(Long id, SaleReturnSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateStatus(Long id, Integer status);

    CommonResult<Void> audit(Long id, Integer auditStatus);
}