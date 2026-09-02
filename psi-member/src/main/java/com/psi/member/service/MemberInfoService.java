package com.psi.member.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psi.member.dto.MemberInfoDTO;
import com.psi.member.dto.MemberInfoQueryDTO;
import com.psi.member.entity.MemberInfo;
import com.psi.common.result.CommonResult;

import java.math.BigDecimal;

public interface MemberInfoService {

    Page<MemberInfo> page(MemberInfoQueryDTO queryDTO);

    MemberInfo getById(Long id);

    MemberInfo getByMemberNo(String memberNo);

    MemberInfo getByPhone(String phone);

    CommonResult<Void> add(MemberInfoDTO dto);

    CommonResult<Void> update(MemberInfoDTO dto);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> recharge(Long memberId, BigDecimal amount, String sourceNo);

    CommonResult<Void> consume(Long memberId, BigDecimal amount, Integer points, String sourceNo);

    CommonResult<Void> addPoints(Long memberId, Integer points, String sourceNo, String remark);

    CommonResult<Void> deductPoints(Long memberId, Integer points, String sourceNo, String remark);

    CommonResult<Void> upgradeLevel(Long memberId, Long levelId);
}
