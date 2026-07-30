package com.trademaster.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.trademaster.common.PageResult;
import com.trademaster.common.Result;
import com.trademaster.entity.Customer;
import com.trademaster.service.CustomerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/page")
    public Result<PageResult<Customer>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        IPage<Customer> result = customerService.findPage(page, size, keyword);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), (int) result.getSize(), (int) result.getCurrent()));
    }

    @GetMapping("/{id}")
    public Result<Customer> getById(@PathVariable Long id) {
        return Result.success(customerService.findById(id));
    }

    @PostMapping
    public Result<Customer> save(@RequestBody Customer customer) {
        customerService.save(customer);
        return Result.success(customer);
    }

    @PutMapping
    public Result<Customer> update(@RequestBody Customer customer) {
        customerService.save(customer);
        return Result.success(customer);
    }

    @PutMapping("/{id}/recharge")
    public Result<Void> recharge(@PathVariable Long id, @RequestParam java.math.BigDecimal amount) {
        customerService.updateBalance(id, amount);
        return Result.success();
    }
}
