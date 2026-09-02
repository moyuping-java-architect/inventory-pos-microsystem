package com.psi.purchase.service;

import com.psi.purchase.dto.PurchaseReturnMainDTO;
import com.psi.purchase.dto.PurchaseReturnQueryDTO;
import com.psi.purchase.dto.PurchaseReturnSaveDTO;
import com.psi.purchase.entity.PurchaseReturnMainEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PurchaseReturnMainService extends IService<PurchaseReturnMainEntity> {

    CommonResult<PurchaseReturnMainDTO> getById(Long id);

    PageResult<PurchaseReturnMainDTO> list(PurchaseReturnQueryDTO queryDTO);

    CommonResult<PurchaseReturnMainDTO> save(PurchaseReturnSaveDTO saveDTO);

    CommonResult<PurchaseReturnMainDTO> update(Long id, PurchaseReturnSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> audit(Long id, Integer auditStatus);

    CommonResult<Void> updateStatus(Long id, Integer status);
}