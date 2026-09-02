package com.psi.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 触点生成规则实体 —— 零代码配置的核心。
 * <p>
 * 老板在页面上配「什么业务事件 + 满足什么条件 → 生成什么客户触点」，
 * 通用引擎按这张表把业务事件翻译成客户触点，不需要为每种玩法写监听器。
 * <p>
 * conditionJson 格式：{@code [{"field":"totalAmount","op":"GE","value":"5000"}]}，
 * 多个条件之间为 AND。字段取自 customer_metrics 的列名（驼峰）或事件 payload。
 *
 * @author PSI
 */
@Data
@TableName("touchpoint_generate_rule")
public class TouchpointGenerateRuleEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    /** 规则名称，如「累计满5000升银卡」 */
    @TableField("rule_name")
    private String ruleName;

    /** 监听的业务事件编码，取自 business_event_dict */
    @TableField("event_code")
    private String eventCode;

    /** 条件表达式 JSON 数组，多条件 AND；为空表示无条件命中 */
    @TableField("condition_json")
    private String conditionJson;

    /** 生成的触点类型 */
    @TableField("touchpoint_type")
    private String touchpointType;

    /** 关联的客户旅程阶段编码（零代码事件→阶段映射） */
    @TableField("stage_code")
    private String stageCode;

    /** 生成的触点意图，支持 {fieldName} 占位符从事件载荷取值 */
    @TableField("intent")
    private String intent;

    /** 生成的触点渠道 */
    @TableField("channel")
    private String channel;

    /** 摘要模板，支持 {docNo} {amount} 等占位符 */
    @TableField("summary_template")
    private String summaryTemplate;

    /** 1-每个客户只触发一次（如会员升级）0-每次满足都触发 */
    @TableField("once_only")
    private Integer onceOnly;

    @TableField("enabled")
    private Integer enabled;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("del_flag")
    private Integer delFlag;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;
}
