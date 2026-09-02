package com.psi.member.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MemberInfoDTO {

    private Long id;

    private String memberNo;

    private String memberName;

    private String phone;

    private String email;

    private Integer gender;

    private LocalDate birthday;

    private Long levelId;

    private BigDecimal balance;

    private Integer points;

    private String address;

    private String remark;

    private Integer status;
}
