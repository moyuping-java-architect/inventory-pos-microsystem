package com.psi.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 规则触发幂等记录。
 * <p>
 * 唯一索引 uk_fire(tenant_id, rule_id, subject_type, subject_id, biz_key) 承担两种语义：
 * <ul>
 *   <li>onceOnly=1 的规则：bizKey 固定写 {@code ONCE}，同一客户同一规则终身只能命中一次</li>
 *   <li>onceOnly=0 的规则：bizKey 写业务单号，防止同一单据被重复消费</li>
 * </ul>
 *
 * @author PSI
 */
@Data
@TableName("journey_rule_fire_log")
public class JourneyRuleFireLogEntity {

    /** onceOnly 规则的固定业务键 */
    public static final String ONCE_KEY = "ONCE";

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("rule_id")
    private Long ruleId;

    @TableField("subject_type")
    private String subjectType;

    @TableField("subject_id")
    private Long subjectId;

    /** onceOnly=1 时固定 ONCE；否则写业务单号 */
    @TableField("biz_key")
    private String bizKey;

    /** 生成的触点ID */
    @TableField("touchpoint_id")
    private Long touchpointId;

    @TableField("fire_time")
    private String fireTime;
}
