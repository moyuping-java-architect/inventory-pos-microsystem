package com.psi.cashier.controller;

import com.psi.cashier.entity.MemberEntity;
import com.psi.cashier.entity.MemberLevelEntity;
import com.psi.cashier.service.MemberService;
import com.psi.common.result.CommonResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 会员控制器
 * 提供会员查询和注册的REST API接口
 */
@RestController
@RequestMapping("/psi/cashier/member")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 根据手机号查询会员
     */
    @GetMapping("/phone/{phone}")
    public CommonResult<MemberEntity> getByPhone(@PathVariable String phone) {
        MemberEntity member = memberService.getByPhone(phone);
        if (member == null) {
            return CommonResult.fail("会员不存在");
        }
        return CommonResult.success(member);
    }

    /**
     * 搜索会员（支持手机号或会员号）
     */
    @GetMapping("/search")
    public CommonResult<Map<String, Object>> searchMember(@RequestParam String keyword) {
        // 先尝试按手机号查询
        MemberEntity member = memberService.getByPhone(keyword);
        
        // 如果没找到，尝试按数据UUID查询
        if (member == null) {
            member = memberService.getByDataUuid(keyword);
        }
        
        if (member == null) {
            return CommonResult.fail("会员不存在");
        }

        MemberLevelEntity level = memberService.getMemberLevel(member.getLevel());
        Double discount = level != null ? level.getDiscount() : 1.0;

        Map<String, Object> result = new HashMap<>();
        result.put("memberId", member.getMemberId());
        result.put("name", member.getName());
        result.put("phone", member.getPhone());
        result.put("level", level != null ? level.getLevelName() : "普通会员");
        result.put("levelId", member.getLevel());
        result.put("discount", discount);
        result.put("points", member.getPoint());
        result.put("balance", member.getBalance());

        return CommonResult.success(result);
    }

    /**
     * 根据数据UUID查询会员
     */
    @GetMapping("/uuid/{dataUuid}")
    public CommonResult<MemberEntity> getByDataUuid(@PathVariable String dataUuid) {
        MemberEntity member = memberService.getByDataUuid(dataUuid);
        if (member == null) {
            return CommonResult.fail("会员不存在");
        }
        return CommonResult.success(member);
    }

    /**
     * 注册新会员
     */
    @PostMapping("/register")
    public CommonResult<MemberEntity> register(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String name = request.get("name");

        if (phone == null || phone.isEmpty()) {
            return CommonResult.fail("手机号不能为空");
        }

        MemberEntity member = memberService.register(phone, name);
        return CommonResult.success(member);
    }

    /**
     * 会员储值充值
     * 收银员为会员卡充值，充值后金额立即可用于消费抵扣
     */
    @PostMapping("/recharge")
    public CommonResult<Map<String, Object>> recharge(@RequestBody Map<String, Object> request) {
        Object memberIdObj = request.get("memberId");
        Object amountObj = request.get("amount");

        if (memberIdObj == null || amountObj == null) {
            return CommonResult.fail("会员ID和充值金额不能为空");
        }

        Integer memberId = Integer.valueOf(memberIdObj.toString());
        Double amount = Double.valueOf(amountObj.toString());

        if (amount <= 0) {
            return CommonResult.fail("充值金额必须大于0");
        }

        try {
            Double afterBalance = memberService.recharge(memberId, amount);

            Map<String, Object> result = new HashMap<>();
            result.put("memberId", memberId);
            result.put("rechargeAmount", amount);
            result.put("afterBalance", afterBalance);

            return CommonResult.success(result);
        } catch (IllegalArgumentException e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    /**
     * 获取会员等级信息
     */
    @GetMapping("/level/{levelId}")
    public CommonResult<MemberLevelEntity> getMemberLevel(@PathVariable Integer levelId) {
        MemberLevelEntity level = memberService.getMemberLevel(levelId);
        if (level == null) {
            return CommonResult.fail("会员等级不存在");
        }
        return CommonResult.success(level);
    }

    /**
     * 获取会员信息（包含等级折扣）
     */
    @GetMapping("/info/{phone}")
    public CommonResult<Map<String, Object>> getMemberInfo(@PathVariable String phone) {
        MemberEntity member = memberService.getByPhone(phone);
        if (member == null) {
            return CommonResult.fail("会员不存在");
        }

        MemberLevelEntity level = memberService.getMemberLevel(member.getLevel());
        Double discount = level != null ? level.getDiscount() : 1.0;

        Map<String, Object> result = new HashMap<>();
        result.put("member", member);
        result.put("discount", discount);
        result.put("levelName", level != null ? level.getLevelName() : "普通会员");

        return CommonResult.success(result);
    }
}