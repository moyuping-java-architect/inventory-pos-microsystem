package com.trademaster.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.trademaster.common.PageResult;
import com.trademaster.common.Result;
import com.trademaster.dto.SaleOrderDTO;
import com.trademaster.entity.Customer;
import com.trademaster.entity.SaleOrder;
import com.trademaster.service.CashierService;
import com.trademaster.service.CustomerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cashier")
public class CashierController {
    private final CashierService cashierService;
    private final CustomerService customerService;

    public CashierController(CashierService cashierService, CustomerService customerService) {
        this.cashierService = cashierService;
        this.customerService = customerService;
    }

    @PostMapping("/order")
    public Result<SaleOrder> createOrder(@RequestBody SaleOrderDTO dto) {
        SaleOrder order = cashierService.createOrder(dto);
        return Result.success(order);
    }

    @GetMapping("/order/{orderNo}")
    public Result<SaleOrder> getOrder(@PathVariable String orderNo) {
        return Result.success(cashierService.findByOrderNo(orderNo));
    }

    @GetMapping("/customer/{phone}")
    public Result<Customer> findCustomer(@PathVariable String phone) {
        Customer customer = customerService.findByPhone(phone);
        if (customer == null) {
            return Result.error("会员不存在");
        }
        return Result.success(customer);
    }

    @PostMapping("/customer")
    public Result<Customer> saveCustomer(@RequestBody Customer customer) {
        customerService.save(customer);
        return Result.success(customer);
    }
}
