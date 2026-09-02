package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_seq")
public class SysSeqEntity {

    private String posId;

    private String seqType;

    private String day;

    private Long currNo;
}