package com.psi.cashier.service.impl;

import com.psi.cashier.entity.CashierShiftEntity;
import com.psi.cashier.entity.CashierShiftPayEntity;
import com.psi.cashier.entity.OrderMainEntity;
import com.psi.cashier.entity.OrderPayEntity;
import com.psi.cashier.mapper.CashierShiftMapper;
import com.psi.cashier.mapper.CashierShiftPayMapper;
import com.psi.cashier.mapper.OrderMainMapper;
import com.psi.cashier.mapper.OrderPayMapper;
import com.psi.cashier.service.CashierShiftService;
import com.psi.common.context.UserContext;
import com.psi.common.context.UserInfo;
import com.psi.cashier.mq.producer.CashierSyncProducer;
import com.psi.common.util.IdUtils;
import com.psi.common.result.PageResult;
import com.psi.common.mybatis.util.BatchUtils;
import com.psi.cashier.service.CashierShiftPayService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 班次结算服务实现�?
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CashierShiftServiceImpl implements CashierShiftService {

    private final CashierShiftMapper cashierShiftMapper;
    private final CashierShiftPayMapper cashierShiftPayMapper;
    private final OrderMainMapper orderMainMapper;
    private final OrderPayMapper orderPayMapper;
    private final CashierShiftPayService cashierShiftPayService;
    private final BatchUtils batchUtils;
    private final CashierSyncProducer cashierSyncProducer;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public CashierShiftEntity save(CashierShiftEntity entity) {
        String tenantId = UserContext.getTenantId();
        entity.setTenantId(tenantId);
        
        UserInfo userInfo = UserContext.get();
        if (userInfo != null) {
            entity.setShopCode(userInfo.getShopId());
            entity.setCreateBy(userInfo.getUpdateUserId());
            entity.setUpdateBy(userInfo.getUpdateUserId());
        }
        
        String currentTime = LocalDateTime.now().format(DATETIME_FORMATTER);
        if (entity.getId() == null) {
            entity.setCreateTime(currentTime);
        }
        entity.setUpdateTime(currentTime);
        
        cashierShiftMapper.insert(entity);
        return entity;
    }

    @Override
    public CashierShiftEntity getByShiftNo(String shiftNo) {
        String tenantId = UserContext.getTenantId();
        return cashierShiftMapper.selectByShiftNo(tenantId, shiftNo);
    }

    @Override
    public List<CashierShiftEntity> getByOperatorId(Integer operatorId) {
        String tenantId = UserContext.getTenantId();
        return cashierShiftMapper.selectByOperatorId(tenantId, operatorId);
    }

    @Override
    public List<CashierShiftEntity> getByDate(String dateStr) {
        String tenantId = UserContext.getTenantId();
        return cashierShiftMapper.selectByDate(tenantId, dateStr);
    }

    @Override
    public PageResult<CashierShiftEntity> queryPage(int pageNum, int pageSize, Integer operatorId, Integer status) {
        String tenantId = UserContext.getTenantId();
        Page<CashierShiftEntity> page = new Page<>(pageNum, pageSize);
        
        QueryWrapper<CashierShiftEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId);
        if (operatorId != null) {
            wrapper.eq("operator_id", operatorId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        
        IPage<CashierShiftEntity> resultPage = cashierShiftMapper.selectPage(page, wrapper);
        
        return PageResult.success(resultPage.getRecords(), resultPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public boolean update(CashierShiftEntity entity) {
        String tenantId = UserContext.getTenantId();
        entity.setTenantId(tenantId);
        entity.setUpdateTime(LocalDateTime.now().format(DATETIME_FORMATTER));
        return cashierShiftMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmShift(String shiftNo) {
        CashierShiftEntity entity = getByShiftNo(shiftNo);
        if (entity == null) {
            return false;
        }
        
        entity.setStatus(1);
        entity.setUpdateTime(LocalDateTime.now().format(DATETIME_FORMATTER));
        return cashierShiftMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String shiftNo) {
        CashierShiftEntity entity = getByShiftNo(shiftNo);
        if (entity == null) {
            return false;
        }
        
        // 删除支付明细
        String tenantId = UserContext.getTenantId();
        cashierShiftPayMapper.deleteByShiftNo(tenantId, shiftNo);
        
        // 删除主记�?
        return cashierShiftMapper.deleteById(entity.getId()) > 0;
    }

    @Override
    public Map<String, Object> calculateShiftData(Integer operatorId, String beginTime, String endTime) {
        String tenantId = UserContext.getTenantId();
        Map<String, Object> result = new HashMap<>();
        
        // 查询订单主表统计
        QueryWrapper<OrderMainEntity> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("tenant_id", tenantId);
        orderWrapper.eq("operator_id", operatorId);
        orderWrapper.ge("create_time", beginTime);
        orderWrapper.le("create_time", endTime);
        
        List<OrderMainEntity> orders = orderMainMapper.selectList(orderWrapper);
        
        int totalOrder = orders.size();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalReal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        
        for (OrderMainEntity order : orders) {
            totalAmount = totalAmount.add(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
            totalReal = totalReal.add(order.getRealAmount() != null ? order.getRealAmount() : BigDecimal.ZERO);
            totalDiscount = totalDiscount.add(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO);
        }
        
        result.put("totalOrder", totalOrder);
        result.put("totalAmount", totalAmount);
        result.put("totalReal", totalReal);
        result.put("totalDiscount", totalDiscount);
        
        // 查询支付方式统计
        QueryWrapper<OrderPayEntity> payWrapper = new QueryWrapper<>();
        payWrapper.eq("tenant_id", tenantId);
        
        List<String> orderNos = orders.stream()
                .map(OrderMainEntity::getOrderNo)
                .toList();
        
        if (!orderNos.isEmpty()) {
            payWrapper.in("order_no", orderNos);
            List<OrderPayEntity> pays = orderPayMapper.selectList(payWrapper);
            
            BigDecimal cashAmount = BigDecimal.ZERO;
            BigDecimal wechatAmount = BigDecimal.ZERO;
            BigDecimal alipayAmount = BigDecimal.ZERO;
            BigDecimal memberAmount = BigDecimal.ZERO;
            BigDecimal otherAmount = BigDecimal.ZERO;
            
            Map<Integer, BigDecimal> payMap = new HashMap<>();
            for (OrderPayEntity pay : pays) {
                BigDecimal amount = pay.getPayAmount() != null ? pay.getPayAmount() : BigDecimal.ZERO;
                payMap.merge(pay.getPayId(), amount, BigDecimal::add);
            }
            
            // 1-现金 2-微信 3-支付�?4-会员�?
            cashAmount = payMap.getOrDefault(1, BigDecimal.ZERO);
            wechatAmount = payMap.getOrDefault(2, BigDecimal.ZERO);
            alipayAmount = payMap.getOrDefault(3, BigDecimal.ZERO);
            memberAmount = payMap.getOrDefault(4, BigDecimal.ZERO);
            
            // 其他支付方式
            for (Map.Entry<Integer, BigDecimal> entry : payMap.entrySet()) {
                if (entry.getKey() != 1 && entry.getKey() != 2 && entry.getKey() != 3 && entry.getKey() != 4) {
                    otherAmount = otherAmount.add(entry.getValue());
                }
            }
            
            result.put("cashAmount", cashAmount);
            result.put("wechatAmount", wechatAmount);
            result.put("alipayAmount", alipayAmount);
            result.put("memberAmount", memberAmount);
            result.put("otherAmount", otherAmount);
            result.put("payDetails", payMap);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CashierShiftEntity createShift(Integer operatorId, String operatorName, BigDecimal cashBegin) {
        String tenantId = UserContext.getTenantId();
        String currentDate = LocalDate.now().format(DATE_FORMATTER);
        
        // 从上下文获取用户信息
        UserInfo userInfo = UserContext.get();
        String shopCode = userInfo != null ? userInfo.getShopId() : "";
        String userId = userInfo != null ? userInfo.getUpdateUserId() : "";
        
        // 检查是否已有未完成的班次
        if (hasUnfinishedShift(operatorId)) {
            throw new RuntimeException("当前收银员已有未完成的班次，请先完成或取消班次记录");
        }
        
        // 生成班次单号
        String shiftNo = generateShiftNo();
        
        // 获取当前时间
        String currentTime = LocalDateTime.now().format(DATETIME_FORMATTER);
        
        // 获取上次班次结束时间作为本次开始时间
        // 如果没有历史班次，使用当天的开始时间（08:00:00）作为默认上班时间
        CashierShiftEntity lastShift = getLastShift(operatorId);
        String beginTime;
        if (lastShift != null) {
            beginTime = lastShift.getEndTime();
        } else {
            // 没有历史班次，使用当天 08:00:00 作为开始时间
            beginTime = LocalDate.now().format(DATE_FORMATTER) + " 08:00:00";
        }
        
        // 计算班次数据
        Map<String, Object> shiftData = calculateShiftData(operatorId, beginTime, currentTime);
        
        // 构建班次实体
        CashierShiftEntity entity = new CashierShiftEntity();
        entity.setDataUuid(IdUtils.snowflakeIdStr());
        entity.setShiftNo(shiftNo);
        entity.setTenantId(tenantId);
        entity.setShopCode(shopCode);
        entity.setBizType(6);
        entity.setOperatorId(operatorId);
        entity.setOperatorName(operatorName);
        entity.setPosId("POS001");
        entity.setBeginTime(beginTime);
        entity.setEndTime(currentTime);
        entity.setCashBegin(cashBegin != null ? cashBegin : BigDecimal.ZERO);
        
        // 设置统计数据
        entity.setTotalOrder((Integer) shiftData.getOrDefault("totalOrder", 0));
        entity.setTotalAmount((BigDecimal) shiftData.getOrDefault("totalAmount", BigDecimal.ZERO));
        entity.setTotalReal((BigDecimal) shiftData.getOrDefault("totalReal", BigDecimal.ZERO));
        entity.setTotalDiscount((BigDecimal) shiftData.getOrDefault("totalDiscount", BigDecimal.ZERO));
        entity.setCashAmount((BigDecimal) shiftData.getOrDefault("cashAmount", BigDecimal.ZERO));
        entity.setWechatAmount((BigDecimal) shiftData.getOrDefault("wechatAmount", BigDecimal.ZERO));
        entity.setAlipayAmount((BigDecimal) shiftData.getOrDefault("alipayAmount", BigDecimal.ZERO));
        entity.setMemberAmount((BigDecimal) shiftData.getOrDefault("memberAmount", BigDecimal.ZERO));
        entity.setOtherAmount((BigDecimal) shiftData.getOrDefault("otherAmount", BigDecimal.ZERO));
        
        // 计算系统现金总额
        BigDecimal cashEnd = entity.getCashBegin().add(entity.getCashAmount());
        entity.setCashEnd(cashEnd);
        entity.setCashReality(BigDecimal.ZERO);
        entity.setCashDiff(BigDecimal.ZERO);
        entity.setStatus(0);
        entity.setCreateBy(userId);
        entity.setUpdateBy(userId);
        entity.setCreateTime(currentTime);
        entity.setUpdateTime(currentTime);
        
        // 保存班次
        cashierShiftMapper.insert(entity);
        
        // 保存支付明细
        @SuppressWarnings("unchecked")
        Map<Integer, BigDecimal> payDetails = (Map<Integer, BigDecimal>) shiftData.get("payDetails");
        if (payDetails != null && !payDetails.isEmpty()) {
            List<CashierShiftPayEntity> payList = new ArrayList<>();
            Map<Integer, String> payNameMap = Map.of(1, "现金", 2, "微信", 3, "支付宝", 4, "会员");
            
            for (Map.Entry<Integer, BigDecimal> entry : payDetails.entrySet()) {
                CashierShiftPayEntity payEntity = new CashierShiftPayEntity();
                payEntity.setDataUuid(IdUtils.snowflakeIdStr());
                payEntity.setTenantId(tenantId);
                payEntity.setShopCode(entity.getShopCode());
                payEntity.setPosId(entity.getPosId());
                payEntity.setShiftNo(shiftNo);
                payEntity.setPayId(entry.getKey());
                payEntity.setPayName(payNameMap.getOrDefault(entry.getKey(), "其他"));
                payEntity.setPayAmount(entry.getValue());
                payEntity.setCreateBy(entity.getCreateBy());
                payEntity.setCreateTime(currentTime);
                payList.add(payEntity);
            }
            
            if (!payList.isEmpty()) {
                batchUtils.saveBatch(cashierShiftPayService, payList);
            }
        }
        
        log.info("班次结算创建成功，班次号：{}，收银员：{}", shiftNo, operatorName);
        
        // 异步发送班次同步消息到sync-ms
        cashierSyncProducer.syncShiftAsync(shiftNo);
        log.info("已触发班次同步消息，班次号：{}", shiftNo);
        
        return entity;
    }

    @Override
    public List<CashierShiftPayEntity> getShiftPayList(String shiftNo) {
        String tenantId = UserContext.getTenantId();
        return cashierShiftPayMapper.selectByShiftNo(tenantId, shiftNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveShiftPayList(String shiftNo, List<CashierShiftPayEntity> payList) {
        String tenantId = UserContext.getTenantId();
        
        // 删除原有明细
        cashierShiftPayMapper.deleteByShiftNo(tenantId, shiftNo);
        
        // 保存新明�?
        if (payList != null && !payList.isEmpty()) {
            for (CashierShiftPayEntity pay : payList) {
                pay.setTenantId(tenantId);
                pay.setShiftNo(shiftNo);
            }
            batchUtils.saveBatch(cashierShiftPayService, payList);
        }
    }

    @Override
    public boolean hasUnfinishedShift(Integer operatorId) {
        String tenantId = UserContext.getTenantId();
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        return cashierShiftMapper.countUnfinishedByOperator(tenantId, operatorId, dateStr) > 0;
    }

    @Override
    public CashierShiftEntity getLastShift(Integer operatorId) {
        String tenantId = UserContext.getTenantId();
        return cashierShiftMapper.selectLastByOperator(tenantId, operatorId);
    }

    /**
     * 生成班次单号
     */
    private String generateShiftNo() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER).replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        return "SH" + dateStr + timestamp;
    }
}