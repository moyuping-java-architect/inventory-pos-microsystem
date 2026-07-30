package com.trademaster.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("warehouse")
public class Warehouse {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String warehouseCode;

    private String warehouseName;

    private String address;

    private Integer status;

    @TableLogic
    private Integer delFlag;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
