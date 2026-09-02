package com.psi.customer.dto;

import lombok.Data;

/**
 * 规则条件项。
 * <p>
 * 表达能力刻意限制为「字段 + 运算符 + 值」，多个条件之间只支持 AND，
 * 不引入表达式引擎。理由：这套东西最终给不懂技术的店主用，
 * 配得明白比配得强大重要；真遇到复杂逻辑，加一个指标列比让用户学语法便宜。
 *
 * @author PSI
 */
@Data
public class RuleCondition {

    /** 字段名：customer_metrics 列的驼峰名，或事件 payload 字段名 */
    private String field;

    /** 运算符：GE / GT / LE / LT / EQ / NE / IN / CONTAINS */
    private String op;

    /** 比较值。IN 运算符时用英文逗号分隔多个值 */
    private String value;
}
