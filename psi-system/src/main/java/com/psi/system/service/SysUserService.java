package com.psi.system.service;

import com.psi.system.dto.SysUserDTO;
import com.psi.system.dto.SysUserQueryDTO;
import com.psi.system.dto.SysUserSaveDTO;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;

public interface SysUserService {

    CommonResult<SysUserDTO> getById(Long id);

    PageResult<SysUserDTO> list(SysUserQueryDTO queryDTO);

    CommonResult<SysUserDTO> save(SysUserSaveDTO saveDTO);

    CommonResult<SysUserDTO> update(Long id, SysUserSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateStatus(Long id, Integer status);
}