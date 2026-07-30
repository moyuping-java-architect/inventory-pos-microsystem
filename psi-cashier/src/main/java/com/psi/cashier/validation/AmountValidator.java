package com.psi.cashier.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

/**
 * 金额校验器实现
 * 支持非空、非负、范围、小数位数和格式校验
 * 
 * @author PSI
 * @version 1.0.0
 */
public class AmountValidator implements ConstraintValidator<ValidAmount, BigDecimal> {

    private double min;
    private double max;
    private int maxDecimals;

    @Override
    public void initialize(ValidAmount constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
        this.maxDecimals = constraintAnnotation.maxDecimals();
    }

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        // 禁用默认提示
        context.disableDefaultConstraintViolation();

        // 检查非空
        if (value == null) {
            context.buildConstraintViolationWithTemplate("金额不能为空").addConstraintViolation();
            return false;
        }

        // 检查是否为负数
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            context.buildConstraintViolationWithTemplate("金额不能为负数").addConstraintViolation();
            return false;
        }

        // 检查最小值
        BigDecimal minValue = BigDecimal.valueOf(min);
        if (value.compareTo(minValue) < 0) {
            context.buildConstraintViolationWithTemplate("金额必须大于" + min).addConstraintViolation();
            return false;
        }

        // 检查最大值
        BigDecimal maxValue = BigDecimal.valueOf(max);
        if (value.compareTo(maxValue) > 0) {
            context.buildConstraintViolationWithTemplate("金额不能超过" + max).addConstraintViolation();
            return false;
        }

        // 检查小数位数
        String valueStr = value.stripTrailingZeros().toPlainString();
        if (valueStr.contains(".")) {
            int decimalPlaces = valueStr.length() - valueStr.indexOf(".") - 1;
            if (decimalPlaces > maxDecimals) {
                context.buildConstraintViolationWithTemplate("金额小数位不能超过" + maxDecimals + "位").addConstraintViolation();
                return false;
            }
        }

        // 检查格式（避免科学计数法等非法格式）
        Pattern pattern = Pattern.compile("^\\d+(\\.\\d{1," + maxDecimals + "})?$");
        if (!pattern.matcher(valueStr).matches()) {
            context.buildConstraintViolationWithTemplate("金额格式不正确").addConstraintViolation();
            return false;
        }

        return true;
    }
}