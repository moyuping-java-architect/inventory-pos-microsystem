package com.psi.finance.controller;

import com.psi.finance.dto.FinanceDailyCloseDTO;
import com.psi.finance.dto.FinanceDailyCloseQueryDTO;
import com.psi.finance.service.FinanceDailyCloseService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/finance/close")
public class FinanceDailyCloseController {

    private final FinanceDailyCloseService financeDailyCloseService;

    public FinanceDailyCloseController(FinanceDailyCloseService financeDailyCloseService) {
        this.financeDailyCloseService = financeDailyCloseService;
    }

    @GetMapping("/{id}")
    public CommonResult<FinanceDailyCloseDTO> getById(@PathVariable Long id) {
        return financeDailyCloseService.getById(id);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<FinanceDailyCloseDTO>> list(@RequestBody FinanceDailyCloseQueryDTO queryDTO) {
        return CommonResult.success(financeDailyCloseService.list(queryDTO));
    }

    @PostMapping
    public CommonResult<FinanceDailyCloseDTO> close(@RequestParam String storeCode, 
                                                    @RequestParam String closeDate, 
                                                    @RequestParam String closeBy) {
        return financeDailyCloseService.close(storeCode, closeDate, closeBy);
    }

    @PostMapping("/reopen")
    public CommonResult<FinanceDailyCloseDTO> reopen(@RequestParam String storeCode, 
                                                     @RequestParam String closeDate) {
        return financeDailyCloseService.reopen(storeCode, closeDate);
    }

    @GetMapping("/query")
    public CommonResult<FinanceDailyCloseDTO> getByDate(@RequestParam String storeCode, 
                                                        @RequestParam String closeDate) {
        return financeDailyCloseService.getByDate(storeCode, closeDate);
    }
}