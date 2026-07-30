package com.psi.cashier.service.impl;

import com.psi.cashier.entity.MemberEntity;
import com.psi.cashier.entity.MemberLevelEntity;
import com.psi.cashier.entity.MemberPriceEntity;
import com.psi.cashier.mapper.MemberLevelMapper;
import com.psi.cashier.mapper.MemberMapper;
import com.psi.cashier.mapper.MemberPriceMapper;
import com.psi.cashier.service.MemberService;
import com.psi.common.context.UserContext;
import com.psi.common.util.IdUtils;
import com.psi.cashier.mq.producer.CashierSyncProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 会员服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final MemberPriceMapper memberPriceMapper;
    private final CashierSyncProducer cashierSyncProducer;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public MemberEntity getByPhone(String phone) {
        String tenantId = UserContext.getTenantId();
        return memberMapper.selectByPhone(tenantId, phone);
    }

    @Override
    public MemberEntity getByDataUuid(String dataUuid) {
        String tenantId = UserContext.getTenantId();
        return memberMapper.selectByDataUuid(tenantId, dataUuid);
    }

    @Override
    public MemberEntity register(String phone, String name) {
        String tenantId = UserContext.getTenantId();
        
        // 检查是否已存在
        MemberEntity existing = memberMapper.selectByPhone(tenantId, phone);
        if (existing != null) {
            log.info("会员已存在，手机号：{}", phone);
            return existing;
        }

        // 获取初始等级，如果没有则创建默认等级
        MemberLevelEntity defaultLevel = memberLevelMapper.selectMinLevel(tenantId);
        int levelId;
        if (defaultLevel != null) {
            levelId = defaultLevel.getLevelId();
        } else {
            // 创建默认会员等级
            defaultLevel = createDefaultMemberLevel(tenantId);
            levelId = defaultLevel.getLevelId();
        }

        // 创建新会员
        MemberEntity member = new MemberEntity();
        member.setTenantId(tenantId);
        member.setDataUuid(IdUtils.snowflakeIdStr());
        member.setDelFlag(0);
        member.setPhone(phone);
        member.setName(name != null && !name.isEmpty() ? name : phone);
        member.setPassword("123456");
        member.setBalance(0.00);
        member.setPoint(0);
        member.setLevel(levelId);
        member.setStatus(1);
        
        String currentTime = LocalDateTime.now().format(DATETIME_FORMATTER);
        member.setCreateTime(currentTime);
        member.setUpdateTime(currentTime);

        memberMapper.insert(member);
        log.info("新会员注册成功，手机号：{}，等级：{}", phone, levelId);
        
        // 异步发送会员同步消息到sync-ms
        cashierSyncProducer.syncMemberAsync(phone);
        log.info("已触发会员同步消息，手机号：{}", phone);
        
        return member;
    }

    @Override
    public MemberLevelEntity getMemberLevel(Integer levelId) {
        String tenantId = UserContext.getTenantId();
        return memberLevelMapper.selectByLevelId(tenantId, levelId);
    }

    @Override
    public Double getMemberPrice(Integer skuId, Integer memberLevel) {
        String tenantId = UserContext.getTenantId();
        MemberPriceEntity memberPrice = memberPriceMapper.selectBySkuIdAndLevel(tenantId, skuId, memberLevel);
        return memberPrice != null ? memberPrice.getMemberPrice() : null;
    }

    @Override
    public Double getMemberDiscount(Integer memberLevel) {
        MemberLevelEntity level = getMemberLevel(memberLevel);
        return level != null ? level.getDiscount() : 1.0;
    }

    /**
     * 生成会员编码
     */
    private String generateMemberCode() {
        return "M" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    /**
     * 创建默认会员等级
     */
    private MemberLevelEntity createDefaultMemberLevel(String tenantId) {
        MemberLevelEntity level = new MemberLevelEntity();
        level.setDataUuid(IdUtils.snowflakeIdStr());
        level.setTenantId(tenantId);
        level.setLevelId(1);
        level.setLevelName("普通会员");
        level.setDiscount(1.0);
        level.setNeedPoint(0);
        level.setDelFlag(0);
        
        memberLevelMapper.insert(level);
        log.info("创建默认会员等级成功，等级ID：1，等级名称：普通会员");
        
        return level;
    }

    @Override
    public void addPoints(Integer memberId, Integer points) {
        if (memberId == null || points == null || points <= 0) {
            return;
        }
        
        MemberEntity member = memberMapper.selectById(memberId);
        if (member != null) {
            member.setPoint(member.getPoint() + points);
            member.setUpdateTime(LocalDateTime.now().format(DATETIME_FORMATTER));
            memberMapper.updateById(member);
            log.info("会员积分增加成功，会员ID：{}，增加积分：{}，当前积分：{}", memberId, points, member.getPoint());
        }
    }

    @Override
    public Double recharge(Integer memberId, Double amount) {
        if (memberId == null || amount == null || amount <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }

        MemberEntity member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }

        Double beforeBalance = member.getBalance() != null ? member.getBalance() : 0.0;
        Double afterBalance = beforeBalance + amount;
        member.setBalance(afterBalance);
        member.setUpdateTime(LocalDateTime.now().format(DATETIME_FORMATTER));
        memberMapper.updateById(member);

        log.info("会员储值充值成功，会员ID：{}，充值金额：{}，充值前余额：{}，充值后余额：{}",
                memberId, amount, beforeBalance, afterBalance);

        // 异步同步到服务端
        cashierSyncProducer.syncMemberAsync(member.getPhone());

        return afterBalance;
    }

    @Override
    public Double deductBalance(Integer memberId, Double amount, String orderNo) {
        if (memberId == null || amount == null || amount <= 0) {
            throw new IllegalArgumentException("扣款金额必须大于0");
        }

        MemberEntity member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }

        Double currentBalance = member.getBalance() != null ? member.getBalance() : 0.0;
        if (currentBalance < amount) {
            log.warn("会员储值余额不足，会员ID：{}，当前余额：{}，需扣减：{}", memberId, currentBalance, amount);
            return null;
        }

        Double afterBalance = currentBalance - amount;
        member.setBalance(afterBalance);
        member.setUpdateTime(LocalDateTime.now().format(DATETIME_FORMATTER));
        memberMapper.updateById(member);

        log.info("会员储值扣款成功，会员ID：{}，扣款金额：{}，扣款前余额：{}，扣款后余额：{}，订单号：{}",
                memberId, amount, currentBalance, afterBalance, orderNo);

        return afterBalance;
    }
}