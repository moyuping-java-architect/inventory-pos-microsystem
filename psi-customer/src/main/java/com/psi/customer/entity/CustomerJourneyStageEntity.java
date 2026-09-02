package com.psi.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

/**
 * 客户旅程阶段实体。
 * <p>
 * 阶段归属于某一套旅程（journeyCode），不再是全局配置。
 * 客户落在哪个阶段由 matchTouchpoint + matchIntent 与触点记录匹配决定，
 * sortOrder 越大代表越靠后，是防止阶段倒退的判断依据。
 *
 * @author PSI
 */
@Data
@TableName("customer_journey_stage")
public class CustomerJourneyStageEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    /** 归属旅程编码 */
    @TableField("journey_code")
    private String journeyCode;

    /** 阶段编码 */
    @TableField("stage_code")
    private String stageCode;

    /** 阶段显示名 */
    @TableField("stage_name")
    private String stageName;

    /** 看板节点颜色 */
    @TableField("color")
    private String color;

    /** 看板节点图标 */
    @TableField("icon")
    private String icon;

    /** 悬浮提示，给老板看的行动建议 */
    @TableField("tip")
    private String tip;

    /** 匹配的触点类型 */
    @TableField("match_touchpoint")
    private String matchTouchpoint;

    /** 匹配的触点意图，NULL 表示不限 */
    @TableField("match_intent")
    private String matchIntent;

    /** 阶段顺序，越大越靠后 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 是否允许从更高阶段退回：1-允许（如流失阶段）0-只进不退 */
    @TableField("allow_rollback")
    private Integer allowRollback;

    /** 透传字段：该阶段由哪些业务事件触发（不直接落库，驱动 touchpoint_generate_rule 同步） */
    @TableField(exist = false)
    private List<String> eventCodes;

    @TableField("del_flag")
    private Integer delFlag;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;
}
