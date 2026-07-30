package com.trademaster.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("stock_transfer_main")
public class StockTransferMain {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String transferNo;

    private String docName;

    private String fromWarehouseCode;

    private String fromWarehouseName;

    private String toWarehouseCode;

    private String toWarehouseName;

    private LocalDateTime transferDate;

    private BigDecimal totalAmount;

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
