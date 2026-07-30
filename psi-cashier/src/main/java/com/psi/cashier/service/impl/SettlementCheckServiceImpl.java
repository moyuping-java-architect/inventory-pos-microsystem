package com.psi.cashier.service.impl;

import com.psi.cashier.entity.OrderMainEntity;
import com.psi.cashier.mapper.CashierSettlementMapper;
import com.psi.cashier.mapper.OrderMainMapper;
import com.psi.cashier.service.SettlementCheckService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日结校验服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementCheckServiceImpl implements SettlementCheckService {

    private final CashierSettlementMapper cashierSettlementMapper;
    private final OrderMainMapper orderMainMapper;

    /**
     * 缓存：是否可以销售
     */
    private volatile Boolean canSellCache = null;

    /**
     * 缓存：第一个未日结的日期
     */
    private volatile LocalDate unsettledDateCache = null;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public boolean canSell() {
        if (canSellCache != null) {
            return canSellCache;
        }
        return checkCanSell();
    }

    @Override
    public LocalDate getFirstUnsettledDate() {
        if (unsettledDateCache != null) {
            return unsettledDateCache;
        }
        return findFirstUnsettledDate();
    }

    @Override
    public String getUnsettledDateStr() {
        LocalDate date = getFirstUnsettledDate();
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    @Override
    public void updateCache() {
        synchronized (this) {
            canSellCache = checkCanSell();
            unsettledDateCache = findFirstUnsettledDate();
            log.info("日结状态缓存已更新：canSell={}, unsettledDate={}", canSellCache, unsettledDateCache);
        }
    }

    @Override
    public boolean canSettleToday(String dateStr) {
        LambdaQueryWrapper<OrderMainEntity> query = new LambdaQueryWrapper<>();
        query.apply("DATE(create_time) = {0}", dateStr);
        Long count = orderMainMapper.selectCount(query);
        return count > 0;
    }

    /**
     * 检查是否可以销售
     */
    private boolean checkCanSell() {
        LocalDate unsettledDate = findFirstUnsettledDate();
        return unsettledDate == null;
    }

    /**
     * 查找第一个未日结的日期
     */
    private LocalDate findFirstUnsettledDate() {
        LocalDate today = LocalDate.now();
        
        // 从第一天开始检查到昨天
        LocalDate checkDate = LocalDate.of(2026, 1, 1); // 系统开始日期
        while (!checkDate.isAfter(today.minusDays(1))) {
            String dateStr = checkDate.format(DATE_FORMATTER);
            
            // 检查该日期是否有日结单
            int count = cashierSettlementMapper.selectByDate(dateStr).size();
            if (count == 0) {
                // 检查该日期是否有订单
                LambdaQueryWrapper<OrderMainEntity> query = new LambdaQueryWrapper<>();
                query.apply("DATE(create_time) = {0}", dateStr);
                Long orderCount = orderMainMapper.selectCount(query);
                
                if (orderCount > 0) {
                    // 有订单但没有日结单，说明未日结
                    log.warn("发现未日结日期：{}，订单数：{}", dateStr, orderCount);
                    return checkDate;
                }
            }
            checkDate = checkDate.plusDays(1);
        }
        
        return null;
    }
}