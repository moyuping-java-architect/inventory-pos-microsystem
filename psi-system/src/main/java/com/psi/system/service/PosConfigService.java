package com.psi.system.service;

import com.psi.system.dto.PosConfigDTO;
import com.psi.system.dto.PosConfigQueryDTO;
import com.psi.system.dto.PosConfigSaveDTO;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;

public interface PosConfigService {

    CommonResult<PosConfigDTO> getById(Long id);

    PageResult<PosConfigDTO> list(PosConfigQueryDTO queryDTO);

    CommonResult<PosConfigDTO> save(PosConfigSaveDTO saveDTO);

    CommonResult<PosConfigDTO> update(Long id, PosConfigSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateStatus(Long id, Integer status);
}