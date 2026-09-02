package com.psi.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 客户旅程状态实体 —— 客户在某一套旅程中的当前位置。
 * <p>
 * <b>同一客户 + 同一旅程 只有一条数据</b>，由唯一索引
 * uk_subject_journey(tenant_id, journey_code, subject_type, subject_id) 保证。
 * <p>
 * 状态推进遵循「只进不退」：只有当新阶段的 sortOrder 大于当前值，
 * 或目标阶段显式声明 allowRollback=1 时才会更新。
 *
 * @author PSI
 */
@Data
@TableName("customer_journey_state")
public class CustomerJourneyStateEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    /** 所属旅程编码 */
    @TableField("journey_code")
    private String journeyCode;

    /** 主体类型：CUSTOMER / MEMBER */
    @TableField("subject_type")
    private String subjectType;

    /** 主体ID：客户ID 或 会员ID */
    @TableField("subject_id")
    private Long subjectId;

    /** 当前阶段编码 */
    @TableField("current_stage_code")
    private String currentStageCode;

    /** 当前阶段顺序（冗余存储，比较防倒退用） */
    @TableField("current_stage_order")
    private Integer currentStageOrder;

    /** 进入当前阶段的时间 */
    @TableField("enter_stage_time")
    private String enterStageTime;

    /** 把客户推到当前阶段的那条触点ID */
    @TableField("last_touchpoint_id")
    private Long lastTouchpointId;

    /** 触发本次推进的业务事件编码 */
    @TableField("last_event_code")
    private String lastEventCode;

    /** 累计推进次数 */
    @TableField("advance_count")
    private Integer advanceCount;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;
}
