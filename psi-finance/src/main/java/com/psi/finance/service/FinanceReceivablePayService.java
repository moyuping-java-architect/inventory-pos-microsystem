package com.psi.finance.service;

import com.psi.finance.dto.FinanceReceivablePayDTO;
import com.psi.finance.dto.FinancePaySaveDTO;
import com.psi.finance.entity.FinanceReceivablePayEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface FinanceReceivablePayService extends IService<FinanceReceivablePayEntity> {

    CommonResult<FinanceReceivablePayDTO> getById(Long id);

    PageResult<FinanceReceivablePayDTO> list(Long receivableId);

    CommonResult<FinanceReceivablePayDTO> pay(FinancePaySaveDTO saveDTO);
}