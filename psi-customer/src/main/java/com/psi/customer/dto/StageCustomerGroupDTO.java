package com.psi.customer.dto;

import lombok.Data;

import java.util.List;

/**
 * 客户旅程阶段分组 - 看板"客户旅程"面板用
 * 每个阶段 = 1个节点，列出当前在该阶段的所有客户
 *
 * 设计理念：让老板一眼看到"客户在哪个阶段卡住了"
 */
@Data
public class StageCustomerGroupDTO {

    /**
     * 阶段代码
     * NEW / ACTIVE / AT_RISK / HIGH_RISK / NEW_CHURNED / CHURNED
     */
    private String stageCode;

    /**
     * 阶段英文标签（前端 i18n 友好）
     */
    private String stageLabel;

    /**
     * 阶段中文标签（前端 fallback 用）
     */
    private String stageDisplayName;

    /**
     * 阶段图标（前端 el-tag type 或 emoji）
     * success/warning/danger/info/primary
     */
    private String tagType;

    /**
     * 阶段颜色 hex（来自配置 color 字段，前端用于节点顶部条 + 标签背景）
     */
    private String color;

    /**
     * 阶段图标名（Element Plus 图标，如 User/VideoCamera/Document/Money/EditPen/Trophy）
     */
    private String icon;

    /**
     * 阶段提示文案（来自配置 tip 字段）
     */
    private String tip;

    /**
     * 该阶段客户数
     */
    private Integer customerCount;

    /**
     * 该阶段客户累计消费合计（财影响）
     */
    private java.math.BigDecimal totalRevenue;

    /**
     * 该阶段客户列表（按累计消费降序，最多 perStageLimit 条）
     */
    private List<MemberSummaryDTO> customers;
}
