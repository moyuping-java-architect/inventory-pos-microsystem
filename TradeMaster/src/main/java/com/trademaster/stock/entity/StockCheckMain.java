package com.trademaster.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("stock_check_main")
public class StockCheckMain {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String checkNo;

    private String docName;

    private String warehouseCode;

    private String warehouseName;

    private LocalDateTime checkDate;

    private BigDecimal varianceAmount;

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
