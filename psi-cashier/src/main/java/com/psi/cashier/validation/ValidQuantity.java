package com.psi.cashier.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 数量校验注解
 * 支持前端手动输入数量的校验
 * 
 * @author PSI
 * @version 1.0.0
 */
@Documented
@Constraint(validatedBy = QuantityValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidQuantity {

    String message() default "数量格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 最小数量（默认0.01）
     */
    double min() default 0.01;

    /**
     * 最大数量（默认9999999999.9999）
     */
    double max() default 9999999999.9999;

    /**
     * 最大小数位数（默认4）
     */
    int maxDecimals() default 4;
}