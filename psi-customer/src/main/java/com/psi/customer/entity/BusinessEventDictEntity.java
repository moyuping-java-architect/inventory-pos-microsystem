package com.psi.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 业务事件字典 —— 系统能发生的业务动作全集。
 * <p>
 * 这是零代码方案成立的前提：一个进销存系统的业务动作是可以穷举完的，
 * 且不会随着老板想加什么新玩法而增长。老板要加会员体系，
 * 用的还是「订单完成」这件事，只是想让它触发不一样的后果。
 * <p>
 * 这张表由开发维护，老板只读不写。implemented=0 的事件在配置页灰显，
 * 表示已规划但尚未接入事件总线。
 *
 * @author PSI
 */
@Data
@TableName("business_event_dict")
public class BusinessEventDictEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 事件编码，如 SALE.ORDER_APPROVED */
    @TableField("event_code")
    private String eventCode;

    /** 事件中文名 */
    @TableField("event_name")
    private String eventName;

    /** 事件英文名（国际化，老板可读） */
    @TableField("event_name_en")
    private String eventNameEn;

    /** 所属模块：SALE / CASHIER / MEMBER / FINANCE / CUSTOMER */
    @TableField("module_name")
    private String moduleName;

    /** 主体类型：CUSTOMER-B2B客户 / MEMBER-B2C会员 */
    @TableField("subject_type")
    private String subjectType;

    /** JSON 数组，该事件能提供的字段，供条件构建器提示 */
    @TableField("payload_fields")
    private String payloadFields;

    /** 1-已接入总线 0-占位未接入 */
    @TableField("implemented")
    private Integer implemented;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("enabled")
    private Integer enabled;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;
}
