package com.psi.system.service;

import com.psi.system.dto.ShopInfoDTO;
import com.psi.system.dto.ShopInfoQueryDTO;
import com.psi.system.dto.ShopInfoSaveDTO;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;

public interface ShopInfoService {

    CommonResult<ShopInfoDTO> getById(Long id);

    PageResult<ShopInfoDTO> list(ShopInfoQueryDTO queryDTO);

    CommonResult<ShopInfoDTO> save(ShopInfoSaveDTO saveDTO);

    CommonResult<ShopInfoDTO> update(Long id, ShopInfoSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateStatus(Long id, Integer status);
}