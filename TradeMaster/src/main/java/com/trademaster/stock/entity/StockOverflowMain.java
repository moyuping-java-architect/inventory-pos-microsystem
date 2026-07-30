package com.trademaster.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("stock_overflow_main")
public class StockOverflowMain {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String overflowNo;

    private String docName;

    private String warehouseCode;

    private String warehouseName;

    private LocalDateTime overflowDate;

    private BigDecimal totalAmount;

    private String overflowReason;

    private Integer status;

    private Long auditBy;

    private LocalDateTime auditTime;

    private String remark;

    @TableLogic
    private Integer delFlag;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
