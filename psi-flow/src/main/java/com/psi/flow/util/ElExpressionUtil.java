package com.psi.flow.util;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * EL表达式工具类
 * 用于解析和执行流程条件中的EL表达式
 */
@Slf4j
public class ElExpressionUtil {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    private ElExpressionUtil() {
        // 私有构造函数，防止实例化
    }

    /**
     * 解析并执行EL表达式，返回布尔结果
     *
     * @param expressionStr EL表达式字符串
     * @param variables     变量键值对
     * @return 表达式执行结果
     */
    public static boolean evaluateBoolean(String expressionStr, Map<String, Object> variables) {
        try {
            Expression expression = PARSER.parseExpression(expressionStr);
            EvaluationContext context = createContext(variables);
            Boolean result = expression.getValue(context, Boolean.class);
            log.info("=== EL evaluate: expr='{}', vars={}, result={}", expressionStr, variables, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("=== EL evaluate failed: expr='{}', vars={}, error={}", expressionStr, variables, e.getMessage());
            return false;
        }
    }

    /**
     * 解析并执行EL表达式，返回指定类型的结果
     *
     * @param expressionStr EL表达式字符串
     * @param variables     变量键值对
     * @param resultType    返回类型
     * @param <T>           返回类型泛型
     * @return 表达式执行结果
     */
    public static <T> T evaluate(String expressionStr, Map<String, Object> variables, Class<T> resultType) {
        try {
            Expression expression = PARSER.parseExpression(expressionStr);
            EvaluationContext context = createContext(variables);
            return expression.getValue(context, resultType);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析并执行EL表达式，返回Object类型结果
     *
     * @param expressionStr EL表达式字符串
     * @param variables     变量键值对
     * @return 表达式执行结果
     */
    public static Object evaluate(String expressionStr, Map<String, Object> variables) {
        try {
            Expression expression = PARSER.parseExpression(expressionStr);
            EvaluationContext context = createContext(variables);
            return expression.getValue(context);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 创建评估上下文
     * 同时支持 #amount 和 amount 两种语法
     * 添加 MapPropertyAccessor 允许直接访问 Map key 作为 property
     */
    private static EvaluationContext createContext(Map<String, Object> variables) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        if (variables != null && !variables.isEmpty()) {
            // 添加 MapPropertyAccessor 允许直接从 Map 中按 key 获取值（如 amount > 10000）
            context.addPropertyAccessor(new MapPropertyAccessor());
            // 使用 setVariable 设置 #variableName 形式的变量（如 #amount > 10000）
            variables.forEach(context::setVariable);
            // 使用 setRootObject 让变量可以直接通过 property 访问（如 amount > 10000）
            context.setRootObject(variables);
        }
        return context;
    }

    /**
     * Map 属性访问器 - 允许 SpEL 直接访问 Map 的 key 作为属性
     */
    private static class MapPropertyAccessor implements PropertyAccessor {
        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return new Class[]{Map.class};
        }

        @Override
        public boolean canRead(EvaluationContext context, Object target, String name) {
            return target instanceof Map && ((Map<?, ?>) target).containsKey(name);
        }

        @Override
        public TypedValue read(EvaluationContext context, Object target, String name) {
            Map<?, ?> map = (Map<?, ?>) target;
            return new TypedValue(map.get(name));
        }

        @Override
        public boolean canWrite(EvaluationContext context, Object target, String name) {
            return false;
        }

        @Override
        public void write(EvaluationContext context, Object target, String name, Object newValue) {
        }
    }

    /**
     * 验证EL表达式语法是否正确
     *
     * @param expressionStr EL表达式字符串
     * @return 是否有效
     */
    public static boolean isValid(String expressionStr) {
        try {
            PARSER.parseExpression(expressionStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 构建比较表达式
     *
     * @param fieldName   字段名
     * @param compareType 比较类型（> < >= <= == !=）
     * @param value       比较值
     * @return EL表达式字符串
     */
    public static String buildCompareExpression(String fieldName, String compareType, Object value) {
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(fieldName);

        switch (compareType) {
            case ">":
                sb.append(" > ");
                break;
            case "<":
                sb.append(" < ");
                break;
            case ">=":
                sb.append(" >= ");
                break;
            case "<=":
                sb.append(" <= ");
                break;
            case "==":
            case "=":
                sb.append(" == ");
                break;
            case "!=":
                sb.append(" != ");
                break;
            default:
                sb.append(" == ");
        }

        // 根据值类型添加引号
        if (value instanceof String) {
            sb.append("'").append(value).append("'");
        } else {
            sb.append(value);
        }

        return sb.toString();
    }
}