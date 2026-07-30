package com.psi.sale.service;

import com.psi.sale.dto.CustomerDebtDTO;
import com.psi.sale.dto.CustomerDebtQueryDTO;
import com.psi.sale.entity.CustomerDebtEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

public interface CustomerDebtService extends IService<CustomerDebtEntity> {

    CommonResult<CustomerDebtDTO> getById(Long id);

    PageResult<CustomerDebtDTO> list(CustomerDebtQueryDTO queryDTO);

    CommonResult<CustomerDebtDTO> getByCustomerId(Long customerId);

    void addDebt(Long customerId, String customerCode, String customerName, String billNo, 
                 Integer billType, BigDecimal amount, String debtDate, String dueDate);

    void payDebt(Long customerId, BigDecimal amount);

    CommonResult<Void> updateStatus(Long id, Integer status);
}