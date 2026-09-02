package com.psi.member.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psi.member.dto.MemberLevelDTO;
import com.psi.member.entity.MemberLevel;
import com.psi.common.result.CommonResult;

import java.math.BigDecimal;
import java.util.List;

public interface MemberLevelService {

    List<MemberLevel> list();

    Page<MemberLevel> page(int pageNum, int pageSize);

    MemberLevel getById(Long id);

    CommonResult<Void> add(MemberLevelDTO dto);

    CommonResult<Void> update(MemberLevelDTO dto);

    CommonResult<Void> delete(Long id);

    BigDecimal calculateDiscount(Long memberId, BigDecimal originalAmount);

    int calculateEarnPoints(Long memberId, BigDecimal amount);
}
