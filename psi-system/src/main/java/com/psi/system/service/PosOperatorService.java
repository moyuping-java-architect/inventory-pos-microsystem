package com.psi.system.service;

import com.psi.system.dto.PosOperatorDTO;
import com.psi.system.dto.PosOperatorQueryDTO;
import com.psi.system.dto.PosOperatorSaveDTO;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;

public interface PosOperatorService {

    CommonResult<PosOperatorDTO> getById(Long id);

    PageResult<PosOperatorDTO> list(PosOperatorQueryDTO queryDTO);

    CommonResult<PosOperatorDTO> save(PosOperatorSaveDTO saveDTO);

    CommonResult<PosOperatorDTO> update(Long id, PosOperatorSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateStatus(Long id, Integer status);
}