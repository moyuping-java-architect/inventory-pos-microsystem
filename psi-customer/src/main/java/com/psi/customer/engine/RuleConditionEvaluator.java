package com.psi.customer.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.customer.dto.RuleCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 规则条件求值器。
 * <p>
 * 把规则表里的 conditionJson 与「指标 + 事件载荷」合并后的上下文做比对。
 * 数值型字段自动按数字比较，非数值按字符串比较，避免出现 "100" &lt; "20" 这种坑。
 * <p>
 * 所有条件之间为 AND 关系；条件为空视为无条件命中。
 *
 * @author PSI
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RuleConditionEvaluator {

    private final ObjectMapper objectMapper;

    /**
     * 解析条件 JSON。解析失败返回空列表（即无条件命中），
     * 不抛异常中断整条事件链路。
     *
     * @param conditionJson 条件 JSON 数组字符串
     * @return 条件列表
     */
    public List<RuleCondition> parse(String conditionJson) {
        if (conditionJson == null || conditionJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(conditionJson, new TypeReference<List<RuleCondition>>() {});
        } catch (Exception e) {
            log.warn("规则条件解析失败, 视为无条件: json={}, error={}", conditionJson, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 判断上下文是否满足全部条件。
     *
     * @param conditions 条件列表，空表示无条件命中
     * @param context    求值上下文（指标 + 事件载荷）
     * @return 全部满足返回 true
     */
    public boolean matches(List<RuleCondition> conditions, Map<String, Object> context) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        for (RuleCondition condition : conditions) {
            if (!matchOne(condition, context)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 单条件求值。字段在上下文中不存在时返回 false（条件视为不满足）。
     */
    private boolean matchOne(RuleCondition condition, Map<String, Object> context) {
        if (condition == null || condition.getField() == null) {
            return true;
        }

        Object actual = context.get(condition.getField());
        if (actual == null) {
            log.debug("规则条件字段在上下文中缺失: field={}", condition.getField());
            return false;
        }

        String op = condition.getOp() == null ? "EQ" : condition.getOp().toUpperCase();
        String expected = condition.getValue();

        // IN / CONTAINS 走字符串语义
        if ("IN".equals(op)) {
            if (expected == null) {
                return false;
            }
            String actualStr = String.valueOf(actual).trim();
            return Arrays.stream(expected.split(","))
                    .map(String::trim)
                    .anyMatch(v -> v.equalsIgnoreCase(actualStr));
        }
        if ("CONTAINS".equals(op)) {
            return expected != null && String.valueOf(actual).contains(expected);
        }

        // 优先尝试数值比较
        BigDecimal actualNum = toNumber(actual);
        BigDecimal expectedNum = toNumber(expected);
        if (actualNum != null && expectedNum != null) {
            int cmp = actualNum.compareTo(expectedNum);
            return compare(op, cmp);
        }

        // 退化为字符串比较
        int cmp = String.valueOf(actual).compareToIgnoreCase(String.valueOf(expected));
        return compare(op, cmp);
    }

    /**
     * 按运算符判定比较结果。
     */
    private boolean compare(String op, int cmp) {
        return switch (op) {
            case "GE" -> cmp >= 0;
            case "GT" -> cmp > 0;
            case "LE" -> cmp <= 0;
            case "LT" -> cmp < 0;
            case "NE" -> cmp != 0;
            default -> cmp == 0;
        };
    }

    /**
     * 尝试转数字，失败返回 null。
     */
    private BigDecimal toNumber(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number num) {
            return new BigDecimal(num.toString());
        }
        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
