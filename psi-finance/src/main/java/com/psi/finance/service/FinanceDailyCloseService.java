package com.psi.finance.service;

import com.psi.finance.dto.FinanceDailyCloseDTO;
import com.psi.finance.dto.FinanceDailyCloseQueryDTO;
import com.psi.finance.entity.FinanceDailyCloseEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface FinanceDailyCloseService extends IService<FinanceDailyCloseEntity> {

    CommonResult<FinanceDailyCloseDTO> getById(Long id);

    PageResult<FinanceDailyCloseDTO> list(FinanceDailyCloseQueryDTO queryDTO);

    CommonResult<FinanceDailyCloseDTO> close(String storeCode, String closeDate, String closeBy);

    CommonResult<FinanceDailyCloseDTO> reopen(String storeCode, String closeDate);

    CommonResult<FinanceDailyCloseDTO> getByDate(String storeCode, String closeDate);
}