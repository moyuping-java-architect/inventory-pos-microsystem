package com.psi.cashier.controller;

import com.psi.cashier.entity.CustomerEntity;
import com.psi.cashier.service.CustomerService;
import com.psi.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户控制器
 * POS端客户管理接口（数据来源于后台下行同步）
 */
@RestController
@RequestMapping("/psi/cashier/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 根据ID查询客户
     */
    @GetMapping("/{id}")
    public CommonResult<CustomerEntity> getById(@PathVariable Integer id) {
        CustomerEntity customer = customerService.getById(id);
        if (customer == null) {
            return CommonResult.fail("客户不存在");
        }
        return CommonResult.success(customer);
    }

    /**
     * 搜索客户（按名称/手机号/编码模糊匹配）
     */
    @GetMapping("/search")
    public CommonResult<List<CustomerEntity>> search(@RequestParam(required = false) String keyword) {
        return CommonResult.success(customerService.search(keyword));
    }

    /**
     * 客户转会员
     */
    @PostMapping("/{id}/convert-to-member")
    public CommonResult<Integer> convertToMember(@PathVariable Integer id) {
        try {
            Integer memberId = customerService.convertToMember(id);
            return CommonResult.success("客户转为会员成功", memberId);
        } catch (RuntimeException e) {
            return CommonResult.fail(e.getMessage());
        }
    }
}