package com.psi.sale.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.sale.entity.SaleOutSelfUseMainEntity;
import com.psi.sale.service.SaleOutSelfUseMainService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/psi/sale/self-use-out")
public class SaleOutSelfUseController {

    private final SaleOutSelfUseMainService saleOutSelfUseMainService;

    public SaleOutSelfUseController(SaleOutSelfUseMainService saleOutSelfUseMainService) {
        this.saleOutSelfUseMainService = saleOutSelfUseMainService;
    }

    @GetMapping("/{id}")
    public CommonResult<SaleOutSelfUseMainEntity> getById(@PathVariable Long id) {
        return CommonResult.success(saleOutSelfUseMainService.getDetail(id));
    }

    @PostMapping("/list")
    public CommonResult<PageResult<SaleOutSelfUseMainEntity>> list(@RequestBody Map<String, Object> params) {
        Long pageNum = params.get("pageNum") != null ? Long.valueOf(params.get("pageNum").toString()) : 1L;
        Long pageSize = params.get("pageSize") != null ? Long.valueOf(params.get("pageSize").toString()) : 10L;
        IPage<SaleOutSelfUseMainEntity> page = saleOutSelfUseMainService.page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SaleOutSelfUseMainEntity>().eq(SaleOutSelfUseMainEntity::getDelFlag, 0)
                        .orderByDesc(SaleOutSelfUseMainEntity::getCreateTime));
        return CommonResult.success(PageResult.success(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }
}
