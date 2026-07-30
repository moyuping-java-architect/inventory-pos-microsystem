package com.psi.member.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_balance_log")
public class MemberBalanceLog extends BaseEntity {

    private Long memberId;

    private String memberNo;

    private Integer type;

    private BigDecimal amount;

    private BigDecimal beforeBalance;

    private BigDecimal afterBalance;

    private String sourceNo;

    private String sourceType;

    private String remark;
}
