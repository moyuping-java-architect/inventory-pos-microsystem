package com.psi.stock.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_mq_process")
public class StockMqProcessEntity extends BaseEntity {

    private String businessCode;
}