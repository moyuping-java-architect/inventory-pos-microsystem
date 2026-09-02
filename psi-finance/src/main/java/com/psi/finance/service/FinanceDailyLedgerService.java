package com.psi.finance.service;

import com.psi.finance.dto.FinanceDailyLedgerDTO;
import com.psi.finance.dto.FinanceDailyLedgerQueryDTO;
import com.psi.finance.entity.FinanceDailyLedgerEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.Map;

public interface FinanceDailyLedgerService extends IService<FinanceDailyLedgerEntity> {

    CommonResult<FinanceDailyLedgerDTO> getById(Long id);

    PageResult<FinanceDailyLedgerDTO> list(FinanceDailyLedgerQueryDTO queryDTO);

    CommonResult<FinanceDailyLedgerDTO> getByDate(String storeCode, String ledgerDate);

    CommonResult<Map<String, BigDecimal>> summary(String storeCode, String startDate, String endDate);

    CommonResult<Map<String, BigDecimal>> summaryAll(String startDate, String endDate);

    CommonResult<Void> recordSale(String outNo, String customerCode, String customerName, 
                                   BigDecimal totalAmount, BigDecimal payAmount, String payType, String warehouseCode);
}