package com.psi.member.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_level")
public class MemberLevel extends BaseEntity {

    private String levelName;

    private Integer level;

    private BigDecimal discount;

    private BigDecimal minConsume;

    private Integer minPoints;

    private BigDecimal pointRate;

    private String levelIcon;

    private String description;

    private Integer sortOrder;
}
