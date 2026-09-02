package com.psi.member.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psi.member.dto.MemberInfoDTO;
import com.psi.member.dto.MemberInfoQueryDTO;
import com.psi.member.entity.MemberInfo;
import com.psi.member.service.MemberInfoService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/member/info")
@Tag(name = "会员管理", description = "会员信息管理")
public class MemberInfoController {

    private final MemberInfoService memberInfoService;

    public MemberInfoController(MemberInfoService memberInfoService) {
        this.memberInfoService = memberInfoService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询会员")
    public CommonResult<PageResult<MemberInfo>> page(MemberInfoQueryDTO queryDTO) {
        Page<MemberInfo> page = memberInfoService.page(queryDTO);
        return CommonResult.success(PageResult.convert(page, java.util.function.Function.identity()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询会员")
    public CommonResult<MemberInfo> getById(@PathVariable Long id) {
        return CommonResult.success(memberInfoService.getById(id));
    }

    @GetMapping("/no/{memberNo}")
    @Operation(summary = "根据会员号查询")
    public CommonResult<MemberInfo> getByMemberNo(@PathVariable String memberNo) {
        return CommonResult.success(memberInfoService.getByMemberNo(memberNo));
    }

    @GetMapping("/phone/{phone}")
    @Operation(summary = "根据手机号查询")
    public CommonResult<MemberInfo> getByPhone(@PathVariable String phone) {
        return CommonResult.success(memberInfoService.getByPhone(phone));
    }

    @PostMapping
    @Operation(summary = "新增会员")
    public CommonResult<Void> add(@RequestBody MemberInfoDTO dto) {
        return memberInfoService.add(dto);
    }

    @PutMapping
    @Operation(summary = "修改会员")
    public CommonResult<Void> update(@RequestBody MemberInfoDTO dto) {
        return memberInfoService.update(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除会员")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return memberInfoService.delete(id);
    }

    @PostMapping("/recharge")
    @Operation(summary = "会员储值充值")
    public CommonResult<Void> recharge(@RequestParam Long memberId,
                                       @RequestParam BigDecimal amount,
                                       @RequestParam(required = false) String sourceNo) {
        return memberInfoService.recharge(memberId, amount, sourceNo);
    }

    @PostMapping("/consume")
    @Operation(summary = "会员消费扣款")
    public CommonResult<Void> consume(@RequestParam Long memberId,
                                      @RequestParam BigDecimal amount,
                                      @RequestParam(required = false) Integer points,
                                      @RequestParam(required = false) String sourceNo) {
        return memberInfoService.consume(memberId, amount, points, sourceNo);
    }

    @PostMapping("/points/add")
    @Operation(summary = "增加积分")
    public CommonResult<Void> addPoints(@RequestParam Long memberId,
                                        @RequestParam Integer points,
                                        @RequestParam(required = false) String sourceNo,
                                        @RequestParam(required = false) String remark) {
        return memberInfoService.addPoints(memberId, points, sourceNo, remark);
    }

    @PostMapping("/points/deduct")
    @Operation(summary = "扣减积分")
    public CommonResult<Void> deductPoints(@RequestParam Long memberId,
                                           @RequestParam Integer points,
                                           @RequestParam(required = false) String sourceNo,
                                           @RequestParam(required = false) String remark) {
        return memberInfoService.deductPoints(memberId, points, sourceNo, remark);
    }

    @PostMapping("/upgrade")
    @Operation(summary = "升级会员等级")
    public CommonResult<Void> upgradeLevel(@RequestParam Long memberId,
                                           @RequestParam Long levelId) {
        return memberInfoService.upgradeLevel(memberId, levelId);
    }
}
