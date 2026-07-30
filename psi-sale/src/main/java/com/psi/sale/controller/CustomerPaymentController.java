package com.psi.sale.controller;

import com.psi.sale.dto.CustomerPaymentDTO;
import com.psi.sale.dto.CustomerPaymentQueryDTO;
import com.psi.sale.dto.CustomerPaymentSaveDTO;
import com.psi.sale.service.CustomerPaymentService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/sale/payment")
public class CustomerPaymentController {

    private final CustomerPaymentService customerPaymentService;

    public CustomerPaymentController(CustomerPaymentService customerPaymentService) {
        this.customerPaymentService = customerPaymentService;
    }

    @GetMapping("/{id}")
    public CommonResult<CustomerPaymentDTO> getById(@PathVariable Long id) {
        return customerPaymentService.getById(id);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<CustomerPaymentDTO>> list(@RequestBody CustomerPaymentQueryDTO queryDTO) {
        return CommonResult.success(customerPaymentService.list(queryDTO));
    }

    @PostMapping
    public CommonResult<CustomerPaymentDTO> save(@RequestBody CustomerPaymentSaveDTO saveDTO) {
        return customerPaymentService.save(saveDTO);
    }

    @PutMapping("/{id}/status/{status}")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        return customerPaymentService.updateStatus(id, status);
    }
}