package com.psi.cashier.controller;

import com.psi.common.result.CommonResult;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/psi/cashier/i18n")
public class I18nController {

    @Resource
    private MessageSource messageSource;

    private static final String[] KEY_PREFIXES = {
        "common.", "button.", "label.", "hint.", "modal.title.",
        "table.header.", "message.", "status.", "pay.", "lang."
    };

    @GetMapping("/messages")
    public CommonResult<Map<String, String>> getMessages(@RequestParam(defaultValue = "zh_CN") String lang) {
        Locale locale = parseLocale(lang);
        Map<String, String> messages = new ConcurrentHashMap<>();

        for (String prefix : KEY_PREFIXES) {
            loadMessagesByPrefix(prefix, locale, messages);
        }

        return CommonResult.success(messages);
    }

    private Locale parseLocale(String lang) {
        if (lang.contains("_")) {
            String[] parts = lang.split("_");
            return new Locale(parts[0], parts[1]);
        }
        return new Locale(lang);
    }

    private void loadMessagesByPrefix(String prefix, Locale locale, Map<String, String> messages) {
        String[] keys = getKeysByPrefix(prefix);
        for (String key : keys) {
            try {
                String value = messageSource.getMessage(key, null, key, locale);
                messages.put(key, value);
            } catch (Exception e) {
                messages.put(key, key);
            }
        }
    }

    private String[] getKeysByPrefix(String prefix) {
        return switch (prefix) {
            case "common." -> new String[]{
                "common.title", "common.cashier", "common.total", "common.amount"
            };
            case "button." -> new String[]{
                "button.upload", "button.download", "button.cashierShift", "button.submit",
                "button.cancel", "button.add", "button.delete", "button.clear",
                "button.search", "button.print", "button.register", "button.confirm",
                "button.close", "button.queryTrade", "button.dailySettlement",
                "button.refund", "button.suspend", "button.resume", "button.saveDraft",
                "button.pay"
            };
            case "label." -> new String[]{
                "label.unit", "label.action", "label.detail", "label.reason",
                "label.phone", "label.name", "label.time"
            };
            case "hint." -> new String[]{
                "hint.enterOrderNo", "hint.emptyData", "hint.emptyProducts",
                "hint.emptyPayRecords", "hint.emptyRefundDetails", "hint.emptyOrders",
                "hint.enterPhone", "hint.enterName", "hint.enterAmount",
                "hint.sourceOrderAmount", "hint.refundedAmount", "hint.availableRefund",
                "hint.refundProducts", "hint.purchaseQty", "hint.refundedQty",
                "hint.refundQty", "hint.enterSourceOrder", "hint.enterRefundReason",
                "hint.refundMethod", "hint.totalRefundAmount", "hint.refundTotal",
                "hint.orderCount", "hint.orderTotal", "hint.realAmount",
                "hint.refundAmount", "hint.payMethodSummary", "hint.todayOrders",
                "hint.count"
            };
            case "modal.title." -> new String[]{
                "modal.title.tradeQuery", "modal.title.orderDetail",
                "modal.title.refund", "modal.title.settlement",
                "modal.title.memberRegister"
            };
            case "table.header." -> new String[]{
                "table.header.product", "table.header.price", "table.header.quantity",
                "table.header.amount", "table.header.orderNo", "table.header.amountReceived",
                "table.header.status", "table.header.date", "table.header.method"
            };
            case "message." -> new String[]{
                "message.paySuccess"
            };
            case "status." -> new String[]{
                "status.paid", "status.unpaid", "status.refunded", "status.partialRefund"
            };
            case "pay." -> new String[]{
                "pay.cash", "pay.wechat", "pay.alipay", "pay.card"
            };
            case "lang." -> new String[]{
                "lang.chinese", "lang.english"
            };
            default -> new String[]{};
        };
    }
}