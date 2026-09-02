package com.psi.common.mybatis.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 带乐观锁的实体基类
 * 继承自 BaseEntity，增加乐观锁版本号字段
 * 适用于需要乐观锁控制的表，如库存表、订单表、商品表等
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseVersionEntity extends BaseEntity {

    /**
     * 乐观锁版本号，用于乐观锁控制
     * 插入时自动填充，更新时 MyBatis-Plus 自动递增
     */
    @Version
    @TableField(value = "version", fill = FieldFill.INSERT)
    private Integer version;
}