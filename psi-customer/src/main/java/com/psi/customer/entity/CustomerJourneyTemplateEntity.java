package com.psi.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 客户旅程模板实体。
 * <p>
 * 一套旅程 = 一条完整的客户生命周期链路（如销售漏斗、会员体系）。
 * 系统支持多套并行，同一个客户可以同时存在于多套旅程的不同阶段。
 *
 * @author PSI
 */
@Data
@TableName("customer_journey_template")
public class CustomerJourneyTemplateEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    /** 旅程编码：SALES_FUNNEL / MEMBERSHIP / 用户自建 */
    @TableField("journey_code")
    private String journeyCode;

    /** 旅程名称 */
    @TableField("journey_name")
    private String journeyName;

    /** 主体类型：CUSTOMER-按B2B客户聚合 / MEMBER-按B2C会员聚合 */
    @TableField("subject_type")
    private String subjectType;

    /** 旅程图标 */
    @TableField("icon")
    private String icon;

    /** 旅程说明 */
    @TableField("description")
    private String description;

    /** 是否默认旅程：1-看板打开时默认选中 */
    @TableField("is_default")
    private Integer isDefault;

    /** 是否启用：1-启用 0-停用（停用不在下拉出现但数据保留） */
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
