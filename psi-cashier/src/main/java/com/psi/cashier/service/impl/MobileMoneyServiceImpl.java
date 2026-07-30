package com.psi.cashier.service.impl;

import com.psi.cashier.dto.MobileMoneyResult;
import com.psi.cashier.service.MobileMoneyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

/**
 * Mobile Money 收款服务实现（模拟）
 * <p>
 * 当前为模拟实现，生成随机交易流水号，不调用真实运营商 API。
 * 后续替换真实 API 时，只需修改 callProviderApi 方法即可。
 */
@Slf4j
@Service
public class MobileMoneyServiceImpl implements MobileMoneyService {

    private static final Set<String> VALID_PROVIDERS = Set.of("AIRTEL", "MTN", "ZAMTEL");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public MobileMoneyResult collect(String provider, String phone, BigDecimal amount, String orderNo) {
        if (provider == null || !VALID_PROVIDERS.contains(provider.toUpperCase(Locale.ROOT))) {
            return MobileMoneyResult.fail(provider, phone, amount, "不支持的 Mobile Money 运营商");
        }
        if (phone == null || phone.trim().isEmpty()) {
            return MobileMoneyResult.fail(provider, phone, amount, "手机号不能为空");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return MobileMoneyResult.fail(provider, phone, amount, "收款金额必须大于 0");
        }

        String normalizedProvider = provider.toUpperCase(Locale.ROOT);
        String transactionNo = generateTransactionNo(normalizedProvider);

        log.info("[MOCK] Mobile Money 收款请求，运营商：{}，手机号：{}，金额：{}，订单号：{}，流水号：{}",
                normalizedProvider, phone, amount, orderNo, transactionNo);

        // 模拟网络延迟
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 模拟调用运营商 API（当前直接返回成功）
        boolean mockSuccess = callProviderApi(normalizedProvider, phone, amount, orderNo, transactionNo);
        if (mockSuccess) {
            log.info("[MOCK] Mobile Money 收款成功，运营商：{}，流水号：{}", normalizedProvider, transactionNo);
            return MobileMoneyResult.success(normalizedProvider, phone, amount, transactionNo);
        }
        return MobileMoneyResult.fail(normalizedProvider, phone, amount, "Mobile Money 收款失败（模拟）");
    }

    @Override
    public MobileMoneyResult reverse(String provider, String transactionNo, String orderNo) {
        if (provider == null || !VALID_PROVIDERS.contains(provider.toUpperCase(Locale.ROOT))) {
            return MobileMoneyResult.fail(provider, null, null, "不支持的 Mobile Money 运营商");
        }
        if (transactionNo == null || transactionNo.trim().isEmpty()) {
            return MobileMoneyResult.fail(provider, null, null, "交易流水号不能为空");
        }
        String normalizedProvider = provider.toUpperCase(Locale.ROOT);
        log.info("[MOCK] Mobile Money 撤销请求，运营商：{}，原流水号：{}，订单号：{}",
                normalizedProvider, transactionNo, orderNo);
        // TODO: 接入 Airtel/MTN/Zamtel 真实退款/撤销 API
        return MobileMoneyResult.success(normalizedProvider, null, null, transactionNo);
    }

    /**
     * 模拟调用运营商 API，预留真实 API 接入点
     */
    private boolean callProviderApi(String provider, String phone, BigDecimal amount,
                                    String orderNo, String transactionNo) {
        // TODO: 接入 Airtel/MTN/Zamtel 真实收款 API
        return true;
    }

    private String generateTransactionNo(String provider) {
        return provider + LocalDateTime.now().format(TIME_FORMATTER) + String.format("%04d", (int) (Math.random() * 10000));
    }
}
