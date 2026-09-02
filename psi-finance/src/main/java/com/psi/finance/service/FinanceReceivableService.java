package com.psi.finance.service;

import com.psi.finance.dto.FinanceReceivableDTO;
import com.psi.finance.dto.FinanceReceivablePayDTO;
import com.psi.finance.dto.FinanceReceivableQueryDTO;
import com.psi.finance.dto.FinancePaySaveDTO;
import com.psi.finance.entity.FinanceReceivableEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface FinanceReceivableService extends IService<FinanceReceivableEntity> {

    CommonResult<FinanceReceivableDTO> getById(Long id);

    PageResult<FinanceReceivableDTO> list(FinanceReceivableQueryDTO queryDTO);

    CommonResult<FinanceReceivablePayDTO> pay(Long receivableId, FinancePaySaveDTO saveDTO);
}