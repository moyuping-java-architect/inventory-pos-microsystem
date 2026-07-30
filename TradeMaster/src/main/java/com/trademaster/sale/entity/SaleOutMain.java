package com.trademaster.sale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sale_out_main")
public class SaleOutMain {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String outNo;

    private String docName;

    private String customerCode;

    private String customerName;

    private String warehouseCode;

    private String warehouseName;

    private String orderNo;

    private LocalDateTime outDate;

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
