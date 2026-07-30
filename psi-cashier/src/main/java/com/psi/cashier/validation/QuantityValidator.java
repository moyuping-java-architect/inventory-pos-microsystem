package com.psi.cashier.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

/**
 * 数量校验器实现
 * 支持前端手动输入的数量校验，提供友好的错误提示
 * 
 * @author PSI
 * @version 1.0.0
 */
public class QuantityValidator implements ConstraintValidator<ValidQuantity, BigDecimal> {

    private double min;
    private double max;
    private int maxDecimals;

    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");

    @Override
    public void initialize(ValidQuantity constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
        this.maxDecimals = constraintAnnotation.maxDecimals();
    }

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        // 禁用默认消息
        context.disableDefaultConstraintViolation();

        if (value == null) {
            context.buildConstraintViolationWithTemplate("数量不能为空").addConstraintViolation();
            return false;
        }

        // 检查是否为负数
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            context.buildConstraintViolationWithTemplate("数量不能为负数").addConstraintViolation();
            return false;
        }

        // 检查最小值
        if (value.compareTo(BigDecimal.valueOf(min)) < 0) {
            context.buildConstraintViolationWithTemplate("数量必须大于" + min).addConstraintViolation();
            return false;
        }

        // 检查最大值
        if (value.compareTo(BigDecimal.valueOf(max)) > 0) {
            context.buildConstraintViolationWithTemplate("数量不能超过" + formatMaxValue()).addConstraintViolation();
            return false;
        }

        // 检查小数位数
        String valueStr = value.stripTrailingZeros().toPlainString();
        if (valueStr.contains(".")) {
            int decimalPlaces = valueStr.length() - valueStr.indexOf(".") - 1;
            if (decimalPlaces > maxDecimals) {
                context.buildConstraintViolationWithTemplate("数量小数位不能超过" + maxDecimals + "位").addConstraintViolation();
                return false;
            }
        }

        // 检查格式（确保是合法的数字格式）
        if (!DECIMAL_PATTERN.matcher(valueStr).matches()) {
            context.buildConstraintViolationWithTemplate("数量格式不正确，请输入有效的数字").addConstraintViolation();
            return false;
        }

        return true;
    }

    private String formatMaxValue() {
        BigDecimal maxValue = BigDecimal.valueOf(max);
        if (maxValue.scale() > 0) {
            return maxValue.stripTrailingZeros().toPlainString();
        }
        return String.valueOf(max);
    }
}