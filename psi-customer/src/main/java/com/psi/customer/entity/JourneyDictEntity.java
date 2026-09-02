package com.psi.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 客户旅程通用字典 —— 配置页所有下拉框的唯一数据源。
 * <p>
 * dictType 取值：TOUCHPOINT_TYPE-触点类型 / INTENT-客户意图 / CHANNEL-接触渠道。
 * 建立这张表是为了终结此前建表注释、配置种子、代码写入三套取值互不相同的问题。
 *
 * @author PSI
 */
@Data
@TableName("journey_dict")
public class JourneyDictEntity {

    public static final String TYPE_TOUCHPOINT = "TOUCHPOINT_TYPE";
    public static final String TYPE_INTENT = "INTENT";
    public static final String TYPE_CHANNEL = "CHANNEL";

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    /** 字典分类 */
    @TableField("dict_type")
    private String dictType;

    /** 编码，写入触点表的实际值 */
    @TableField("dict_code")
    private String dictCode;

    /** 显示名称 */
    @TableField("dict_name")
    private String dictName;

    /** 英文显示名称，配置页切到英文环境时用 */
    @TableField("dict_name_en")
    private String dictNameEn;

    /** 适用主体：CUSTOMER / MEMBER / BOTH */
    @TableField("subject_type")
    private String subjectType;

    /** 附加属性 JSON */
    @TableField("extra_json")
    private String extraJson;

    /** 1-出厂内置不可删 0-用户自建 */
    @TableField("built_in")
    private Integer builtIn;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("enabled")
    private Integer enabled;

    @TableField("del_flag")
    private Integer delFlag;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;
}
