package com.trademaster.workflow.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

@Slf4j
public class ElExpressionUtil {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    private ElExpressionUtil() {
    }

    public static boolean evaluateBoolean(String expressionStr, Map<String, Object> variables) {
        try {
            Expression expression = PARSER.parseExpression(expressionStr);
            EvaluationContext context = createContext(variables);
            Boolean result = expression.getValue(context, Boolean.class);
            log.info("EL evaluate: expr='{}', vars={}, result={}", expressionStr, variables, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("EL evaluate failed: expr='{}', vars={}, error={}", expressionStr, variables, e.getMessage());
            return false;
        }
    }

    public static <T> T evaluate(String expressionStr, Map<String, Object> variables, Class<T> resultType) {
        try {
            Expression expression = PARSER.parseExpression(expressionStr);
            EvaluationContext context = createContext(variables);
            return expression.getValue(context, resultType);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object evaluate(String expressionStr, Map<String, Object> variables) {
        try {
            Expression expression = PARSER.parseExpression(expressionStr);
            EvaluationContext context = createContext(variables);
            return expression.getValue(context);
        } catch (Exception e) {
            return null;
        }
    }

    private static EvaluationContext createContext(Map<String, Object> variables) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        if (variables != null && !variables.isEmpty()) {
            context.addPropertyAccessor(new MapPropertyAccessor());
            variables.forEach(context::setVariable);
            context.setRootObject(variables);
        }
        return context;
    }

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

    public static boolean isValid(String expressionStr) {
        try {
            PARSER.parseExpression(expressionStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

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

        if (value instanceof String) {
            sb.append("'").append(value).append("'");
        } else {
            sb.append(value);
        }

        return sb.toString();
    }
}
