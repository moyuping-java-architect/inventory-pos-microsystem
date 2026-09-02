package com.psi.common.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.util.Assert;

import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.function.Supplier;

/**
 * Bean 工具类
 * 基于 Spring BeanUtils 实现，提供对象属性复制、转换等功能
 * 
 * @author PSI
 * @version 1.0.0
 */
public final class BeanUtils {

    private BeanUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    // ==================== 基础属性复制方法 ====================

    /**
     * 将源对象的属性复制到目标对象
     * 
     * @param source 源对象
     * @param target 目标对象
     * @throws BeansException 如果复制过程中发生错误
     */
    public static void copyProperties(Object source, Object target) throws BeansException {
        org.springframework.beans.BeanUtils.copyProperties(source, target);
    }

    /**
     * 将源对象的属性复制到目标对象，忽略指定属性
     * 
     * @param source           源对象
     * @param target           目标对象
     * @param ignoreProperties 要忽略的属性名称数组
     * @throws BeansException 如果复制过程中发生错误
     */
    public static void copyProperties(Object source, Object target, String... ignoreProperties) throws BeansException {
        org.springframework.beans.BeanUtils.copyProperties(source, target, ignoreProperties);
    }

    /**
     * 将源对象的属性复制到目标对象，仅复制指定属性
     * 
     * @param source         源对象
     * @param target         目标对象
     * @param includeProperties 要复制的属性名称数组
     * @throws BeansException 如果复制过程中发生错误
     */
    public static void copyPropertiesInclude(Object source, Object target, String... includeProperties) throws BeansException {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");
        
        if (includeProperties == null || includeProperties.length == 0) {
            copyProperties(source, target);
            return;
        }
        
        Set<String> includeSet = new HashSet<>(Arrays.asList(includeProperties));
        BeanWrapper sourceWrapper = new BeanWrapperImpl(source);
        BeanWrapper targetWrapper = new BeanWrapperImpl(target);
        
        for (String propertyName : includeSet) {
            PropertyDescriptor sourceDescriptor = sourceWrapper.getPropertyDescriptor(propertyName);
            PropertyDescriptor targetDescriptor = targetWrapper.getPropertyDescriptor(propertyName);
            
            if (sourceDescriptor != null && targetDescriptor != null) {
                try {
                    Object value = sourceDescriptor.getReadMethod().invoke(source);
                    targetDescriptor.getWriteMethod().invoke(target, value);
                } catch (Exception e) {
                    throw new BeansException("Failed to copy property '" + propertyName + "'", e) {};
                }
            }
        }
    }

    // ==================== 对象转换方法 ====================

    /**
     * 将源对象转换为指定类型的目标对象
     * 
     * @param source 源对象
     * @param targetClass 目标类型
     * @param <T> 目标类型泛型
     * @return 转换后的目标对象
     * @throws BeansException 如果转换过程中发生错误
     */
    public static <T> T convert(Object source, Class<T> targetClass) throws BeansException {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(targetClass, "Target class must not be null");
        
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new BeansException("Failed to convert object to " + targetClass.getName(), e) {};
        }
    }

    /**
     * 将源对象转换为指定类型的目标对象，忽略指定属性
     * 
     * @param source           源对象
     * @param targetClass      目标类型
     * @param ignoreProperties 要忽略的属性名称数组
     * @param <T> 目标类型泛型
     * @return 转换后的目标对象
     * @throws BeansException 如果转换过程中发生错误
     */
    public static <T> T convert(Object source, Class<T> targetClass, String... ignoreProperties) throws BeansException {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(targetClass, "Target class must not be null");
        
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            copyProperties(source, target, ignoreProperties);
            return target;
        } catch (Exception e) {
            throw new BeansException("Failed to convert object to " + targetClass.getName(), e) {};
        }
    }

    /**
     * 使用供应商创建目标对象并进行属性复制
     * 
     * @param source 源对象
     * @param targetSupplier 目标对象供应商
     * @param <T> 目标类型泛型
     * @return 转换后的目标对象
     * @throws BeansException 如果转换过程中发生错误
     */
    public static <T> T convert(Object source, Supplier<T> targetSupplier) throws BeansException {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(targetSupplier, "Target supplier must not be null");
        
        T target = targetSupplier.get();
        copyProperties(source, target);
        return target;
    }

    // ==================== 集合转换方法 ====================

    /**
     * 将源对象集合转换为目标类型集合
     * 
     * @param sourceList 源对象集合
     * @param targetClass 目标类型
     * @param <S> 源类型泛型
     * @param <T> 目标类型泛型
     * @return 转换后的目标对象集合
     * @throws BeansException 如果转换过程中发生错误
     */
    public static <S, T> List<T> convertList(Collection<S> sourceList, Class<T> targetClass) throws BeansException {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        
        Assert.notNull(targetClass, "Target class must not be null");
        
        List<T> targetList = new ArrayList<>(sourceList.size());
        for (S source : sourceList) {
            if (source != null) {
                targetList.add(convert(source, targetClass));
            } else {
                targetList.add(null);
            }
        }
        return targetList;
    }

    /**
     * 将源对象集合转换为目标类型集合，忽略指定属性
     * 
     * @param sourceList       源对象集合
     * @param targetClass      目标类型
     * @param ignoreProperties 要忽略的属性名称数组
     * @param <S> 源类型泛型
     * @param <T> 目标类型泛型
     * @return 转换后的目标对象集合
     * @throws BeansException 如果转换过程中发生错误
     */
    public static <S, T> List<T> convertList(Collection<S> sourceList, Class<T> targetClass, String... ignoreProperties) throws BeansException {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        
        Assert.notNull(targetClass, "Target class must not be null");
        
        List<T> targetList = new ArrayList<>(sourceList.size());
        for (S source : sourceList) {
            if (source != null) {
                targetList.add(convert(source, targetClass, ignoreProperties));
            } else {
                targetList.add(null);
            }
        }
        return targetList;
    }

    /**
     * 将源对象数组转换为目标类型集合
     * 
     * @param sourceArray 源对象数组
     * @param targetClass 目标类型
     * @param <S> 源类型泛型
     * @param <T> 目标类型泛型
     * @return 转换后的目标对象集合
     * @throws BeansException 如果转换过程中发生错误
     */
    public static <S, T> List<T> convertArray(S[] sourceArray, Class<T> targetClass) throws BeansException {
        if (sourceArray == null || sourceArray.length == 0) {
            return new ArrayList<>();
        }
        
        return convertList(Arrays.asList(sourceArray), targetClass);
    }

    // ==================== 属性操作方法 ====================

    /**
     * 获取对象的所有属性名称
     * 
     * @param obj 目标对象
     * @return 属性名称集合
     */
    public static Set<String> getPropertyNames(Object obj) {
        Assert.notNull(obj, "Object must not be null");
        
        BeanWrapper wrapper = new BeanWrapperImpl(obj);
        PropertyDescriptor[] descriptors = wrapper.getPropertyDescriptors();
        
        Set<String> propertyNames = new HashSet<>();
        for (PropertyDescriptor descriptor : descriptors) {
            if (!"class".equals(descriptor.getName())) {
                propertyNames.add(descriptor.getName());
            }
        }
        return propertyNames;
    }

    /**
     * 获取对象的属性值
     * 
     * @param obj 目标对象
     * @param propertyName 属性名称
     * @return 属性值
     * @throws BeansException 如果获取属性值失败
     */
    public static Object getProperty(Object obj, String propertyName) throws BeansException {
        Assert.notNull(obj, "Object must not be null");
        Assert.notNull(propertyName, "Property name must not be null");
        
        BeanWrapper wrapper = new BeanWrapperImpl(obj);
        return wrapper.getPropertyValue(propertyName);
    }

    /**
     * 设置对象的属性值
     * 
     * @param obj 目标对象
     * @param propertyName 属性名称
     * @param value 属性值
     * @throws BeansException 如果设置属性值失败
     */
    public static void setProperty(Object obj, String propertyName, Object value) throws BeansException {
        Assert.notNull(obj, "Object must not be null");
        Assert.notNull(propertyName, "Property name must not be null");
        
        BeanWrapper wrapper = new BeanWrapperImpl(obj);
        wrapper.setPropertyValue(propertyName, value);
    }

    /**
     * 判断对象是否具有指定属性
     * 
     * @param obj 目标对象
     * @param propertyName 属性名称
     * @return true-存在该属性，false-不存在
     */
    public static boolean hasProperty(Object obj, String propertyName) {
        if (obj == null || propertyName == null) {
            return false;
        }
        
        BeanWrapper wrapper = new BeanWrapperImpl(obj);
        return wrapper.isWritableProperty(propertyName);
    }

    /**
     * 获取对象中值为 null 的属性名称数组
     * 
     * @param source 源对象
     * @return 值为 null 的属性名称数组
     */
    public static String[] getNullPropertyNames(Object source) {
        Assert.notNull(source, "Source must not be null");
        
        BeanWrapper wrapper = new BeanWrapperImpl(source);
        PropertyDescriptor[] descriptors = wrapper.getPropertyDescriptors();
        
        Set<String> nullPropertyNames = new HashSet<>();
        for (PropertyDescriptor descriptor : descriptors) {
            String propertyName = descriptor.getName();
            if (!"class".equals(propertyName)) {
                Object value = wrapper.getPropertyValue(propertyName);
                if (value == null) {
                    nullPropertyNames.add(propertyName);
                }
            }
        }
        return nullPropertyNames.toArray(new String[0]);
    }

    // ==================== 合并方法 ====================

    /**
     * 将源对象的非空属性合并到目标对象
     * 
     * @param source 源对象
     * @param target 目标对象
     * @throws BeansException 如果合并过程中发生错误
     */
    public static void mergeNonNullProperties(Object source, Object target) throws BeansException {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");
        
        String[] nullPropertyNames = getNullPropertyNames(source);
        copyProperties(source, target, nullPropertyNames);
    }

    /**
     * 深度合并两个对象的属性（递归处理嵌套对象）
     * 
     * @param source 源对象
     * @param target 目标对象
     * @throws BeansException 如果合并过程中发生错误
     */
    public static void deepMerge(Object source, Object target) throws BeansException {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");
        
        BeanWrapper sourceWrapper = new BeanWrapperImpl(source);
        BeanWrapper targetWrapper = new BeanWrapperImpl(target);
        
        PropertyDescriptor[] sourceDescriptors = sourceWrapper.getPropertyDescriptors();
        
        for (PropertyDescriptor sourceDescriptor : sourceDescriptors) {
            String propertyName = sourceDescriptor.getName();
            if ("class".equals(propertyName)) {
                continue;
            }
            
            Object sourceValue = sourceWrapper.getPropertyValue(propertyName);
            Object targetValue = targetWrapper.getPropertyValue(propertyName);
            
            if (sourceValue == null) {
                continue;
            }
            
            if (isSimpleType(sourceValue.getClass())) {
                targetWrapper.setPropertyValue(propertyName, sourceValue);
            } else if (sourceValue instanceof Collection) {
                if (targetValue == null || !(targetValue instanceof Collection)) {
                    targetWrapper.setPropertyValue(propertyName, sourceValue);
                }
            } else if (sourceValue instanceof Map) {
                if (targetValue == null || !(targetValue instanceof Map)) {
                    targetWrapper.setPropertyValue(propertyName, sourceValue);
                }
            } else if (sourceValue.getClass().isArray()) {
                targetWrapper.setPropertyValue(propertyName, sourceValue);
            } else if (targetValue != null && sourceValue.getClass().isAssignableFrom(targetValue.getClass())) {
                deepMerge(sourceValue, targetValue);
            } else {
                targetWrapper.setPropertyValue(propertyName, sourceValue);
            }
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 判断类型是否为简单类型（基本类型、包装类型、字符串、日期等）
     * 
     * @param clazz 类型
     * @return true-简单类型，false-复杂类型
     */
    private static boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == String.class
                || clazz == Boolean.class
                || clazz == Byte.class
                || clazz == Character.class
                || clazz == Short.class
                || clazz == Integer.class
                || clazz == Long.class
                || clazz == Float.class
                || clazz == Double.class
                || clazz == Date.class
                || clazz == java.sql.Date.class
                || clazz == java.sql.Time.class
                || clazz == java.sql.Timestamp.class
                || clazz.isEnum();
    }

    /**
     * 安全地复制属性，忽略异常
     * 
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyPropertiesSafely(Object source, Object target) {
        try {
            copyProperties(source, target);
        } catch (Exception e) {
            // 忽略异常
        }
    }

    /**
     * 安全地转换对象，忽略异常
     * 
     * @param source 源对象
     * @param targetClass 目标类型
     * @param <T> 目标类型泛型
     * @return 转换后的目标对象，如果转换失败返回 null
     */
    public static <T> T convertSafely(Object source, Class<T> targetClass) {
        try {
            return convert(source, targetClass);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 安全地转换集合，忽略异常
     * 
     * @param sourceList 源对象集合
     * @param targetClass 目标类型
     * @param <S> 源类型泛型
     * @param <T> 目标类型泛型
     * @return 转换后的目标对象集合，如果转换失败返回空集合
     */
    public static <S, T> List<T> convertListSafely(Collection<S> sourceList, Class<T> targetClass) {
        try {
            return convertList(sourceList, targetClass);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}