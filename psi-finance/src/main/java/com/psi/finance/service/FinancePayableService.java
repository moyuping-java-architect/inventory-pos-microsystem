package com.psi.finance.service;

import com.psi.finance.dto.FinancePayableDTO;
import com.psi.finance.dto.FinancePayablePayDTO;
import com.psi.finance.dto.FinancePayableQueryDTO;
import com.psi.finance.dto.FinancePaySaveDTO;
import com.psi.finance.entity.FinancePayableEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface FinancePayableService extends IService<FinancePayableEntity> {

    CommonResult<FinancePayableDTO> getById(Long id);

    PageResult<FinancePayableDTO> list(FinancePayableQueryDTO queryDTO);

    CommonResult<FinancePayablePayDTO> pay(Long payableId, FinancePaySaveDTO saveDTO);
}