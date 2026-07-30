package com.psi.purchase.service;

import com.psi.purchase.dto.PurchaseInMainDTO;
import com.psi.purchase.dto.PurchaseInQueryDTO;
import com.psi.purchase.dto.PurchaseInSaveDTO;
import com.psi.purchase.entity.PurchaseInMainEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PurchaseInMainService extends IService<PurchaseInMainEntity> {

    CommonResult<PurchaseInMainDTO> getById(Long id);

    PageResult<PurchaseInMainDTO> list(PurchaseInQueryDTO queryDTO);

    CommonResult<PurchaseInMainDTO> save(PurchaseInSaveDTO saveDTO);

    CommonResult<PurchaseInMainDTO> update(Long id, PurchaseInSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> audit(Long id, Integer auditStatus);

    CommonResult<Void> updateStatus(Long id, Integer status);
}