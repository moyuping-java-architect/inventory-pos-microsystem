package com.psi.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON 工具类
 * 基于 Jackson 实现，提供 JSON 序列化、反序列化、格式化等功能
 * 
 * @author PSI
 * @version 1.0.0
 */
public final class JsonUtils {

    private JsonUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * 默认 ObjectMapper 实例
     * 配置：
     * - 禁用空对象序列化失败
     * - 禁用日期时间戳格式，使用 ISO-8601 格式
     * - 忽略未知属性
     */
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    /**
     * 格式化输出的 ObjectMapper 实例（带缩进）
     */
    private static final ObjectMapper PRETTY_OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(SerializationFeature.INDENT_OUTPUT);

    // ==================== 序列化方法 ====================

    /**
     * 将对象转换为 JSON 字符串
     * @param obj 待序列化的对象
     * @return JSON 字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return DEFAULT_OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }

    /**
     * 将对象转换为格式化的 JSON 字符串（带缩进）
     * @param obj 待序列化的对象
     * @return 格式化的 JSON 字符串
     */
    public static String toJsonPretty(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return PRETTY_OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 格式化序列化失败", e);
        }
    }

    /**
     * 将对象写入 JSON 文件
     * @param obj 待序列化的对象
     * @param file 目标文件
     */
    public static void writeJsonToFile(Object obj, File file) {
        if (obj == null || file == null) {
            return;
        }
        try {
            DEFAULT_OBJECT_MAPPER.writeValue(file, obj);
        } catch (IOException e) {
            throw new RuntimeException("写入 JSON 文件失败", e);
        }
    }

    /**
     * 将对象写入格式化的 JSON 文件（带缩进）
     * @param obj 待序列化的对象
     * @param file 目标文件
     */
    public static void writeJsonToFilePretty(Object obj, File file) {
        if (obj == null || file == null) {
            return;
        }
        try {
            PRETTY_OBJECT_MAPPER.writeValue(file, obj);
        } catch (IOException e) {
            throw new RuntimeException("写入格式化 JSON 文件失败", e);
        }
    }

    // ==================== 反序列化方法 ====================

    /**
     * 将 JSON 字符串转换为指定类型的对象
     * @param json JSON 字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json) || clazz == null) {
            return null;
        }
        try {
            return DEFAULT_OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    /**
     * 将 JSON 字符串转换为复杂类型（如 List<T>, Map<K,V>）
     * @param json JSON 字符串
     * @param typeReference 类型引用
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(json) || typeReference == null) {
            return null;
        }
        try {
            return DEFAULT_OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    /**
     * 从文件读取 JSON 并转换为指定类型的对象
     * @param file JSON 文件
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T readJsonFromFile(File file, Class<T> clazz) throws IOException {
        if (file == null || clazz == null) {
            return null;
        }
        return DEFAULT_OBJECT_MAPPER.readValue(file, clazz);
    }

    /**
     * 从输入流读取 JSON 并转换为指定类型的对象
     * @param inputStream 输入流
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T readJsonFromStream(InputStream inputStream, Class<T> clazz) throws IOException {
        if (inputStream == null || clazz == null) {
            return null;
        }
        return DEFAULT_OBJECT_MAPPER.readValue(inputStream, clazz);
    }

    // ==================== 集合转换方法 ====================

    /**
     * 将 JSON 字符串转换为 List
     * @param json JSON 字符串
     * @param elementType 元素类型
     * @param <T> 泛型类型
     * @return List 对象
     */
    public static <T> List<T> toList(String json, Class<T> elementType) {
        if (StringUtils.isEmpty(json) || elementType == null) {
            return new ArrayList<>();
        }
        try {
            JavaType javaType = DEFAULT_OBJECT_MAPPER.getTypeFactory()
                    .constructCollectionType(List.class, elementType);
            return DEFAULT_OBJECT_MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 转 List 失败", e);
        }
    }

    /**
     * 将 JSON 字符串转换为 Map
     * @param json JSON 字符串
     * @param keyType 键类型
     * @param valueType 值类型
     * @param <K> 键泛型
     * @param <V> 值泛型
     * @return Map 对象
     */
    public static <K, V> Map<K, V> toMap(String json, Class<K> keyType, Class<V> valueType) {
        if (StringUtils.isEmpty(json) || keyType == null || valueType == null) {
            return new HashMap<>();
        }
        try {
            JavaType javaType = DEFAULT_OBJECT_MAPPER.getTypeFactory()
                    .constructMapType(Map.class, keyType, valueType);
            return DEFAULT_OBJECT_MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 转 Map 失败", e);
        }
    }

    /**
     * 将对象转换为 Map
     * @param obj 待转换的对象
     * @return Map 对象
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        String json = toJson(obj);
        return fromJson(json, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 将对象转换为 List
     * @param obj 待转换的对象
     * @return List 对象
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> toList(Object obj) {
        if (obj == null) {
            return new ArrayList<>();
        }
        if (obj instanceof List) {
            return (List<T>) obj;
        }
        String json = toJson(obj);
        return fromJson(json, new TypeReference<List<T>>() {});
    }

    // ==================== JSON 节点操作方法 ====================

    /**
     * 创建空的 ObjectNode
     * @return ObjectNode 对象
     */
    public static ObjectNode createObjectNode() {
        return DEFAULT_OBJECT_MAPPER.createObjectNode();
    }

    /**
     * 创建空的 ArrayNode
     * @return ArrayNode 对象
     */
    public static ArrayNode createArrayNode() {
        return DEFAULT_OBJECT_MAPPER.createArrayNode();
    }

    /**
     * 从对象创建 ObjectNode
     * @param obj 待转换的对象
     * @return ObjectNode 对象
     */
    public static ObjectNode toObjectNode(Object obj) {
        if (obj == null) {
            return createObjectNode();
        }
        if (obj instanceof ObjectNode) {
            return (ObjectNode) obj;
        }
        return DEFAULT_OBJECT_MAPPER.valueToTree(obj);
    }

    /**
     * 从对象创建 ArrayNode
     * @param obj 待转换的对象（通常是 Collection 或数组）
     * @return ArrayNode 对象
     */
    public static ArrayNode toArrayNode(Object obj) {
        if (obj == null) {
            return createArrayNode();
        }
        if (obj instanceof ArrayNode) {
            return (ArrayNode) obj;
        }
        return DEFAULT_OBJECT_MAPPER.valueToTree(obj);
    }

    // ==================== JSON 值提取方法 ====================

    /**
     * 从 JSON 字符串中提取指定路径的值
     * @param json JSON 字符串
     * @param path 属性路径，支持 "." 分隔（如 "user.name"）
     * @param defaultValue 默认值
     * @param <T> 泛型类型
     * @return 提取的值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(String json, String path, T defaultValue) {
        if (StringUtils.isEmpty(json) || StringUtils.isEmpty(path)) {
            return defaultValue;
        }
        try {
            ObjectNode node = DEFAULT_OBJECT_MAPPER.readValue(json, ObjectNode.class);
            String[] keys = path.split("\\.");
            Object current = node;
            for (String key : keys) {
                if (current instanceof ObjectNode) {
                    current = ((ObjectNode) current).get(key);
                } else if (current instanceof ArrayNode) {
                    try {
                        int index = Integer.parseInt(key);
                        current = ((ArrayNode) current).get(index);
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                } else {
                    return defaultValue;
                }
                if (current == null) {
                    return defaultValue;
                }
            }
            return (T) current;
        } catch (JsonProcessingException e) {
            return defaultValue;
        }
    }

    /**
     * 从 JSON 字符串中提取字符串值
     * @param json JSON 字符串
     * @param path 属性路径
     * @return 字符串值
     */
    public static String getString(String json, String path) {
        return getValue(json, path, null);
    }

    /**
     * 从 JSON 字符串中提取整数值
     * @param json JSON 字符串
     * @param path 属性路径
     * @return 整数值
     */
    public static Integer getInt(String json, String path) {
        return getValue(json, path, null);
    }

    /**
     * 从 JSON 字符串中提取长整数值
     * @param json JSON 字符串
     * @param path 属性路径
     * @return 长整数值
     */
    public static Long getLong(String json, String path) {
        return getValue(json, path, null);
    }

    /**
     * 从 JSON 字符串中提取布尔值
     * @param json JSON 字符串
     * @param path 属性路径
     * @return 布尔值
     */
    public static Boolean getBoolean(String json, String path) {
        return getValue(json, path, null);
    }

    /**
     * 从 JSON 字符串中提取双精度浮点值
     * @param json JSON 字符串
     * @param path 属性路径
     * @return 双精度浮点值
     */
    public static Double getDouble(String json, String path) {
        return getValue(json, path, null);
    }

    // ==================== JSON 校验方法 ====================

    /**
     * 判断字符串是否为有效的 JSON
     * @param json 待校验的字符串
     * @return true-有效 JSON，false-无效 JSON
     */
    public static boolean isValidJson(String json) {
        if (StringUtils.isEmpty(json)) {
            return false;
        }
        try {
            DEFAULT_OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 判断字符串是否为有效的 JSON 对象
     * @param json 待校验的字符串
     * @return true-有效 JSON 对象，false-无效
     */
    public static boolean isValidJsonObject(String json) {
        if (StringUtils.isEmpty(json)) {
            return false;
        }
        String trimmed = json.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return false;
        }
        return isValidJson(json);
    }

    /**
     * 判断字符串是否为有效的 JSON 数组
     * @param json 待校验的字符串
     * @return true-有效 JSON 数组，false-无效
     */
    public static boolean isValidJsonArray(String json) {
        if (StringUtils.isEmpty(json)) {
            return false;
        }
        String trimmed = json.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return false;
        }
        return isValidJson(json);
    }

    // ==================== JSON 转换方法 ====================

    /**
     * 将对象深拷贝（通过 JSON 序列化/反序列化实现）
     * @param obj 待拷贝的对象
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 拷贝后的对象
     */
    public static <T> T deepCopy(Object obj, Class<T> clazz) {
        if (obj == null || clazz == null) {
            return null;
        }
        String json = toJson(obj);
        return fromJson(json, clazz);
    }

    /**
     * 将对象转换为 JSON 字节数组
     * @param obj 待转换的对象
     * @return JSON 字节数组
     */
    public static byte[] toJsonBytes(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return DEFAULT_OBJECT_MAPPER.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 转字节数组失败", e);
        }
    }

    /**
     * 将 JSON 字节数组转换为对象
     * @param bytes JSON 字节数组
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromJsonBytes(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0 || clazz == null) {
            return null;
        }
        try {
            return DEFAULT_OBJECT_MAPPER.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new RuntimeException("字节数组转对象失败", e);
        }
    }

    /**
     * 移除 JSON 字符串中的指定属性
     * @param json JSON 字符串
     * @param path 属性路径
     * @return 移除属性后的 JSON 字符串
     */
    public static String removeProperty(String json, String path) {
        if (StringUtils.isEmpty(json) || StringUtils.isEmpty(path)) {
            return json;
        }
        try {
            ObjectNode node = DEFAULT_OBJECT_MAPPER.readValue(json, ObjectNode.class);
            String[] keys = path.split("\\.");
            Object current = node;
            Object parent = null;
            String lastKey = null;
            
            for (String key : keys) {
                if (current instanceof ObjectNode) {
                    parent = current;
                    lastKey = key;
                    current = ((ObjectNode) current).get(key);
                } else if (current instanceof ArrayNode) {
                    try {
                        int index = Integer.parseInt(key);
                        parent = current;
                        lastKey = String.valueOf(index);
                        current = ((ArrayNode) current).get(index);
                    } catch (NumberFormatException e) {
                        return json;
                    }
                } else {
                    return json;
                }
                if (current == null) {
                    return json;
                }
            }
            
            if (parent instanceof ObjectNode && lastKey != null) {
                ((ObjectNode) parent).remove(lastKey);
            } else if (parent instanceof ArrayNode && lastKey != null) {
                try {
                    int index = Integer.parseInt(lastKey);
                    ((ArrayNode) parent).remove(index);
                } catch (NumberFormatException e) {
                    return json;
                }
            }
            
            return toJson(node);
        } catch (JsonProcessingException e) {
            return json;
        }
    }

    /**
     * 设置 JSON 字符串中的指定属性值
     * @param json JSON 字符串
     * @param path 属性路径
     * @param value 属性值
     * @return 修改后的 JSON 字符串
     */
    public static String setProperty(String json, String path, Object value) {
        if (StringUtils.isEmpty(json) || StringUtils.isEmpty(path)) {
            return json;
        }
        try {
            ObjectNode node = DEFAULT_OBJECT_MAPPER.readValue(json, ObjectNode.class);
            String[] keys = path.split("\\.");
            ObjectNode current = node;
            
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (i == keys.length - 1) {
                    current.putPOJO(key, value);
                } else {
                    ObjectNode child = (ObjectNode) current.get(key);
                    if (child == null) {
                        child = DEFAULT_OBJECT_MAPPER.createObjectNode();
                        current.set(key, child);
                    }
                    current = child;
                }
            }
            
            return toJson(node);
        } catch (JsonProcessingException e) {
            return json;
        }
    }

    // ==================== 获取 ObjectMapper 实例 ====================

    /**
     * 获取默认的 ObjectMapper 实例
     * @return ObjectMapper 实例
     */
    public static ObjectMapper getObjectMapper() {
        return DEFAULT_OBJECT_MAPPER;
    }

    /**
     * 获取格式化输出的 ObjectMapper 实例
     * @return ObjectMapper 实例
     */
    public static ObjectMapper getPrettyObjectMapper() {
        return PRETTY_OBJECT_MAPPER;
    }
}