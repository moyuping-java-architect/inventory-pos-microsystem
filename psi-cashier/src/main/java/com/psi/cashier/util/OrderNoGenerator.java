package com.psi.cashier.util;

import com.psi.cashier.constant.BizTypeConstant;
import com.psi.cashier.entity.SysConfigEntity;
import com.psi.cashier.entity.SysSeqEntity;
import com.psi.cashier.mapper.SysSeqMapper;
import com.psi.cashier.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 订单号/单据号生成器
 * 生成格式：收银机编码 + 业务类型编码 + yyyymmdd + 6位递增数字（从000001开始）
 * 例如：POS01_XS_20260603000001, POS01_XS_20260603000002*
 * 支持多收银机、多业务类型的序列号隔离
 * 基于 sys_seq 表实现，按 收银机+类型+日期 维度独立计数
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Component
public class OrderNoGenerator {

    private final SysSeqMapper sysSeqMapper;
    private final SysConfigService sysConfigService;

    /**
     * 默认收银机编码（备用值，当数据库未配置时使用）
     */
    private static final String FALLBACK_POS_CODE = "POS01";

    /**
     * 默认序列号位数
     */
    private static final int DEFAULT_DIGIT_LENGTH = 6;

    /**
     * 日期格式：yyyyMMdd
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 单据号分隔符
     */
    private static final String SEPARATOR = "_";

    public OrderNoGenerator(SysSeqMapper sysSeqMapper, SysConfigService sysConfigService) {
        this.sysSeqMapper = sysSeqMapper;
        this.sysConfigService = sysConfigService;
    }

    /**
     * 生成唯一单据号（使用默认收银机编码）
     * 格式：POS01 + 业务类型编码 + yyyymmdd + 6位递增数字
     * @param bizType 业务类型
     * @return 单据号
     */
    public String generate(BizTypeConstant bizType) {
        return generate(getDefaultPosCode(), bizType);
    }

    /**
     * 生成唯一单据号
     * 格式：收银机编码 + 业务类型编码 + yyyymmdd + 6位递增数字
     * 
     * @param posCode 收银机编码
     * @param bizType 业务类型
     * @return 单据号
     */
    public String generate(String posCode, BizTypeConstant bizType) {
        String currentDate = LocalDate.now().format(DATE_FORMATTER);
        return generate(posCode, bizType, currentDate);
    }

    /**
     * 生成唯一单据号（指定日期，用于特殊场景）
     * 
     * @param posCode  收银机编码
     * @param bizType  业务类型
     * @param dateStr  日期字符串（格式：yyyyMMdd）
     * @return 单据号
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generate(String posCode, BizTypeConstant bizType, String dateStr) {
        String seqType = bizType.getCode();
        
        SysSeqEntity entity = sysSeqMapper.selectForUpdate(posCode, seqType, dateStr);
        
        long serialValue;
        if (entity == null) {
            // 当日首次生成，创建新记录
            entity = new SysSeqEntity();
            entity.setPosId(posCode);
            entity.setSeqType(seqType);
            entity.setDay(dateStr);
            entity.setCurrNo(1L);
            sysSeqMapper.insert(entity);
            serialValue = 1;
            log.info("创建新日期序列号记录，收银机：{}，类型：{}，日期：{}", posCode, seqType, dateStr);
        } else {
            // 已有记录，递增序列号
            sysSeqMapper.incrementValue(posCode, seqType, dateStr);
            serialValue = entity.getCurrNo() + 1;
        }
        
        // 生成6位序列号字符串
        String serialStr = String.format("%0" + DEFAULT_DIGIT_LENGTH + "d", serialValue);
        
        // 拼接完整单据号：收银机编码_业务类型_日期序列号
        String orderNo = posCode + SEPARATOR + seqType + SEPARATOR + dateStr + serialStr;
        
        log.debug("生成单据号：{}", orderNo);
        return orderNo;
    }

    /**
     * 批量生成单据号
     * 
     * @param posCode 收银机编码
     * @param bizType 业务类型
     * @param count   生成数量
     * @return 单据号数组
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String[] generateBatch(String posCode, BizTypeConstant bizType, int count) {
        if (count <= 0) {
            return new String[0];
        }

        String currentDate = LocalDate.now().format(DATE_FORMATTER);
        String seqType = bizType.getCode();
        String[] result = new String[count];

        SysSeqEntity entity = sysSeqMapper.selectForUpdate(posCode, seqType, currentDate);

        long startValue;
        if (entity == null) {
            // 当日首次生成，创建新记录
            entity = new SysSeqEntity();
            entity.setPosId(posCode);
            entity.setSeqType(seqType);
            entity.setDay(currentDate);
            entity.setCurrNo((long) count);
            sysSeqMapper.insert(entity);
            startValue = 1;
            log.info("创建新日期序列号记录（批量），收银机：{}，类型：{}，日期：{}，数量：{}", 
                    posCode, seqType, currentDate, count);
        } else {
            // 已有记录，批量递增
            startValue = entity.getCurrNo() + 1;
            entity.setCurrNo(entity.getCurrNo() + count);
            sysSeqMapper.updateById(entity);
        }

        // 生成多个单据号
        for (int i = 0; i < count; i++) {
            long serialValue = startValue + i;
            String serialStr = String.format("%0" + DEFAULT_DIGIT_LENGTH + "d", serialValue);
            result[i] = posCode + SEPARATOR + seqType + SEPARATOR + currentDate + serialStr;
        }

        log.debug("批量生成单据号，起始：{}，结束：{}", result[0], result[count - 1]);
        return result;
    }

    /**
     * 生成销售订单号（快捷方法）
     * 
     * @param posCode 收银机编码
     * @return 销售订单号
     */
    public String generateSaleOrder(String posCode) {
        return generate(posCode, BizTypeConstant.SALE);
    }

    /**
     * 生成退货订单号（快捷方法）
     * 
     * @param posCode 收银机编码
     * @return 退货订单号
     */
    public String generateRefundOrder(String posCode) {
        return generate(posCode, BizTypeConstant.REFUND);
    }

    /**
     * 生成挂单单号（快捷方法）
     * 
     * @param posCode 收银机编码
     * @return 挂单单号
     */
    public String generatePendingOrder(String posCode) {
        return generate(posCode, BizTypeConstant.PENDING);
    }

    /**
     * 生成小票流水号（与销售订单完全共用序列号）
     * 生成的流水号格式与销售订单号完全相同，方便对账
     * 
     * @param posCode 收银机编码
     * @return 小票流水号（格式：POS01_XS_20260603000001）
     */
    public String generateReceipt(String posCode) {
        // 与销售订单完全共用序列号，确保流水号一致
        return generate(posCode, BizTypeConstant.SALE);
    }

    /**
     * 生成小票流水号（使用默认收银机编码）
     * 
     * @return 小票流水号
     */
    public String generateReceipt() {
        return generate(getDefaultPosCode(), BizTypeConstant.SALE);
    }

    /**
     * 获取销售订单当前序列号值（用于小票打印时显示）
     * 
     * @param posCode 收银机编码
     * @return 当前序列号值
     */
    public long getCurrentSaleSeq(String posCode) {
        return getCurrentValue(posCode, BizTypeConstant.SALE);
    }

    /**
     * 生成销售订单号并返回用于小票打印的流水号信息
     * 此方法确保订单号和小票流水号完全一致，便于对账
     * 
     * @param posCode 收银机编码
     * @return 包含订单号和流水号信息的对象
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReceiptResult generateOrderAndReceipt(String posCode) {
        String orderNo = generate(posCode, BizTypeConstant.SALE);
        
        // 提取流水号部分（业务类型_日期_序列号），用于小票打印
        String[] parts = orderNo.split(SEPARATOR);
        String receiptNo = parts.length > 2 ? parts[1] + SEPARATOR + parts[2] : orderNo;
        
        return new ReceiptResult(orderNo, receiptNo, orderNo);
    }

    /**
     * 生成销售订单号并返回用于小票打印的流水号信息（使用默认收银机编码）
     * 
     * @return 包含订单号和流水号信息的对象
     */
    public ReceiptResult generateOrderAndReceipt() {
        return generateOrderAndReceipt(getDefaultPosCode());
    }

    /**
     * 从订单号中提取小票流水号（用于打印小票时获取流水号）
     * 
     * @param orderNo 订单号
     * @return 小票流水号（格式：XS_20260603000001）
     */
    public String extractReceiptNo(String orderNo) {
        if (orderNo == null || orderNo.isEmpty()) {
            return "";
        }
        String[] parts = orderNo.split(SEPARATOR);
        return parts.length > 2 ? parts[1] + SEPARATOR + parts[2] : orderNo;
    }

    /**
     * 从订单号中提取纯流水号（仅日期+数字，用于简洁打印）
     * 
     * @param orderNo 订单号
     * @return 纯流水号（格式：20260603000001）
     */
    public String extractPureSerialNo(String orderNo) {
        if (orderNo == null || orderNo.isEmpty()) {
            return "";
        }
        String[] parts = orderNo.split(SEPARATOR);
        return parts.length > 2 ? parts[2] : orderNo;
    }

    /**
     * 校验订单号和小票流水号是否匹配（用于对账）
     * 
     * @param orderNo   订单号
     * @param receiptNo 小票流水号
     * @return true表示匹配，false表示不匹配
     */
    public boolean isMatching(String orderNo, String receiptNo) {
        if (orderNo == null || receiptNo == null) {
            return false;
        }
        
        // 提取订单号中的流水号部分
        String extractedReceiptNo = extractReceiptNo(orderNo);
        
        // 完全匹配或包含匹配
        return orderNo.equals(receiptNo) || extractedReceiptNo.equals(receiptNo);
    }

    /**
     * 构建完整订单号（根据收银机编码和小票流水号）
     * 用于从小票流水号反查订单
     * 
     * @param posCode   收银机编码
     * @param receiptNo 小票流水号
     * @return 完整订单号
     */
    public String buildOrderNo(String posCode, String receiptNo) {
        if (receiptNo == null || receiptNo.isEmpty()) {
            return "";
        }
        
        // 如果已经是完整订单号，直接返回
        if (receiptNo.contains(posCode + SEPARATOR)) {
            return receiptNo;
        }
        
        // 如果是简化的流水号，拼接完整订单号
        return posCode + SEPARATOR + receiptNo;
    }

    /**
     * 小票打印结果对象
     * 包含订单号、小票流水号（简化版）和完整流水号（与订单号一致）
     */
    public static class ReceiptResult {
        /** 完整订单号（如：POS01_XS_20260603000001） */
        private final String orderNo;
        
        /** 小票流水号（简化版，如：XS_20260603000001） */
        private final String receiptNo;
        
        /** 完整流水号（与订单号完全一致，用于对账，如：POS01_XS_20260603000001） */
        private final String fullReceiptNo;

        public ReceiptResult(String orderNo, String receiptNo, String fullReceiptNo) {
            this.orderNo = orderNo;
            this.receiptNo = receiptNo;
            this.fullReceiptNo = fullReceiptNo;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public String getReceiptNo() {
            return receiptNo;
        }

        public String getFullReceiptNo() {
            return fullReceiptNo;
        }
    }

    /**
     * 重置指定日期的序列号（谨慎使用）
     * 
     * @param posCode 收银机编码
     * @param bizType 业务类型
     * @param dateStr 日期字符串（格式：yyyyMMdd）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reset(String posCode, BizTypeConstant bizType, String dateStr) {
        String seqType = bizType.getCode();
        SysSeqEntity entity = sysSeqMapper.selectForUpdate(posCode, seqType, dateStr);
        if (entity != null) {
            entity.setCurrNo(0L);
            sysSeqMapper.updateById(entity);
            log.warn("重置序列号，收银机：{}，类型：{}，日期：{}", posCode, seqType, dateStr);
        }
    }

    /**
     * 获取当前日期的当前序列号值
     * 
     * @param posCode 收银机编码
     * @param bizType 业务类型
     * @return 当前序列号值
     */
    public long getCurrentValue(String posCode, BizTypeConstant bizType) {
        String currentDate = LocalDate.now().format(DATE_FORMATTER);
        return getCurrentValue(posCode, bizType, currentDate);
    }

    /**
     * 获取指定日期的当前序列号值
     * 
     * @param posCode 收银机编码
     * @param bizType 业务类型
     * @param dateStr 日期字符串（格式：yyyyMMdd）
     * @return 当前序列号值
     */
    public long getCurrentValue(String posCode, BizTypeConstant bizType, String dateStr) {
        String seqType = bizType.getCode();
        SysSeqEntity entity = sysSeqMapper.selectForUpdate(posCode, seqType, dateStr);
        return entity != null ? entity.getCurrNo() : 0;
    }

    /**
     * 获取默认收银机编码
     * 优先从 sys_config 表获取 posId，若未配置则返回备用值
     * 
     * @return 默认收银机编码
     */
    public String getDefaultPosCode() {
        try {
            SysConfigEntity config = sysConfigService.getConfig();
            if (config != null && config.getPosId() != null && !config.getPosId().isEmpty()) {
                return config.getPosId();
            }
        } catch (Exception e) {
            log.warn("从sys_config获取默认收银机编码失败，使用备用值: {}", FALLBACK_POS_CODE, e);
        }
        return FALLBACK_POS_CODE;
    }

    /**
     * 获取默认序列号位数
     * 
     * @return 位数
     */
    public int getDefaultDigitLength() {
        return DEFAULT_DIGIT_LENGTH;
    }
}