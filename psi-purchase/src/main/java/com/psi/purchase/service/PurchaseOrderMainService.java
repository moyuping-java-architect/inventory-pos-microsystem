package com.psi.purchase.service;

import com.psi.purchase.dto.PurchaseOrderMainDTO;
import com.psi.purchase.dto.PurchaseOrderQueryDTO;
import com.psi.purchase.dto.PurchaseOrderSaveDTO;
import com.psi.purchase.entity.PurchaseOrderMainEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PurchaseOrderMainService extends IService<PurchaseOrderMainEntity> {

    CommonResult<PurchaseOrderMainDTO> getById(Long id);

    PageResult<PurchaseOrderMainDTO> list(PurchaseOrderQueryDTO queryDTO);

    CommonResult<PurchaseOrderMainDTO> save(PurchaseOrderSaveDTO saveDTO);

    CommonResult<PurchaseOrderMainDTO> update(Long id, PurchaseOrderSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> audit(Long id, Integer auditStatus);

    CommonResult<Void> updateStatus(Long id, Integer status);
}