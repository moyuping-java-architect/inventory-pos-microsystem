package com.psi.finance.controller;

import com.psi.finance.dto.FinanceReceivableDTO;
import com.psi.finance.dto.FinanceReceivablePayDTO;
import com.psi.finance.dto.FinanceReceivableQueryDTO;
import com.psi.finance.dto.FinancePaySaveDTO;
import com.psi.finance.service.FinanceReceivableService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/finance/receivable")
public class FinanceReceivableController {

    private final FinanceReceivableService financeReceivableService;

    public FinanceReceivableController(FinanceReceivableService financeReceivableService) {
        this.financeReceivableService = financeReceivableService;
    }

    @GetMapping("/{id}")
    public CommonResult<FinanceReceivableDTO> getById(@PathVariable Long id) {
        return financeReceivableService.getById(id);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<FinanceReceivableDTO>> list(@RequestBody FinanceReceivableQueryDTO queryDTO) {
        return CommonResult.success(financeReceivableService.list(queryDTO));
    }

    @PostMapping("/{id}/pay")
    public CommonResult<FinanceReceivablePayDTO> pay(@PathVariable Long id, @RequestBody FinancePaySaveDTO saveDTO) {
        return financeReceivableService.pay(id, saveDTO);
    }
}