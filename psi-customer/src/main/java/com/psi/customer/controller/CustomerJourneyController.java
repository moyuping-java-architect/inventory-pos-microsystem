package com.psi.customer.controller;

import com.psi.common.result.CommonResult;
import com.psi.customer.dto.*;
import com.psi.customer.service.CustomerJourneyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 客户增长 Controller
 *
 * PSI 核心卖点模块：流失预警 / 复购分析 / 召回建议 / 触点管理
 * 防损（进销存）= 保险逻辑 = 砍价
 * 增收（客户旅程）= 投资逻辑 = 愿付
 */
@RestController
@RequestMapping("/psi/customer/journey")
@Tag(name = "客户增长", description = "客户流失预警 / 复购分析 / 召回建议 / 触点管理")
public class CustomerJourneyController {

    private final CustomerJourneyService journeyService;

    public CustomerJourneyController(CustomerJourneyService journeyService) {
        this.journeyService = journeyService;
    }

    // ========== 看板 ==========

    @GetMapping("/dashboard")
    @Operation(summary = "客户增长看板", description = "老板打开第一屏：总客户/活跃/风险/流失/风险收入/月度对比/复购率/Top5/Top10预警")
    public CommonResult<CustomerJourneyDashboardDTO> getDashboard() {
        return CommonResult.success(journeyService.getDashboard());
    }

    // ========== 流失预警 ==========

    @GetMapping("/churn-alerts")
    @Operation(summary = "流失预警列表", description = "所有 AT_RISK / HIGH_RISK / CHURNED / NEW_CHURNED 客户，按累计消费降序")
    public CommonResult<List<ChurnAlertDTO>> getChurnAlerts() {
        return CommonResult.success(journeyService.getChurnAlerts());
    }

    // ========== 客户旅程时间线 ==========

    @GetMapping("/{customerId}/timeline")
    @Operation(summary = "客户旅程时间线", description = "单个客户完整旅程：购买事件 + 触点事件混排，按时间倒序，限20条")
    public CommonResult<MemberJourneyDTO> getCustomerJourney(@PathVariable Long customerId) {
        return CommonResult.success(journeyService.getCustomerJourney(customerId));
    }

    // ========== Top 客户 ==========

    @GetMapping("/top-customers")
    @Operation(summary = "Top N 客户", description = "按累计消费排名的 Top 客户，默认10名")
    public CommonResult<List<MemberSummaryDTO>> getTopCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        return CommonResult.success(journeyService.getTopCustomers(limit));
    }

    // ========== 召回建议 ==========

    @GetMapping("/winback-suggestions")
    @Operation(summary = "召回建议", description = "为风险客户生成 WhatsApp 消息模板 + 预估回客单价 + 渠道建议")
    public CommonResult<List<WinBackSuggestionDTO>> getWinBackSuggestions() {
        return CommonResult.success(journeyService.getWinBackSuggestions());
    }

    // ========== 触点录入 ==========

    @PostMapping("/touchpoint")
    @Operation(summary = "录入客户触点", description = "手动 or WhatsApp Bot 自动：记录非购买类触点（问价/到店/电话等）")
    public CommonResult<Integer> recordTouchpoint(@RequestBody CustomerTouchpointDTO dto) {
        return journeyService.recordTouchpoint(dto);
    }

    // ========== 风险收入 ==========

    @GetMapping("/revenue-at-risk")
    @Operation(summary = "风险收入", description = "不召回就丢掉的钱：所有风险客户的月均消费之和")
    public CommonResult<BigDecimal> getRevenueAtRisk() {
        return CommonResult.success(journeyService.getRevenueAtRisk());
    }

    // ========== 客户旅程分组 ==========

    @GetMapping("/stages")
    @Operation(summary = "客户旅程分组",
            description = "按旅程阶段列出每个阶段的客户。阶段节点来自配置页维护的阶段表，客户归属来自旅程引擎算出的状态表——"
                    + "老板改了「业务事件→旅程阶段」映射，看板立刻跟着变。journeyCode 不传时取默认旅程。")
    public CommonResult<List<StageCustomerGroupDTO>> getCustomersByStage(
            @RequestParam(required = false) String journeyCode,
            @RequestParam(defaultValue = "10") int perStageLimit) {
        return CommonResult.success(journeyService.getCustomersByStage(journeyCode, perStageLimit));
    }
}
