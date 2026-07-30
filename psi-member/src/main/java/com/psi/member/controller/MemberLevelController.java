package com.psi.member.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psi.member.dto.MemberLevelDTO;
import com.psi.member.entity.MemberLevel;
import com.psi.member.service.MemberLevelService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/member/level")
@Tag(name = "会员等级", description = "会员等级管理")
public class MemberLevelController {

    private final MemberLevelService memberLevelService;

    public MemberLevelController(MemberLevelService memberLevelService) {
        this.memberLevelService = memberLevelService;
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有等级")
    public CommonResult<List<MemberLevel>> list() {
        return CommonResult.success(memberLevelService.list());
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询等级")
    public CommonResult<PageResult<MemberLevel>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        Page<MemberLevel> page = memberLevelService.page(pageNum, pageSize);
        return CommonResult.success(PageResult.convert(page, java.util.function.Function.identity()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询等级")
    public CommonResult<MemberLevel> getById(@PathVariable Long id) {
        return CommonResult.success(memberLevelService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增等级")
    public CommonResult<Void> add(@RequestBody MemberLevelDTO dto) {
        return memberLevelService.add(dto);
    }

    @PutMapping
    @Operation(summary = "修改等级")
    public CommonResult<Void> update(@RequestBody MemberLevelDTO dto) {
        return memberLevelService.update(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除等级")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return memberLevelService.delete(id);
    }

    @GetMapping("/discount")
    @Operation(summary = "计算会员折扣价")
    public CommonResult<BigDecimal> calculateDiscount(@RequestParam Long memberId,
                                                       @RequestParam BigDecimal amount) {
        return CommonResult.success(memberLevelService.calculateDiscount(memberId, amount));
    }

    @GetMapping("/points/calculate")
    @Operation(summary = "计算获得积分")
    public CommonResult<Integer> calculateEarnPoints(@RequestParam Long memberId,
                                                     @RequestParam BigDecimal amount) {
        return CommonResult.success(memberLevelService.calculateEarnPoints(memberId, amount));
    }
}
