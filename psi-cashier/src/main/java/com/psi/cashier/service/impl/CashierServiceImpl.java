package com.psi.cashier.service.impl;

import com.psi.cashier.constant.BizTypeConstant;
import com.psi.cashier.dto.CashierItemSaveDTO;
import com.psi.cashier.dto.CashierMainSaveDTO;
import com.psi.cashier.dto.CashierPaySaveDTO;
import com.psi.cashier.dto.MobileMoneyResult;
import com.psi.cashier.entity.MemberEntity;
import com.psi.cashier.mapper.MemberMapper;
import com.psi.cashier.entity.OrderItemEntity;
import com.psi.cashier.entity.OrderMainEntity;
import com.psi.cashier.entity.OrderPayEntity;
import com.psi.cashier.service.CashierService;
import com.psi.cashier.service.ExchangeRateService;
import com.psi.cashier.service.MemberService;
import com.psi.cashier.service.MobileMoneyService;
import com.psi.cashier.service.OrderItemService;
import com.psi.cashier.service.OrderMainService;
import com.psi.cashier.service.OrderPayService;
import com.psi.cashier.service.PromotionService;
import com.psi.cashier.service.ReceiptPrintService;
import com.psi.cashier.util.OrderNoGenerator;
import com.psi.cashier.mq.producer.CashierSyncProducer;
import com.psi.common.context.UserContext;
import com.psi.common.context.UserInfo;
import com.psi.common.util.IdUtils;
import com.psi.common.context.VirtualThreadContextWrapper;
import com.psi.common.mybatis.util.BatchUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 收银服务实现类
 * 负责处理收银订单的保存逻辑，包括主订单、订单明细和支付明细
 * 支持多种支付方式组合支付
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CashierServiceImpl implements CashierService {

    /**
     * 主订单服务
     */
    private final OrderMainService orderMainService;

    /**
     * 订单明细服务
     */
    private final OrderItemService orderItemService;

    /**
     * 支付明细服务
     */
    private final OrderPayService orderPayService;

    /**
     * 批量操作工具类（通过Nacos配置管理批次大小）
     */
    private final BatchUtils batchUtils;

    /**
     * 订单号生成器（格式：收银机编码_业务类型_yyyymmdd_6位递增数字）
     */
    private final OrderNoGenerator orderNoGenerator;

    /**
     * 小票打印服务
     */
    private final ReceiptPrintService receiptPrintService;

    /**
     * 会员服务
     */
    private final MemberService memberService;

    /**
     * 促销服务（本地计算促销优惠）
     */
    private final PromotionService promotionService;

    /**
     * 会员Mapper
     */
    private final MemberMapper memberMapper;

    /**
     * MQ同步生产者
     */
    private final CashierSyncProducer cashierSyncProducer;

    /**
     * 汇率服务
     */
    private final ExchangeRateService exchangeRateService;

    /**
     * Mobile Money 服务
     */
    private final MobileMoneyService mobileMoneyService;

    /**
     * 支付时间日期时间格式
     */
    private static final DateTimeFormatter PAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 业务类型：零售收款
     */
    private static final int BIZ_TYPE_RETAIL = 60;

    /**
     * 支付方式：Mobile Money
     */
    private static final int PAY_TYPE_MOBILE_MONEY = 1;

    /**
     * 赞比亚默认 VAT 税率 16%
     */
    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.16");

    /**
     * 保存收银订单（同步打印）
     * 事务性操作，确保主订单、订单明细、支付明细要么全部保存成功，要么全部回滚
     * 支持多种支付方式组合支付
     *
     * @param dto 收银保存数据传输对象
     * @return 保存成功的主订单实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderMainEntity saveOrder(CashierMainSaveDTO dto) {
        return saveOrder(dto, false);
    }

    /**
     * 保存收银订单
     * 事务性操作，确保主订单、订单明细、支付明细要么全部保存成功，要么全部回滚
     * 支持多种支付方式组合支付
     *
     * @param dto        收银保存数据传输对象
     * @param asyncPrint 是否异步打印小票
     * @return 保存成功的主订单实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderMainEntity saveOrder(CashierMainSaveDTO dto, boolean asyncPrint) {
        // 使用订单号生成器生成唯一订单号（格式：收银机编码_XH_20260603000001，收银机编码从sys_config表获取）
        String orderNo = orderNoGenerator.generate(BizTypeConstant.SALE);
        log.info("开始保存收银订单，订单号：{}", orderNo);

        // 先处理 Mobile Money 收款：收款成功后才落库，若落库失败则补偿撤销
        List<MobileMoneyTransaction> mobileMoneyTxs = processMobileMoneyPays(dto.getPays(), orderNo);

        OrderMainEntity orderMain = buildOrderMain(dto, orderNo);
        try {
            // 保存主订单
            orderMainService.save(orderMain);
            log.debug("主订单保存成功，订单号：{}", orderNo);

            // 获取会员等级
            Integer memberLevel = null;
            if (dto.getMemberId() != null) {
                MemberEntity member = memberMapper.selectById(dto.getMemberId());
                if (member != null) {
                    memberLevel = member.getLevel();
                }
            }

            // 批量保存订单明细（使用BatchUtils进行批次控制）
            List<OrderItemEntity> items = buildOrderItems(dto.getItems(), orderNo, orderMain.getShopCode(), orderMain.getPosId(), orderMain.getBizType(), memberLevel);
            if (!items.isEmpty()) {
                batchUtils.saveBatch(orderItemService, items);
                log.debug("订单明细批量保存成功，订单号：{}，明细数量：{}", orderNo, items.size());
            }

            // 根据订单明细汇总 VAT 净额与税额，并更新主订单
            BigDecimal netAmount = items.stream()
                    .map(OrderItemEntity::getNetAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal taxAmount = items.stream()
                    .map(OrderItemEntity::getTaxAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalAmount = netAmount.add(taxAmount);
            orderMain.setNetAmount(netAmount);
            orderMain.setTaxAmount(taxAmount);
            orderMain.setTotalAmount(totalAmount);

            // 折扣计算：优先使用前端传入的折扣，否则自动计算促销优惠
        if (dto.getDiscountAmount() != null) {
            orderMain.setDiscountAmount(dto.getDiscountAmount().max(BigDecimal.ZERO));
        } else {
            // 自动计算促销优惠
            BigDecimal promotionDiscount = promotionService.calculateDiscount(dto);
            if (promotionDiscount.compareTo(BigDecimal.ZERO) > 0) {
                log.info("促销优惠自动计算：订单号：{}，促销优惠金额：{}", orderNo, promotionDiscount);
            }
            orderMain.setDiscountAmount(promotionDiscount.max(
                    totalAmount.subtract(dto.getPayAmount()).max(BigDecimal.ZERO)
            ));
        }
            orderMainService.updateById(orderMain);
            log.debug("主订单 VAT 汇总更新成功，订单号：{}，netAmount={}，taxAmount={}", orderNo, netAmount, taxAmount);

            // 批量保存支付明细（支持多种支付方式）
            List<OrderPayEntity> pays = buildOrderPays(dto.getPays(), orderNo, orderMain.getShopCode(), orderMain.getPosId());
            if (!pays.isEmpty()) {
                batchUtils.saveBatch(orderPayService, pays);
                log.debug("支付明细批量保存成功，订单号：{}，支付方式数量：{}", orderNo, pays.size());
            }
        } catch (Exception e) {
            log.error("收银订单落库失败，触发 Mobile Money 补偿撤销，订单号：{}", orderNo, e);
            reverseMobileMoneyPays(mobileMoneyTxs, orderNo);
            throw e;
        }

        // 打印小票（根据参数决定同步或异步，直接使用DTO数据避免重复查询数据库）
        if (asyncPrint) {
            VirtualThreadContextWrapper.executeAsync(() -> {
                try {
                    receiptPrintService.print(dto, orderNo);
                } catch (Exception e) {
                    log.error("异步打印小票失败，订单号：{}", orderNo, e);
                }
            });
            log.info("订单保存完成，已触发异步打印（直接使用DTO数据），订单号：{}", orderNo);
        } else {
            receiptPrintService.print(dto, orderNo);
            log.info("订单保存完成，同步打印完成（直接使用DTO数据），订单号：{}", orderNo);
        }

        // 如果有会员，处理储值抵扣和积分
        if (dto.getMemberId() != null) {
            // 检查支付明细中是否使用了会员储值抵扣
            BigDecimal memberPayAmount = BigDecimal.ZERO;
            for (CashierPaySaveDTO pay : dto.getPays()) {
                if ("MEMBER_BALANCE".equals(pay.getPayChannel())) {
                    memberPayAmount = memberPayAmount.add(pay.getPayAmount());
                }
            }

            // 会员储值扣款
            if (memberPayAmount.compareTo(BigDecimal.ZERO) > 0) {
                Double afterBalance = memberService.deductBalance(
                        dto.getMemberId(), memberPayAmount.doubleValue(), orderNo);
                if (afterBalance == null) {
                    log.error("会员储值余额不足，订单号：{}，会员ID：{}，需扣减：{}",
                            orderNo, dto.getMemberId(), memberPayAmount);
                    throw new RuntimeException("会员储值余额不足");
                }
                log.info("会员储值抵扣成功，订单号：{}，会员ID：{}，抵扣金额：{}，抵扣后余额：{}",
                        orderNo, dto.getMemberId(), memberPayAmount, afterBalance);
            }

            // 增加积分（每消费1元增加1积分，储值抵扣部分也累计积分）
            int points = dto.getPayAmount().intValue();
            if (points > 0) {
                memberService.addPoints(dto.getMemberId(), points);
            }
            log.info("订单支付完成，会员ID：{}，消费金额：{}，增加积分：{}", dto.getMemberId(), dto.getPayAmount(), points);
        }

        return orderMain;
    }

    /**
     * 构建主订单实体
     *
     * @param dto     收银保存数据传输对象
     * @param orderNo 订单号
     * @return 主订单实体
     */
    private OrderMainEntity buildOrderMain(CashierMainSaveDTO dto, String orderNo) {
        OrderMainEntity entity = new OrderMainEntity();
        entity.setDataUuid(IdUtils.snowflakeIdStr());
        entity.setOrderNo(orderNo);
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setRealAmount(dto.getPayAmount());

        // 设置折扣金额，如果DTO中已设置则使用，否则计算（避免找零时折扣为负数）
        if (dto.getDiscountAmount() != null) {
            entity.setDiscountAmount(dto.getDiscountAmount().max(BigDecimal.ZERO));
        } else {
            BigDecimal computedDiscount = dto.getTotalAmount().subtract(dto.getPayAmount());
            entity.setDiscountAmount(computedDiscount.max(BigDecimal.ZERO));
        }
        
        // 设置会员ID
        if (dto.getMemberId() != null) {
            entity.setMemberId(dto.getMemberId());
        }
        
        // 设置操作员ID
        if (dto.getOperatorId() != null) {
            entity.setOperatorId(dto.getOperatorId());
        }
        
        entity.setPayStatus(1); // 1表示已支付
        entity.setTenantId(UserContext.getTenantId());
        
        UserInfo userInfo = UserContext.get();
        if (userInfo != null) {
            entity.setShopCode(userInfo.getShopId());
            entity.setCreateBy(userInfo.getUpdateUserId());
            entity.setUpdateBy(userInfo.getUpdateUserId());
        } else {
            entity.setShopCode("SH001");
            entity.setCreateBy("1");
            entity.setUpdateBy("1");
        }
        
        entity.setPosId(dto.getPosId() != null && !dto.getPosId().isEmpty() ? dto.getPosId() : "POS001");
        entity.setBizType(dto.getBizType() != null ? dto.getBizType() : 20);

        // 币种与汇率
        String currency = dto.getCurrency() != null ? dto.getCurrency().toUpperCase() : "ZMW";
        entity.setCurrency(currency);
        if (dto.getExchangeRate() != null) {
            entity.setExchangeRate(dto.getExchangeRate());
        } else if (!"ZMW".equals(currency)) {
            entity.setExchangeRate(exchangeRateService.getEffectiveRate(currency, "ZMW"));
        } else {
            entity.setExchangeRate(BigDecimal.ONE);
        }
        entity.setOriginalAmount(dto.getOriginalAmount());

        // 设置创建时间和修改时间
        String currentTime = LocalDateTime.now().format(PAY_TIME_FORMATTER);
        entity.setCreateTime(currentTime);
        entity.setUpdateTime(currentTime);

        return entity;
    }

    /**
     * 构建订单明细实体列表
     *
     * @param items      明细数据传输对象列表
     * @param orderNo    订单号
     * @param shopCode   门店编码
     * @param posId      收银机ID
     * @param bizType    业务类型
     * @param memberLevel 会员等级（可为null）
     * @return 订单明细实体列表
     */
    private List<OrderItemEntity> buildOrderItems(List<CashierItemSaveDTO> items, String orderNo, String shopCode, String posId, Integer bizType, Integer memberLevel) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        
        String tenantId = UserContext.getTenantId();
        
        return items.stream().map(item -> {
            OrderItemEntity entity = new OrderItemEntity();
            entity.setDataUuid(IdUtils.snowflakeIdStr());
            entity.setTenantId(tenantId);
            entity.setShopCode(shopCode);
            entity.setPosId(posId);
            entity.setOrderNo(orderNo);
            entity.setBizType(bizType);
            Integer skuId = parseIntegerSafely(item.getSkuId());
            if (skuId == null) {
                log.error("skuId 解析失败，前端传入的值: {}, 商品编码: {}, 商品名称: {}", 
                    item.getSkuId(), item.getGoodsCode(), item.getGoodsName());
                throw new IllegalArgumentException("skuId 必须是有效的整数，当前值: " + item.getSkuId());
            }
            entity.setSkuId(skuId);
            entity.setSkuCode(item.getGoodsCode());
            entity.setBarCode(item.getBarCode());
            entity.setProductName(item.getGoodsName());
            entity.setSaleUnitName(item.getUnit());
            entity.setSaleQuantity(item.getQuantity());
            entity.setUnitPrice(item.getUnitPrice());

            // 如果有会员等级，尝试获取会员价
            BigDecimal memberPrice = null;
            if (memberLevel != null && skuId != null) {
                Double price = memberService.getMemberPrice(skuId, memberLevel);
                if (price != null) {
                    memberPrice = BigDecimal.valueOf(price);
                }
            }

            // 计算不含税净额：优先使用会员价，否则使用单价
            BigDecimal unitPrice = memberPrice != null ? memberPrice : item.getUnitPrice();
            if (memberPrice != null) {
                entity.setMemberPrice(memberPrice);
            } else {
                entity.setMemberPrice(BigDecimal.ZERO);
            }

            BigDecimal taxRate = item.getTaxRate() != null ? item.getTaxRate() : DEFAULT_TAX_RATE;
            boolean taxInclusive = item.getIsTaxInclusive() != null && item.getIsTaxInclusive() == 1;

            BigDecimal grossAmount = unitPrice.multiply(item.getQuantity());
            BigDecimal netAmount;
            BigDecimal taxAmount;
            BigDecimal subtotal;
            if (taxInclusive) {
                // 含税价：先拆出净额，再算税额，subtotal 保持为含税总价
                netAmount = grossAmount.divide(BigDecimal.ONE.add(taxRate), 4, RoundingMode.HALF_UP);
                taxAmount = netAmount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
                netAmount = grossAmount.subtract(taxAmount);
                subtotal = grossAmount;
            } else {
                // 价外税：净额 = 数量*单价，税额 = 净额*税率
                netAmount = grossAmount;
                taxAmount = netAmount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
                subtotal = netAmount.add(taxAmount);
            }

            entity.setNetAmount(netAmount.setScale(2, RoundingMode.HALF_UP));
            entity.setTaxRate(taxRate);
            entity.setTaxAmount(taxAmount);
            entity.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
            entity.setIsTaxInclusive(taxInclusive ? 1 : 0);
            entity.setBatchNo(item.getBatchNo());
            entity.setCurrency(item.getCurrency());

            return entity;
        }).toList();
    }

    /**
     * 处理 Mobile Money 支付：调用运营商收款接口，并将返回的流水号写回 DTO。
     * 返回已成功扣款的交易列表，用于后续异常补偿。
     */
    private List<MobileMoneyTransaction> processMobileMoneyPays(List<CashierPaySaveDTO> pays, String orderNo) {
        List<MobileMoneyTransaction> successList = new ArrayList<>();
        if (pays == null || pays.isEmpty()) {
            return successList;
        }
        for (CashierPaySaveDTO pay : pays) {
            if (!isMobileMoneyPay(pay)) {
                continue;
            }
            MobileMoneyResult result = mobileMoneyService.collect(
                    pay.getMobileProvider(), pay.getMobilePhone(), pay.getPayAmount(), orderNo);
            if (!result.isSuccess()) {
                log.error("Mobile Money 收款失败，订单号：{}，运营商：{}，原因：{}",
                        orderNo, pay.getMobileProvider(), result.getMessage());
                throw new IllegalStateException("Mobile Money 收款失败：" + result.getMessage());
            }
            pay.setMobileTransactionNo(result.getTransactionNo());
            successList.add(new MobileMoneyTransaction(pay.getMobileProvider(), result.getTransactionNo()));
            log.info("Mobile Money 收款成功，订单号：{}，运营商：{}，流水号：{}",
                    orderNo, pay.getMobileProvider(), result.getTransactionNo());
        }
        return successList;
    }

    private void reverseMobileMoneyPays(List<MobileMoneyTransaction> transactions, String orderNo) {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }
        for (MobileMoneyTransaction tx : transactions) {
            try {
                MobileMoneyResult result = mobileMoneyService.reverse(tx.provider(), tx.transactionNo(), orderNo);
                if (!result.isSuccess()) {
                    log.error("Mobile Money 补偿撤销失败，订单号：{}，运营商：{}，流水号：{}",
                            orderNo, tx.provider(), tx.transactionNo());
                } else {
                    log.info("Mobile Money 补偿撤销成功，订单号：{}，运营商：{}，流水号：{}",
                            orderNo, tx.provider(), tx.transactionNo());
                }
            } catch (Exception e) {
                log.error("Mobile Money 补偿撤销异常，订单号：{}，运营商：{}，流水号：{}",
                        orderNo, tx.provider(), tx.transactionNo(), e);
            }
        }
    }

    private record MobileMoneyTransaction(String provider, String transactionNo) {
    }

    private boolean isMobileMoneyPay(CashierPaySaveDTO pay) {
        return pay.getMobileProvider() != null &&
                ("AIRTEL".equalsIgnoreCase(pay.getMobileProvider())
                        || "MTN".equalsIgnoreCase(pay.getMobileProvider())
                        || "ZAMTEL".equalsIgnoreCase(pay.getMobileProvider()));
    }

    /**
     * 构建支付明细实体列表（支持多种支付方式）
     *
     * @param pays    支付明细数据传输对象列表
     * @param orderNo 订单号
     * @return 支付明细实体列表
     */
    private List<OrderPayEntity> buildOrderPays(List<CashierPaySaveDTO> pays, String orderNo, String shopCode, String posId) {
        if (pays == null || pays.isEmpty()) {
            return new ArrayList<>();
        }

        String currentTime = LocalDateTime.now().format(PAY_TIME_FORMATTER);
        String tenantId = UserContext.getTenantId();
        
        return pays.stream().map(pay -> {
            OrderPayEntity entity = new OrderPayEntity();
            entity.setDataUuid(IdUtils.snowflakeIdStr());
            entity.setTenantId(tenantId);
            entity.setShopCode(shopCode);
            entity.setPosId(posId);
            entity.setOrderNo(orderNo);
            entity.setBizType(BIZ_TYPE_RETAIL); // 零售收款
            
            if (pay.getPayId() != null) {
                entity.setPayId(pay.getPayId());
            }

            entity.setPayType(isMobileMoneyPay(pay) ? PAY_TYPE_MOBILE_MONEY : 0);
            entity.setPayAmount(pay.getPayAmount());
            entity.setCurrency(pay.getCurrency());
            entity.setPayTime(currentTime);
            entity.setMobileProvider(pay.getMobileProvider());
            entity.setMobilePhone(pay.getMobilePhone());
            entity.setMobileTransactionNo(pay.getMobileTransactionNo());

            return entity;
        }).toList();
    }

    /**
     * 安全解析字符串为 Integer
     * 如果字符串为 null、空或无法解析为整数，则返回 null
     *
     * @param value 待解析的字符串
     * @return 解析后的 Integer，解析失败返回 null
     */
    private Integer parseIntegerSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("无法解析字符串为整数: {}", value);
            return null;
        }
    }
}