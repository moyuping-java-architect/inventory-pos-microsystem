package com.psi.system.service;

import com.psi.system.dto.SysLoginDTO;
import com.psi.system.dto.SysLoginResponseDTO;
import com.psi.common.result.CommonResult;

public interface SysLoginService {

    CommonResult<SysLoginResponseDTO> login(SysLoginDTO loginDTO);

    CommonResult<Void> logout();

    CommonResult<SysLoginResponseDTO> refreshToken(String refreshToken);
}