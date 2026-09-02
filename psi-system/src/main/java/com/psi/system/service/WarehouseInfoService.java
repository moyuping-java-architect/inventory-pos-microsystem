package com.psi.system.service;

import com.psi.system.dto.WarehouseInfoDTO;
import com.psi.system.dto.WarehouseInfoQueryDTO;
import com.psi.system.dto.WarehouseInfoSaveDTO;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;

public interface WarehouseInfoService {

    CommonResult<WarehouseInfoDTO> getById(Long id);

    PageResult<WarehouseInfoDTO> list(WarehouseInfoQueryDTO queryDTO);

    CommonResult<WarehouseInfoDTO> save(WarehouseInfoSaveDTO saveDTO);

    CommonResult<WarehouseInfoDTO> update(Long id, WarehouseInfoSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateStatus(Long id, Integer status);
}