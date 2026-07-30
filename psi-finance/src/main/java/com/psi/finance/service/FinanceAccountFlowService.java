package com.psi.finance.service;

import com.psi.finance.dto.FinanceAccountFlowDTO;
import com.psi.finance.dto.FinanceAccountFlowQueryDTO;
import com.psi.finance.entity.FinanceAccountFlowEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface FinanceAccountFlowService extends IService<FinanceAccountFlowEntity> {

    CommonResult<FinanceAccountFlowDTO> getById(Long id);

    PageResult<FinanceAccountFlowDTO> list(FinanceAccountFlowQueryDTO queryDTO);

    CommonResult<Void> addFlow(String storeCode, String storeName, String accountType, String accountName, 
                                 Integer flowType, String inAmount, String outAmount, 
                                 String balanceBefore, String balanceAfter, 
                                 String sourceNo, String sourceType, String payNo, String remark);
}