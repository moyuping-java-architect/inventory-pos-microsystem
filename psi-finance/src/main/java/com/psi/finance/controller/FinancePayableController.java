package com.psi.finance.controller;

import com.psi.finance.dto.FinancePayableDTO;
import com.psi.finance.dto.FinancePayablePayDTO;
import com.psi.finance.dto.FinancePayableQueryDTO;
import com.psi.finance.dto.FinancePaySaveDTO;
import com.psi.finance.service.FinancePayableService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/finance/payable")
public class FinancePayableController {

    private final FinancePayableService financePayableService;

    public FinancePayableController(FinancePayableService financePayableService) {
        this.financePayableService = financePayableService;
    }

    @GetMapping("/{id}")
    public CommonResult<FinancePayableDTO> getById(@PathVariable Long id) {
        return financePayableService.getById(id);
    }

    @PostMapping("/list")
    public PageResult<FinancePayableDTO> list(@RequestBody FinancePayableQueryDTO queryDTO) {
        return financePayableService.list(queryDTO);
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public CommonResult<FinancePayablePayDTO> pay(@PathVariable Long id, @RequestBody FinancePaySaveDTO saveDTO) {
        return financePayableService.pay(id, saveDTO);
    }
}