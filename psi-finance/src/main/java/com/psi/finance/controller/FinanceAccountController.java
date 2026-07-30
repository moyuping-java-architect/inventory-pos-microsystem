package com.psi.finance.controller;

import com.psi.finance.dto.FinanceAccountDTO;
import com.psi.finance.dto.FinanceAccountFlowDTO;
import com.psi.finance.dto.FinanceAccountFlowQueryDTO;
import com.psi.finance.service.FinanceAccountService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/finance/account")
public class FinanceAccountController {

    private final FinanceAccountService financeAccountService;

    public FinanceAccountController(FinanceAccountService financeAccountService) {
        this.financeAccountService = financeAccountService;
    }

    @GetMapping("/{id}")
    public CommonResult<FinanceAccountDTO> getById(@PathVariable Long id) {
        return financeAccountService.getById(id);
    }

    @GetMapping("/store/{storeCode}")
    public CommonResult<PageResult<FinanceAccountDTO>> listByStore(@PathVariable String storeCode) {
        return CommonResult.success(financeAccountService.listByStore(storeCode));
    }

    @PostMapping("/flow/list")
    public CommonResult<PageResult<FinanceAccountFlowDTO>> listFlow(@RequestBody FinanceAccountFlowQueryDTO queryDTO) {
        return CommonResult.success(financeAccountService.listFlow(queryDTO));
    }
}