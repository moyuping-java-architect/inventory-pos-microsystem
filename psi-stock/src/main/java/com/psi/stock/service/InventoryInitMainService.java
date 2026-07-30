package com.psi.stock.service;

import com.psi.stock.dto.InventoryInitSaveDTO;
import com.psi.stock.entity.InventoryInitMainEntity;
import com.psi.common.result.CommonResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface InventoryInitMainService extends IService<InventoryInitMainEntity> {

    CommonResult<Long> save(InventoryInitSaveDTO saveDTO);

    CommonResult<Void> audit(Long id, Integer auditStatus);
}