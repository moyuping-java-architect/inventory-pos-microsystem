package com.psi.common.mybatis.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * 包含所有表的公共字段（不含乐观锁），支持自动填充和逻辑删除
 * 适用于不需要乐观锁的表，如系统配置表、字典表等
 */
@Data
public class BaseEntity implements Serializable {

    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID，插入时自动填充
     */
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 创建人ID，插入时自动填充
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建时间，插入时自动填充
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人ID，插入和更新时自动填充
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新时间，插入和更新时自动填充
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记，0-未删除，1-已删除，插入时自动填充，逻辑删除字段
     */
    @TableField(value = "del_flag", fill = FieldFill.INSERT)
    @TableLogic
    private Integer delFlag;

    /**
     * 状态，1-启用，0-禁用，插入时自动填充
     */
    @TableField(value = "status", fill = FieldFill.INSERT)
    private Integer status;

    /**
     * 数据唯一标识（雪花算法生成），用于分布式数据同步，插入时自动填充
     */
    @TableField(value = "data_uuid", fill = FieldFill.INSERT)
    private String dataUuid;
}