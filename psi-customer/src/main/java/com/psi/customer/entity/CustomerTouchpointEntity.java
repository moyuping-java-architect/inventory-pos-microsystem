package com.psi.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 客户旅程触点实体
 * 记录非购买类触点（WhatsApp问价/到店/电话等），购买类触点自动从订单表聚合
 */
@Data
@TableName("customer_touchpoint")
public class CustomerTouchpointEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("tenant_id")
    private String tenantId;

    /** 会员ID（关联 member_info.id，B2C用） */
    @TableField("member_id")
    private Long memberId;

    /** 客户ID（关联 customer.id，B2B大客户用） */
    @TableField("customer_id")
    private Long customerId;

    /** 触点类型：INQUIRY / NEGOTIATION / WHATSAPP_MSG / STORE_VISIT / PHONE_CALL / SOCIAL / OTHER */
    @TableField("touchpoint_type")
    private String touchpointType;

    /** 渠道：WHATSAPP / PHONE / IN_STORE / FACEBOOK / OTHER */
    @TableField("channel")
    private String channel;

    /** 接触时间 */
    @TableField("contact_time")
    private String contactTime;

    /** 触点摘要（老板/店员手写一句话） */
    @TableField("summary")
    private String summary;

    /** 客户意图：BUY / COMPARE / COMPLAINT / INFO / CHITCHAT */
    @TableField("intent")
    private String intent;

    /**
     * 生成该触点的规则 ID（人工录入的触点为空）。
     * <p>
     * 有了它，引擎推进阶段时可以直接读规则上老板配好的 stage_code，
     * 不必再拿 touchpoint_type 去和阶段的 match_touchpoint 做字符串比对——
     * 那种比对只要两边编码约定不一致，整套旅程就悄无声息地不动了。
     */
    @TableField("source_rule_id")
    private Long sourceRuleId;

    /** 跟进动作（给老板的提醒） */
    @TableField("follow_up")
    private String followUp;

    /** 跟进是否完成 0-未完成 1-已完成 */
    @TableField("follow_up_done")
    private Integer followUpDone;

    /** 记录人 */
    @TableField("operator")
    private String operator;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;

    @TableField("del_flag")
    private Integer delFlag;
}
