package com.psi.customer.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 单个会员的完整客户旅程时间线
 * 把购买事件（订单）和非购买事件（触点）混排成一个时间轴
 */
@Data
public class MemberJourneyDTO {

    /** 会员ID */
    private Integer memberId;

    /** 会员名 */
    private String memberName;

    /** 手机号 */
    private String phone;

    /** 累计消费 */
    private BigDecimal totalSpent;

    /** 总购买次数 */
    private Integer totalOrders;

    /** 客单价 */
    private BigDecimal avgOrderValue;

    /** 最近购买时间 */
    private String lastOrderTime;

    /** 平均购买间隔（天） */
    private Double avgIntervalDays;

    /** 流失等级 */
    private String churnLevel;

    /** 风险说明 */
    private String riskDescription;

    /** 当前旅程阶段编码（来自旅程引擎状态表） */
    private String currentStageCode;

    /** 当前旅程阶段名称 */
    private String currentStageName;

    /** 在当前阶段已停留天数 */
    private Integer daysInStage;

    /** 客户标签名称列表 */
    private List<String> customerTags;

    /** 当前应发送（可触达）话术 */
    private List<ScriptMatchResultDTO> matchedScripts;

    /** 已发送话术历史 */
    private List<CustomerScriptSendLogDTO> sentScripts;

    /** 时间线条目（按时间倒序，购买+触点混排） */
    private List<TimelineItem> timeline;

    /**
     * 时间线单条
     */
    @Data
    public static class TimelineItem {
        /** 时间 */
        private String time;
        /** 事件类型：ORDER / PAYMENT / REFUND / INQUIRY / WHATSAPP_MSG / STORE_VISIT ... */
        private String eventType;
        /** 摘要（一句话描述） */
        private String summary;
        /** 金额（购买/付款类有值） */
        private BigDecimal amount;
        /** 商品名（购买类有值） */
        private String productName;
        /** 渠道 */
        private String channel;
    }
}
