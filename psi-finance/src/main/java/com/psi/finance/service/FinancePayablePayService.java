package com.psi.finance.service;

import com.psi.finance.dto.FinancePayablePayDTO;
import com.psi.finance.dto.FinancePaySaveDTO;
import com.psi.finance.entity.FinancePayablePayEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface FinancePayablePayService extends IService<FinancePayablePayEntity> {

    CommonResult<FinancePayablePayDTO> getById(Long id);

    PageResult<FinancePayablePayDTO> list(Long payableId);

    CommonResult<FinancePayablePayDTO> pay(FinancePaySaveDTO saveDTO);
}