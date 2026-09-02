package com.psi.customer.service;

import com.psi.customer.dto.*;
import com.psi.common.result.CommonResult;

import java.math.BigDecimal;
import java.util.List;

/**
 * 客户增长服务接口
 * PSI 主卖点模块：流失预警 / 复购分析 / 召回建议 / 触点管理
 */
public interface CustomerJourneyService {

    /**
     * 看板概览（老板打开第一屏）
     * "你知道上个月来过的客户有几个这个月没回来吗？"
     */
    CustomerJourneyDashboardDTO getDashboard();

    /**
     * 流失预警列表（所有 AT_RISK / HIGH_RISK / CHURNED 客户）
     */
    List<ChurnAlertDTO> getChurnAlerts();

    /**
     * 单个客户完整旅程时间线（购买 + 触点混排）
     */
    MemberJourneyDTO getCustomerJourney(Long customerId);

    /**
     * Top N 客户（按累计消费）
     */
    List<MemberSummaryDTO> getTopCustomers(int limit);

    /**
     * 召回建议（给老板的 WhatsApp 消息模板 + 预估回款）
     */
    List<WinBackSuggestionDTO> getWinBackSuggestions();

    /**
     * 录入触点（手动 or WhatsApp Bot 自动）
     */
    CommonResult<Integer> recordTouchpoint(CustomerTouchpointDTO dto);

    /**
     * 风险收入（不召回就丢掉的钱）
     */
    BigDecimal getRevenueAtRisk();

    /**
     * 客户旅程分组 - 按旅程阶段列出每个阶段的客户
     * <p>
     * 阶段元数据来自 {@code customer_journey_stage}（老板在配置页维护），
     * 客户归属来自 {@code customer_journey_state}（旅程引擎按规则算出来的结果）。
     * 也就是说：老板在配置页改了「业务事件 → 旅程阶段」，看板跟着变，不用改代码。
     *
     * @param journeyCode   旅程编码，为空时取默认旅程（is_default=1）
     * @param perStageLimit 每个阶段最多返回多少个客户
     */
    List<StageCustomerGroupDTO> getCustomersByStage(String journeyCode, int perStageLimit);
}
