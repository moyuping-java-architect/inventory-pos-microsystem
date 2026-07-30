package com.psi.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psi.common.exception.BusinessException;
import com.psi.common.result.CommonResult;
import com.psi.common.result.ResultCode;
import com.psi.common.util.BeanUtils;
import com.psi.member.dto.MemberInfoDTO;
import com.psi.member.dto.MemberInfoQueryDTO;
import com.psi.member.entity.MemberBalanceLog;
import com.psi.member.entity.MemberInfo;
import com.psi.member.entity.MemberLevel;
import com.psi.member.entity.MemberPointLog;
import com.psi.member.mapper.MemberBalanceLogMapper;
import com.psi.member.mapper.MemberInfoMapper;
import com.psi.member.mapper.MemberLevelMapper;
import com.psi.member.mapper.MemberPointLogMapper;
import com.psi.member.service.MemberInfoService;
import com.psi.member.service.MemberLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class MemberInfoServiceImpl implements MemberInfoService {

    private final MemberInfoMapper memberInfoMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final MemberBalanceLogMapper balanceLogMapper;
    private final MemberPointLogMapper pointLogMapper;

    public MemberInfoServiceImpl(MemberInfoMapper memberInfoMapper,
                                  MemberLevelMapper memberLevelMapper,
                                  MemberBalanceLogMapper balanceLogMapper,
                                  MemberPointLogMapper pointLogMapper) {
        this.memberInfoMapper = memberInfoMapper;
        this.memberLevelMapper = memberLevelMapper;
        this.balanceLogMapper = balanceLogMapper;
        this.pointLogMapper = pointLogMapper;
    }

    @Override
    public Page<MemberInfo> page(MemberInfoQueryDTO queryDTO) {
        LambdaQueryWrapper<MemberInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getMemberNo())) {
            wrapper.like(MemberInfo::getMemberNo, queryDTO.getMemberNo());
        }
        if (StringUtils.hasText(queryDTO.getMemberName())) {
            wrapper.like(MemberInfo::getMemberName, queryDTO.getMemberName());
        }
        if (StringUtils.hasText(queryDTO.getPhone())) {
            wrapper.like(MemberInfo::getPhone, queryDTO.getPhone());
        }
        if (queryDTO.getLevelId() != null) {
            wrapper.eq(MemberInfo::getLevelId, queryDTO.getLevelId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(MemberInfo::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(MemberInfo::getCreateTime);
        return memberInfoMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
                wrapper
        );
    }

    @Override
    public MemberInfo getById(Long id) {
        return memberInfoMapper.selectById(id);
    }

    @Override
    public MemberInfo getByMemberNo(String memberNo) {
        return memberInfoMapper.selectOne(
                new LambdaQueryWrapper<MemberInfo>().eq(MemberInfo::getMemberNo, memberNo)
        );
    }

    @Override
    public MemberInfo getByPhone(String phone) {
        return memberInfoMapper.selectOne(
                new LambdaQueryWrapper<MemberInfo>().eq(MemberInfo::getPhone, phone)
        );
    }

    @Override
    @Transactional
    public CommonResult<Void> add(MemberInfoDTO dto) {
        MemberInfo exist = getByPhone(dto.getPhone());
        if (exist != null) {
            return CommonResult.fail("该手机号已注册会员");
        }

        MemberInfo member = BeanUtils.convert(dto, MemberInfo.class);
        member.setMemberNo(generateMemberNo());
        member.setRegisterTime(LocalDateTime.now());
        member.setStatus(1);
        if (member.getBalance() == null) {
            member.setBalance(BigDecimal.ZERO);
        }
        if (member.getPoints() == null) {
            member.setPoints(0);
        }
        if (member.getTotalConsume() == null) {
            member.setTotalConsume(BigDecimal.ZERO);
        }
        if (member.getTotalOrders() == null) {
            member.setTotalOrders(0);
        }

        if (dto.getLevelId() != null) {
            MemberLevel level = memberLevelMapper.selectById(dto.getLevelId());
            if (level != null) {
                member.setLevelName(level.getLevelName());
            }
        }

        memberInfoMapper.insert(member);
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> update(MemberInfoDTO dto) {
        MemberInfo member = memberInfoMapper.selectById(dto.getId());
        if (member == null) {
            return CommonResult.fail("会员不存在");
        }

        MemberInfo update = BeanUtils.convert(dto, MemberInfo.class);
        if (dto.getLevelId() != null && !dto.getLevelId().equals(member.getLevelId())) {
            MemberLevel level = memberLevelMapper.selectById(dto.getLevelId());
            if (level != null) {
                update.setLevelName(level.getLevelName());
            }
        }

        memberInfoMapper.updateById(update);
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> delete(Long id) {
        memberInfoMapper.deleteById(id);
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> recharge(Long memberId, BigDecimal amount, String sourceNo) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return CommonResult.fail("充值金额必须大于0");
        }

        MemberInfo member = memberInfoMapper.selectById(memberId);
        if (member == null) {
            return CommonResult.fail("会员不存在");
        }

        BigDecimal beforeBalance = member.getBalance();
        BigDecimal afterBalance = beforeBalance.add(amount);

        member.setBalance(afterBalance);
        memberInfoMapper.updateById(member);

        MemberBalanceLog log = new MemberBalanceLog();
        log.setMemberId(memberId);
        log.setMemberNo(member.getMemberNo());
        log.setType(1);
        log.setAmount(amount);
        log.setBeforeBalance(beforeBalance);
        log.setAfterBalance(afterBalance);
        log.setSourceNo(sourceNo);
        log.setSourceType("RECHARGE");
        log.setRemark("会员储值充值");
        balanceLogMapper.insert(log);

        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> consume(Long memberId, BigDecimal amount, Integer points, String sourceNo) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return CommonResult.fail("消费金额必须大于0");
        }

        MemberInfo member = memberInfoMapper.selectById(memberId);
        if (member == null) {
            return CommonResult.fail("会员不存在");
        }
        if (member.getStatus() != null && member.getStatus() == 0) {
            return CommonResult.fail("会员已冻结");
        }
        if (member.getBalance().compareTo(amount) < 0) {
            return CommonResult.fail("余额不足");
        }

        BigDecimal beforeBalance = member.getBalance();
        BigDecimal afterBalance = beforeBalance.subtract(amount);
        int beforePoints = member.getPoints() != null ? member.getPoints() : 0;
        int earnPoints = points != null ? points : 0;
        int afterPoints = beforePoints + earnPoints;

        member.setBalance(afterBalance);
        member.setPoints(afterPoints);
        member.setTotalConsume(member.getTotalConsume().add(amount));
        member.setTotalOrders(member.getTotalOrders() + 1);
        member.setLastConsumeTime(LocalDateTime.now());
        memberInfoMapper.updateById(member);

        MemberBalanceLog balanceLog = new MemberBalanceLog();
        balanceLog.setMemberId(memberId);
        balanceLog.setMemberNo(member.getMemberNo());
        balanceLog.setType(2);
        balanceLog.setAmount(amount.negate());
        balanceLog.setBeforeBalance(beforeBalance);
        balanceLog.setAfterBalance(afterBalance);
        balanceLog.setSourceNo(sourceNo);
        balanceLog.setSourceType("CONSUME");
        balanceLog.setRemark("会员消费扣款");
        balanceLogMapper.insert(balanceLog);

        if (earnPoints > 0) {
            MemberPointLog pointLog = new MemberPointLog();
            pointLog.setMemberId(memberId);
            pointLog.setMemberNo(member.getMemberNo());
            pointLog.setType(1);
            pointLog.setPoints(earnPoints);
            pointLog.setBeforePoints(beforePoints);
            pointLog.setAfterPoints(afterPoints);
            pointLog.setSourceNo(sourceNo);
            pointLog.setSourceType("CONSUME_EARN");
            pointLog.setRemark("消费获得积分");
            pointLogMapper.insert(pointLog);
        }

        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> addPoints(Long memberId, Integer points, String sourceNo, String remark) {
        if (points == null || points <= 0) {
            return CommonResult.fail("积分必须大于0");
        }

        MemberInfo member = memberInfoMapper.selectById(memberId);
        if (member == null) {
            return CommonResult.fail("会员不存在");
        }

        int beforePoints = member.getPoints() != null ? member.getPoints() : 0;
        int afterPoints = beforePoints + points;

        member.setPoints(afterPoints);
        memberInfoMapper.updateById(member);

        MemberPointLog pointLog = new MemberPointLog();
        pointLog.setMemberId(memberId);
        pointLog.setMemberNo(member.getMemberNo());
        pointLog.setType(1);
        pointLog.setPoints(points);
        pointLog.setBeforePoints(beforePoints);
        pointLog.setAfterPoints(afterPoints);
        pointLog.setSourceNo(sourceNo);
        pointLog.setSourceType("MANUAL_ADD");
        pointLog.setRemark(remark != null ? remark : "手动增加积分");
        pointLogMapper.insert(pointLog);

        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> deductPoints(Long memberId, Integer points, String sourceNo, String remark) {
        if (points == null || points <= 0) {
            return CommonResult.fail("积分必须大于0");
        }

        MemberInfo member = memberInfoMapper.selectById(memberId);
        if (member == null) {
            return CommonResult.fail("会员不存在");
        }

        int beforePoints = member.getPoints() != null ? member.getPoints() : 0;
        if (beforePoints < points) {
            return CommonResult.fail("积分不足");
        }

        int afterPoints = beforePoints - points;

        member.setPoints(afterPoints);
        memberInfoMapper.updateById(member);

        MemberPointLog pointLog = new MemberPointLog();
        pointLog.setMemberId(memberId);
        pointLog.setMemberNo(member.getMemberNo());
        pointLog.setType(2);
        pointLog.setPoints(-points);
        pointLog.setBeforePoints(beforePoints);
        pointLog.setAfterPoints(afterPoints);
        pointLog.setSourceNo(sourceNo);
        pointLog.setSourceType("MANUAL_DEDUCT");
        pointLog.setRemark(remark != null ? remark : "手动扣减积分");
        pointLogMapper.insert(pointLog);

        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> upgradeLevel(Long memberId, Long levelId) {
        MemberInfo member = memberInfoMapper.selectById(memberId);
        if (member == null) {
            return CommonResult.fail("会员不存在");
        }

        MemberLevel level = memberLevelMapper.selectById(levelId);
        if (level == null) {
            return CommonResult.fail("等级不存在");
        }

        member.setLevelId(levelId);
        member.setLevelName(level.getLevelName());
        memberInfoMapper.updateById(member);

        return CommonResult.success();
    }

    private String generateMemberNo() {
        return "M" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
