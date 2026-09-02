
package com.psi.cashier.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/locale")
public class LocaleController {

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/messages")
    public Map<String, String> getMessages(@RequestParam(required = false) String lang) {
        Locale locale = LocaleContextHolder.getLocale();
        if (lang != null && !lang.isEmpty()) {
            locale = new Locale(lang);
        }

        Map<String, String> messages = new HashMap<>();
        
        // 通用
        messages.put("common.title", getMessage("common.title", locale));
        messages.put("common.cashier", getMessage("common.cashier", locale));
        
        // 按钮
        messages.put("button.upload", getMessage("button.upload", locale));
        messages.put("button.download", getMessage("button.download", locale));
        messages.put("button.cashierShift", getMessage("button.cashierShift", locale));
        messages.put("button.submit", getMessage("button.submit", locale));
        messages.put("button.cancel", getMessage("button.cancel", locale));
        messages.put("button.add", getMessage("button.add", locale));
        messages.put("button.delete", getMessage("button.delete", locale));
        messages.put("button.clear", getMessage("button.clear", locale));
        messages.put("button.search", getMessage("button.search", locale));
        messages.put("button.print", getMessage("button.print", locale));
        
        // 标签
        messages.put("label.product", getMessage("label.product", locale));
        messages.put("label.price", getMessage("label.price", locale));
        messages.put("label.quantity", getMessage("label.quantity", locale));
        messages.put("label.amount", getMessage("label.amount", locale));
        messages.put("label.total", getMessage("label.total", locale));
        messages.put("label.payment", getMessage("label.payment", locale));
        messages.put("label.member", getMessage("label.member", locale));
        messages.put("label.order", getMessage("label.order", locale));
        messages.put("label.refund", getMessage("label.refund", locale));
        messages.put("label.reason", getMessage("label.reason", locale));
        
        // 提示
        messages.put("hint.searchProduct", getMessage("hint.searchProduct", locale));
        messages.put("hint.enterAmount", getMessage("hint.enterAmount", locale));
        messages.put("hint.enterMember", getMessage("hint.enterMember", locale));
        messages.put("hint.enterOrderNo", getMessage("hint.enterOrderNo", locale));
        messages.put("hint.enterRefundReason", getMessage("hint.enterRefundReason", locale));
        
        return messages;
    }

    private String getMessage(String key, Locale locale) {
        try {
            return messageSource.getMessage(key, null, locale);
        } catch (Exception e) {
            return key;
        }
    }
}