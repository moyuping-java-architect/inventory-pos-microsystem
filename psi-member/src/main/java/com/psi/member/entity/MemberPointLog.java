package com.psi.member.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_point_log")
public class MemberPointLog extends BaseEntity {

    private Long memberId;

    private String memberNo;

    private Integer type;

    private Integer points;

    private Integer beforePoints;

    private Integer afterPoints;

    private String sourceNo;

    private String sourceType;

    private String remark;
}
