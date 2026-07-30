package com.psi.member.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_info")
public class MemberInfo extends BaseEntity {

    private String memberNo;

    private String memberName;

    private String phone;

    private String email;

    private Integer gender;

    private LocalDate birthday;

    private Long levelId;

    private String levelName;

    private BigDecimal balance;

    private Integer points;

    private BigDecimal totalConsume;

    private Integer totalOrders;

    private String address;

    private String remark;

    private Integer status;

    private LocalDateTime registerTime;

    private LocalDateTime lastConsumeTime;
}
