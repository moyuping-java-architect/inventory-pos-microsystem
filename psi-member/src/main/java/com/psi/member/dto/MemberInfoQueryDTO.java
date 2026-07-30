package com.psi.member.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MemberInfoQueryDTO implements Serializable {

    private String memberNo;

    private String memberName;

    private String phone;

    private Long levelId;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
