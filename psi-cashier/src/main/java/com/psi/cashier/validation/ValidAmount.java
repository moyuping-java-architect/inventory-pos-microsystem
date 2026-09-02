package com.psi.cashier.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 金额校验注解
 * 支持配置最小值、最大值和小数位数
 * 
 * @author PSI
 * @version 1.0.0
 */
@Documented
@Constraint(validatedBy = AmountValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAmount {
    
    String message() default "金额格式不正确";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 最小金额（默认0.01）
     */
    double min() default 0.01;
    
    /**
     * 最大金额（默认9999999999.99）
     */
    double max() default 9999999999.99;
    
    /**
     * 最大小数位数（默认2位）
     */
    int maxDecimals() default 2;
}