package com.psi.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psi.common.result.CommonResult;
import com.psi.common.util.BeanUtils;
import com.psi.member.dto.MemberLevelDTO;
import com.psi.member.entity.MemberInfo;
import com.psi.member.entity.MemberLevel;
import com.psi.member.mapper.MemberInfoMapper;
import com.psi.member.mapper.MemberLevelMapper;
import com.psi.member.service.MemberLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class MemberLevelServiceImpl implements MemberLevelService {

    private final MemberLevelMapper memberLevelMapper;
    private final MemberInfoMapper memberInfoMapper;

    public MemberLevelServiceImpl(MemberLevelMapper memberLevelMapper, MemberInfoMapper memberInfoMapper) {
        this.memberLevelMapper = memberLevelMapper;
        this.memberInfoMapper = memberInfoMapper;
    }

    @Override
    public List<MemberLevel> list() {
        return memberLevelMapper.selectList(
                new LambdaQueryWrapper<MemberLevel>()
                        .eq(MemberLevel::getStatus, 1)
                        .orderByAsc(MemberLevel::getLevel)
        );
    }

    @Override
    public Page<MemberLevel> page(int pageNum, int pageSize) {
        return memberLevelMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<MemberLevel>()
                        .orderByAsc(MemberLevel::getLevel)
        );
    }

    @Override
    public MemberLevel getById(Long id) {
        return memberLevelMapper.selectById(id);
    }

    @Override
    public CommonResult<Void> add(MemberLevelDTO dto) {
        MemberLevel level = BeanUtils.convert(dto, MemberLevel.class);
        memberLevelMapper.insert(level);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> update(MemberLevelDTO dto) {
        MemberLevel level = BeanUtils.convert(dto, MemberLevel.class);
        memberLevelMapper.updateById(level);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> delete(Long id) {
        memberLevelMapper.deleteById(id);
        return CommonResult.success();
    }

    @Override
    public BigDecimal calculateDiscount(Long memberId, BigDecimal originalAmount) {
        if (memberId == null || originalAmount == null) {
            return originalAmount;
        }

        MemberInfo member = memberInfoMapper.selectById(memberId);
        if (member == null || member.getLevelId() == null) {
            return originalAmount;
        }

        MemberLevel level = memberLevelMapper.selectById(member.getLevelId());
        if (level == null || level.getDiscount() == null) {
            return originalAmount;
        }

        return originalAmount.multiply(level.getDiscount()).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public int calculateEarnPoints(Long memberId, BigDecimal amount) {
        if (memberId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        MemberInfo member = memberInfoMapper.selectById(memberId);
        if (member == null || member.getLevelId() == null) {
            return amount.intValue();
        }

        MemberLevel level = memberLevelMapper.selectById(member.getLevelId());
        if (level == null || level.getPointRate() == null) {
            return amount.intValue();
        }

        return amount.multiply(level.getPointRate()).intValue();
    }
}
