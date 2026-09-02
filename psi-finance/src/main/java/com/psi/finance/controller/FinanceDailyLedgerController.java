package com.psi.finance.controller;

import com.psi.finance.dto.FinanceDailyLedgerDTO;
import com.psi.finance.dto.FinanceDailyLedgerQueryDTO;
import com.psi.finance.service.FinanceDailyLedgerService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/psi/finance/ledger")
public class FinanceDailyLedgerController {

    private final FinanceDailyLedgerService financeDailyLedgerService;

    public FinanceDailyLedgerController(FinanceDailyLedgerService financeDailyLedgerService) {
        this.financeDailyLedgerService = financeDailyLedgerService;
    }

    @GetMapping("/{id}")
    public CommonResult<FinanceDailyLedgerDTO> getById(@PathVariable Long id) {
        return financeDailyLedgerService.getById(id);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<FinanceDailyLedgerDTO>> list(@RequestBody FinanceDailyLedgerQueryDTO queryDTO) {
        return CommonResult.success(financeDailyLedgerService.list(queryDTO));
    }

    @GetMapping("/query")
    public CommonResult<FinanceDailyLedgerDTO> getByDate(@RequestParam String storeCode, @RequestParam String ledgerDate) {
        return financeDailyLedgerService.getByDate(storeCode, ledgerDate);
    }

    @GetMapping("/summary")
    public CommonResult<Map<String, BigDecimal>> summary(@RequestParam String storeCode, 
                                                         @RequestParam String startDate, 
                                                         @RequestParam String endDate) {
        return financeDailyLedgerService.summary(storeCode, startDate, endDate);
    }

    @GetMapping("/summary/all")
    public CommonResult<Map<String, BigDecimal>> summaryAll(@RequestParam String startDate, 
                                                            @RequestParam String endDate) {
        return financeDailyLedgerService.summaryAll(startDate, endDate);
    }
}