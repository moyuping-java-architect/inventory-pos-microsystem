package com.psi.sale.service;

import com.psi.sale.dto.SaleOutMainDTO;
import com.psi.sale.dto.SaleOutQueryDTO;
import com.psi.sale.dto.SaleOutSaveDTO;
import com.psi.sale.entity.SaleOutMainEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SaleOutMainService extends IService<SaleOutMainEntity> {

    CommonResult<SaleOutMainDTO> getById(Long id);

    PageResult<SaleOutMainDTO> list(SaleOutQueryDTO queryDTO);

    CommonResult<SaleOutMainDTO> save(SaleOutSaveDTO saveDTO);

    CommonResult<SaleOutMainDTO> update(Long id, SaleOutSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateStatus(Long id, Integer status);

    CommonResult<Void> audit(Long id, Integer auditStatus);
}