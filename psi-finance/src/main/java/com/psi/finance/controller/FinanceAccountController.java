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

    @GetMapping("/list")
    public PageResult<FinanceAccountDTO> list() {
        return financeAccountService.listByStore(null);
    }

    @GetMapping("/store/{storeCode}")
    public PageResult<FinanceAccountDTO> listByStore(@PathVariable String storeCode) {
        return financeAccountService.listByStore(storeCode);
    }

    @PostMapping("/flow/list")
    public PageResult<FinanceAccountFlowDTO> listFlow(@RequestBody FinanceAccountFlowQueryDTO queryDTO) {
        return financeAccountService.listFlow(queryDTO);
    }
}
