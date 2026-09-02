package com.psi.cashier.service.impl;

import com.psi.cashier.entity.ExchangeRateEntity;
import com.psi.cashier.mapper.ExchangeRateMapper;
import com.psi.cashier.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 汇率服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateMapper exchangeRateMapper;

    private static final BigDecimal DEFAULT_USD_TO_ZMW = new BigDecimal("27.0");
    private static final BigDecimal DEFAULT_ZMW_TO_USD = new BigDecimal("0.037");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public BigDecimal getEffectiveRate(String from, String to) {
        if (from == null || to == null) {
            return BigDecimal.ONE;
        }
        if (from.equalsIgnoreCase(to)) {
            return BigDecimal.ONE;
        }

        ExchangeRateEntity rate = exchangeRateMapper.selectLatest(from.toUpperCase(), to.toUpperCase());
        if (rate != null && rate.getRate() != null) {
            return rate.getRate();
        }

        log.warn("未找到 {} 到 {} 的有效汇率，使用默认汇率", from, to);
        if ("USD".equalsIgnoreCase(from) && "ZMW".equalsIgnoreCase(to)) {
            return DEFAULT_USD_TO_ZMW;
        }
        if ("ZMW".equalsIgnoreCase(from) && "USD".equalsIgnoreCase(to)) {
            return DEFAULT_ZMW_TO_USD;
        }
        return BigDecimal.ONE;
    }

    @Override
    public void upsertRate(ExchangeRateEntity rate) {
        if (rate.getEffectiveDate() == null) {
            rate.setEffectiveDate(LocalDate.now().format(DATE_FORMATTER));
        }
        if (rate.getFromCurrency() != null) {
            rate.setFromCurrency(rate.getFromCurrency().toUpperCase());
        }
        if (rate.getToCurrency() != null) {
            rate.setToCurrency(rate.getToCurrency().toUpperCase());
        }
        ExchangeRateEntity existing = exchangeRateMapper.selectLatest(rate.getFromCurrency(), rate.getToCurrency());
        if (existing != null && existing.getEffectiveDate().equals(rate.getEffectiveDate())) {
            rate.setId(existing.getId());
            exchangeRateMapper.updateById(rate);
        } else {
            exchangeRateMapper.insert(rate);
        }
    }
}
