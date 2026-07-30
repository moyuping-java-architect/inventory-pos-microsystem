package com.trademaster.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("purchase_in_main")
public class PurchaseInMain {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String inNo;

    private String docName;

    private String supplierCode;

    private String supplierName;

    private String warehouseCode;

    private String warehouseName;

    private String orderNo;

    private LocalDateTime inDate;

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
