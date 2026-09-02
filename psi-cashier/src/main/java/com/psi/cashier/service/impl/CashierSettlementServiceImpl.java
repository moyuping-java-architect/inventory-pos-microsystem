package com.psi.cashier.service.impl;

import com.psi.cashier.constant.BizTypeConstant;
import com.psi.cashier.entity.CashierSettlementEntity;
import com.psi.cashier.entity.OrderMainEntity;
import com.psi.cashier.entity.OrderPayEntity;
import com.psi.cashier.mapper.CashierSettlementMapper;
import com.psi.cashier.mapper.OrderMainMapper;
import com.psi.cashier.mapper.OrderPayMapper;
import com.psi.cashier.service.CashierSettlementService;
import com.psi.cashier.service.SettlementCheckService;
import com.psi.cashier.mq.producer.CashierSyncProducer;
import com.psi.cashier.util.OrderNoGenerator;
import com.psi.common.result.PageResult;
import com.psi.common.util.IdUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.*;

/**
 * 日结服务实现类
 * 实现日结单的CRUD操作及确认功能
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CashierSettlementServiceImpl implements CashierSettlementService {

    private final CashierSettlementMapper cashierSettlementMapper;
    private final OrderMainMapper orderMainMapper;
    private final OrderPayMapper orderPayMapper;
    private final OrderNoGenerator orderNoGenerator;
    private final SettlementCheckService settlementCheckService;
    private final CashierSyncProducer cashierSyncProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CashierSettlementEntity save(CashierSettlementEntity entity) {
        cashierSettlementMapper.insert(entity);
        log.info("保存日结成功，日结单号：{}", entity.getSettleNo());
        return entity;
    }

    @Override
    public CashierSettlementEntity getBySettleNo(String settleNo) {
        return cashierSettlementMapper.selectBySettleNo(settleNo);
    }

    @Override
    public List<CashierSettlementEntity> getByOperatorId(Integer operatorId) {
        return cashierSettlementMapper.selectByOperatorId(operatorId);
    }

    @Override
    public List<CashierSettlementEntity> getByDate(String dateStr) {
        return cashierSettlementMapper.selectByDate(dateStr);
    }

    @Override
    public PageResult<CashierSettlementEntity> queryPage(int pageNum, int pageSize, Integer operatorId, Integer status) {
        LambdaQueryWrapper<CashierSettlementEntity> query = new LambdaQueryWrapper<>();
        if (operatorId != null) {
            query.eq(CashierSettlementEntity::getOperatorId, operatorId);
        }
        if (status != null) {
            query.eq(CashierSettlementEntity::getStatus, status);
        }
        query.orderByDesc(CashierSettlementEntity::getCreateTime);
        IPage<CashierSettlementEntity> page = cashierSettlementMapper.selectPage(new Page<>(pageNum, pageSize), query);
        return PageResult.success(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(CashierSettlementEntity entity) {
        int rows = cashierSettlementMapper.updateById(entity);
        log.info("更新日结成功，日结单号：{}，影响行数：{}", entity.getSettleNo(), rows);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmSettlement(String settleNo) {
        CashierSettlementEntity entity = getBySettleNo(settleNo);
        if (entity == null) {
            return false;
        }
        entity.setStatus(1);
        cashierSettlementMapper.updateById(entity);
        log.info("确认日结成功，日结单号：{}", settleNo);
        return true;
    }

    @Override
    public Map<String, Object> getSettlementSummary(String dateStr) {
        Map<String, Object> result = new HashMap<>();
        
        LambdaQueryWrapper<OrderMainEntity> query = new LambdaQueryWrapper<>();
        query.apply("DATE(create_time) = {0}", dateStr);
        
        List<OrderMainEntity> orders = orderMainMapper.selectList(query);
        
        int orderCount = orders.size();
        double totalAmount = 0;
        double realAmount = 0;
        
        Map<Integer, Double> payTypeAmounts = new HashMap<>();
        Map<Integer, Integer> payTypeCounts = new HashMap<>();
        
        List<Map<String, Object>> orderList = new ArrayList<>();
        
        for (OrderMainEntity order : orders) {
            totalAmount += order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0;
            realAmount += order.getRealAmount() != null ? order.getRealAmount().doubleValue() : 0;
            
            List<OrderPayEntity> pays = orderPayMapper.selectByOrderNo(order.getOrderNo());
            for (OrderPayEntity pay : pays) {
                Integer payMethod = pay.getPayId();
                Double amount = pay.getPayAmount() != null ? pay.getPayAmount().doubleValue() : 0.0;
                
                payTypeAmounts.merge(payMethod, amount, Double::sum);
                payTypeCounts.merge(payMethod, 1, Integer::sum);
            }
            
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("orderNo", order.getOrderNo());
            orderMap.put("totalAmount", order.getTotalAmount());
            orderMap.put("payMethod", pays.isEmpty() ? "-" : pays.get(0).getPayId());
            orderMap.put("createTime", order.getCreateTime());
            orderList.add(orderMap);
        }
        
        List<Map<String, Object>> payTypes = new ArrayList<>();
        payTypeAmounts.forEach((payMethod, amount) -> {
            Map<String, Object> payType = new HashMap<>();
            payType.put("payMethod", payMethod);
            payType.put("count", payTypeCounts.get(payMethod));
            payType.put("amount", amount);
            payTypes.add(payType);
        });
        
        result.put("orderCount", orderCount);
        result.put("totalAmount", totalAmount);
        result.put("realAmount", realAmount);
        result.put("refundAmount", 0.0);
        result.put("payTypes", payTypes);
        result.put("orders", orderList);
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CashierSettlementEntity createSettlement(String dateStr, Integer operatorId) {
        List<CashierSettlementEntity> existing = getByDate(dateStr);
        if (!existing.isEmpty()) {
            log.warn("当日已经存在日结单，日期：{}", dateStr);
            return null;
        }

        // 检查是否为空日结
        if (!settlementCheckService.canSettleToday(dateStr)) {
            log.warn("当日没有订单，不允许空日结，日期：{}", dateStr);
            return null;
        }

        LambdaQueryWrapper<OrderMainEntity> query = new LambdaQueryWrapper<>();
        query.apply("DATE(create_time) = {0}", dateStr);
        
        List<OrderMainEntity> orders = orderMainMapper.selectList(query);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalReal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal cashAmount = BigDecimal.ZERO;
        BigDecimal wechatAmount = BigDecimal.ZERO;
        BigDecimal alipayAmount = BigDecimal.ZERO;
        BigDecimal memberAmount = BigDecimal.ZERO;
        BigDecimal otherAmount = BigDecimal.ZERO;
        
        for (OrderMainEntity order : orders) {
            if (order.getTotalAmount() != null) {
                totalAmount = totalAmount.add(order.getTotalAmount());
            }
            if (order.getRealAmount() != null) {
                totalReal = totalReal.add(order.getRealAmount());
            }
            
            List<OrderPayEntity> pays = orderPayMapper.selectByOrderNo(order.getOrderNo());
            for (OrderPayEntity pay : pays) {
                if (pay.getPayAmount() == null) continue;
                BigDecimal amount = pay.getPayAmount();
                
                if (pay.getPayId() == 1) {
                    cashAmount = cashAmount.add(amount);
                } else if (pay.getPayId() == 2) {
                    wechatAmount = wechatAmount.add(amount);
                } else if (pay.getPayId() == 3) {
                    alipayAmount = alipayAmount.add(amount);
                } else if (pay.getPayId() == 4) {
                    memberAmount = memberAmount.add(amount);
                } else {
                    otherAmount = otherAmount.add(amount);
                }
            }
        }
        
        totalDiscount = totalAmount.subtract(totalReal);
        
        CashierSettlementEntity entity = new CashierSettlementEntity();
        entity.setDataUuid(IdUtils.snowflakeIdStr());
        entity.setSettleNo(orderNoGenerator.generate(orderNoGenerator.getDefaultPosCode(), BizTypeConstant.DAILY_SETTLE));
        entity.setTenantId("1");
        entity.setShopCode("SH001");
        entity.setPosId("POS001");
        entity.setBizType(5);
        entity.setOperatorId(operatorId != null ? operatorId : 1);
        entity.setUsername("admin");
        entity.setRealName("管理员");
        entity.setBeginTime(dateStr + " 00:00:00");
        entity.setEndTime(dateStr + " 23:59:59");
        entity.setTotalOrder(orders.size());
        entity.setTotalAmount(totalAmount);
        entity.setTotalReal(totalReal);
        entity.setTotalDiscount(totalDiscount);
        entity.setCashAmount(cashAmount);
        entity.setWechatAmount(wechatAmount);
        entity.setAlipayAmount(alipayAmount);
        entity.setMemberAmount(memberAmount);
        entity.setOtherAmount(otherAmount);
        entity.setStatus(0);
        
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        entity.setCreateBy("admin");
        entity.setCreateTime(currentTime);
        entity.setUpdateBy("admin");
        entity.setUpdateTime(currentTime);
        
        cashierSettlementMapper.insert(entity);
        
        log.info("创建日结单成功，日结单号：{}，日期：{}，订单数：{}", entity.getSettleNo(), dateStr, orders.size());
        
        // 更新日结状态缓存
        settlementCheckService.updateCache();
        
        // 异步发送日结同步消息到sync-ms
        cashierSyncProducer.syncSettlementAsync(entity.getSettleNo());
        log.info("已触发日结同步消息，日结单号：{}", entity.getSettleNo());
        
        return entity;
    }
}