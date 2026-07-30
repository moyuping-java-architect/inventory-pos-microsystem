package com.psi.sale.controller;

import com.psi.sale.dto.CustomerDebtDTO;
import com.psi.sale.dto.CustomerDebtQueryDTO;
import com.psi.sale.service.CustomerDebtService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/sale/debt")
public class CustomerDebtController {

    private final CustomerDebtService customerDebtService;

    public CustomerDebtController(CustomerDebtService customerDebtService) {
        this.customerDebtService = customerDebtService;
    }

    @GetMapping("/{id}")
    public CommonResult<CustomerDebtDTO> getById(@PathVariable Long id) {
        return customerDebtService.getById(id);
    }

    @GetMapping("/customer/{customerId}")
    public CommonResult<CustomerDebtDTO> getByCustomerId(@PathVariable Long customerId) {
        return customerDebtService.getByCustomerId(customerId);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<CustomerDebtDTO>> list(@RequestBody CustomerDebtQueryDTO queryDTO) {
        return CommonResult.success(customerDebtService.list(queryDTO));
    }

    @PutMapping("/{id}/status/{status}")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        return customerDebtService.updateStatus(id, status);
    }
}