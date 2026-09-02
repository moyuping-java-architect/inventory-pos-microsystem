package com.psi.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 话术发送记录（去重核心）
 * 保证同一客户在同一节点-天数区间不会重复收到同一条话术
 */
@Data
@TableName("customer_script_send_log")
public class CustomerScriptSendLogEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    /** 客户ID */
    @TableField("customer_id")
    private Long customerId;

    /** 话术ID */
    @TableField("script_id")
    private Long scriptId;

    /** 发送时客户所在节点 */
    @TableField("stage_code")
    private String stageCode;

    /** 发送时客户在节点第几天 */
    @TableField("day_in_stage")
    private Integer dayInStage;

    /** 发送渠道 */
    @TableField("channel")
    private String channel;

    /** 发送状态 0-失败 1-成功 2-待发送 */
    @TableField("send_status")
    private Integer sendStatus;

    /** 发送时间 */
    @TableField("send_time")
    private String sendTime;

    /** 操作人ID */
    @TableField("operator_id")
    private String operatorId;

    /** 操作人姓名 */
    @TableField("operator_name")
    private String operatorName;

    /** 备注 */
    @TableField("remark")
    private String remark;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;

    @TableField("del_flag")
    private Integer delFlag;
}
