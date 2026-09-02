package com.psi.finance.service;

import com.psi.finance.dto.FinanceAccountDTO;
import com.psi.finance.dto.FinanceAccountFlowDTO;
import com.psi.finance.dto.FinanceAccountFlowQueryDTO;
import com.psi.finance.entity.FinanceAccountEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

public interface FinanceAccountService extends IService<FinanceAccountEntity> {

    CommonResult<FinanceAccountDTO> getById(Long id);

    PageResult<FinanceAccountDTO> listByStore(String storeCode);

    PageResult<FinanceAccountFlowDTO> listFlow(FinanceAccountFlowQueryDTO queryDTO);

    CommonResult<Void> addFlow(String storeCode, String accountType, Integer flowType, 
                                BigDecimal amount, String sourceNo, String sourceType, String payNo, String remark);
}