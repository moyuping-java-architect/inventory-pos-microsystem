package com.psi.cashier.controller;

import com.psi.cashier.service.ExchangeRateService;
import com.psi.common.result.CommonResult;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 汇率控制器
 * 提供当前有效汇率查询接口
 *
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/psi/cashier/exchange-rate")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    /**
     * 查询当前有效汇率列表（非本位币对 ZMW）
     */
    @GetMapping("/current")
    public CommonResult<List<ExchangeRateVO>> currentRates() {
        List<ExchangeRateVO> rates = new ArrayList<>();
        rates.add(buildRate("USD", "ZMW"));
        return CommonResult.success(rates);
    }

    private ExchangeRateVO buildRate(String from, String to) {
        ExchangeRateVO vo = new ExchangeRateVO();
        vo.setFromCurrency(from);
        vo.setToCurrency(to);
        vo.setRate(exchangeRateService.getEffectiveRate(from, to));
        return vo;
    }

    @Data
    public static class ExchangeRateVO {
        private String fromCurrency;
        private String toCurrency;
        private BigDecimal rate;
    }
}
