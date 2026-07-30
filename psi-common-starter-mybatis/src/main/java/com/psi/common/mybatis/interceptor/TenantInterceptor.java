package com.psi.common.mybatis.interceptor;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.psi.common.context.UserContext;
import com.psi.common.context.UserInfo;
import com.psi.common.mybatis.properties.MyBatisProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 表信息（包含表名和别名）
 */
class TableInfo {
    private String tableName;
    private String alias;

    public TableInfo(String tableName, String alias) {
        this.tableName = tableName;
        this.alias = alias;
    }

    public String getTableName() {
        return tableName;
    }

    public String getAlias() {
        return alias;
    }
}

/**
 * 租户拦截器
 * 自动在查询条件中添加租户ID过滤
 * 支持白名单配置，白名单中的表不会被添加租户条件
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
public class TenantInterceptor implements InnerInterceptor {

    /**
     * 租户字段名
     */
    private static final String TENANT_ID_FIELD = "tenant_id";

    /**
     * SQL 模板：添加租户条件（使用参数化方式）
     */
    private static final String TENANT_CONDITION_TEMPLATE = " AND %s = #{tenantId}";

    private final MyBatisProperties properties;

    public TenantInterceptor(MyBatisProperties properties) {
        this.properties = properties;
    }

    /**
     * 白名单表名集合（小写，用于快速查找）
     */
    private Set<String> whitelistTableSet;

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
                           ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        // 检查是否启用租户拦截器
        if (!properties.getTenant().isEnabled()) {
            return;
        }

        // 获取当前用户的租户ID
        UserInfo currentUser = UserContext.get();
        if (currentUser == null || currentUser.getTenantId() == null || currentUser.getTenantId().isEmpty()) {
            return;
        }

        // 获取表名和别名
        TableInfo tableInfo = getTableInfo(ms, boundSql);
        if (tableInfo == null) {
            return;
        }

        // 检查表是否在白名单中
        if (isWhitelistTable(tableInfo.getTableName())) {
            return;
        }

        String tenantId = currentUser.getTenantId();
        
        // 获取原始 SQL
        String originalSql = boundSql.getSql();
        if (originalSql == null || originalSql.isEmpty()) {
            return;
        }

        // 检查 SQL 的 WHERE 子句中是否已经包含租户条件
        // 注意：不能简单判断整个 SQL 是否包含 tenant_id，因为 SELECT 字段列表中也可能出现该字段
        if (hasTenantCondition(originalSql)) {
            return;
        }

        // 构建租户条件（使用表别名）
        String tenantField = tableInfo.getAlias() != null ? tableInfo.getAlias() + "." + TENANT_ID_FIELD : TENANT_ID_FIELD;
        
        // 租户ID可能是字符串或数字，直接作为字符串处理
        String tenantCondition = String.format(" AND %s = '%s'", tenantField, tenantId);
        
        // 修改 SQL，添加租户条件
        // 需要在 WHERE 和 ORDER BY/LIMIT 之间插入租户条件
        String newSql = insertTenantCondition(originalSql, tenantCondition);

        log.debug("Tenant filter applied for table [{}]: add condition '{}'", tableInfo.getTableName(), tenantCondition);

        // 更新 BoundSql 中的 SQL
        try {
            MetaObject metaObject = SystemMetaObject.forObject(boundSql);
            metaObject.setValue("sql", newSql);
        } catch (Exception e) {
            log.warn("Failed to set BoundSql.sql via MetaObject, fallback to PluginUtils", e);
            PluginUtils.mpBoundSql(boundSql).sql(newSql);
        }
    }

    /**
     * 判断 SQL 的 WHERE 子句中是否已经包含租户条件
     *
     * @param originalSql 原始 SQL
     * @return true-已包含租户条件，false-未包含
     */
    private boolean hasTenantCondition(String originalSql) {
        if (originalSql == null || originalSql.isEmpty()) {
            return false;
        }
        String sqlLower = originalSql.toLowerCase();
        int whereIndex = sqlLower.lastIndexOf(" where ");
        if (whereIndex < 0) {
            return false;
        }
        // 只检查 WHERE 之后的部分是否包含 tenant_id
        String wherePart = sqlLower.substring(whereIndex + " where ".length());
        // 排除 ORDER BY / LIMIT / GROUP BY / HAVING 等后续子句，只关注条件部分
        int orderByIndex = wherePart.indexOf(" order by ");
        int limitIndex = wherePart.indexOf(" limit ");
        int groupByIndex = wherePart.indexOf(" group by ");
        int havingIndex = wherePart.indexOf(" having ");
        int endIndex = wherePart.length();
        for (int idx : new int[]{orderByIndex, limitIndex, groupByIndex, havingIndex}) {
            if (idx >= 0 && idx < endIndex) {
                endIndex = idx;
            }
        }
        String conditionPart = wherePart.substring(0, endIndex);
        return conditionPart.contains(TENANT_ID_FIELD.toLowerCase());
    }

    /**
     * 在 SQL 中正确的位置插入租户条件
     * 
     * @param originalSql 原始 SQL
     * @param tenantCondition 租户条件
     * @return 修改后的 SQL
     */
    private String insertTenantCondition(String originalSql, String tenantCondition) {
        if (originalSql == null || originalSql.isEmpty()) {
            return originalSql;
        }

        String sql = originalSql.trim();
        String sqlLower = sql.toLowerCase();

        // 查找 GROUP BY / HAVING / ORDER BY / LIMIT 的位置，取最早出现者作为插入边界
        int groupByIndex = sqlLower.indexOf(" group by ");
        int havingIndex = sqlLower.indexOf(" having ");
        int orderByIndex = sqlLower.indexOf(" order by ");
        int limitIndex = sqlLower.indexOf(" limit ");

        int insertIndex = -1;
        for (int idx : new int[]{groupByIndex, havingIndex, orderByIndex, limitIndex}) {
            if (idx >= 0 && (insertIndex < 0 || idx < insertIndex)) {
                insertIndex = idx;
            }
        }

        // 检查是否有 WHERE 子句，且 WHERE 在插入边界之前
        int whereIndex = sqlLower.lastIndexOf(" where ");

        if (insertIndex > 0) {
            String beforePart = sql.substring(0, insertIndex);
            String afterPart = sql.substring(insertIndex);

            if (whereIndex > 0 && whereIndex < insertIndex) {
                // 已有 WHERE，直接追加条件
                return beforePart + tenantCondition + " " + afterPart;
            } else {
                // 没有 WHERE，需要添加 WHERE
                return beforePart + " WHERE 1=1" + tenantCondition + " " + afterPart;
            }
        } else {
            // 没有 GROUP BY/HAVING/ORDER BY/LIMIT，检查是否有 WHERE
            if (whereIndex > 0) {
                // 已有 WHERE，直接追加
                return sql + tenantCondition;
            } else {
                // 没有 WHERE，添加 WHERE
                return sql + " WHERE 1=1" + tenantCondition;
            }
        }
    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {
        // 更新操作也添加租户条件
        beforeQuery(executor, ms, parameter, null, null, ms.getBoundSql(parameter));
    }

    /**
     * 从 MappedStatement 中获取表信息（表名和别名）
     *
     * @param ms       MappedStatement
     * @param boundSql BoundSql
     * @return 表信息
     */
    private TableInfo getTableInfo(MappedStatement ms, BoundSql boundSql) {
        if (ms == null || boundSql == null) {
            return null;
        }

        // 从 SQL 中提取表名（简单实现，适用于单表操作）
        String sql = boundSql.getSql();
        if (sql == null || sql.isEmpty()) {
            return null;
        }

        // 去除前后空格并转小写
        String originalSql = sql.trim();
        String sqlLower = originalSql.toLowerCase();

        // 复杂 SQL（UNION / 子查询）跳过租户过滤，避免解析错误
        if (sqlLower.contains(" union ") || sqlLower.contains(" union all ")) {
            return null;
        }

        // 处理 SELECT 语句
        if (sqlLower.startsWith("select")) {
            // 必须匹配独立的 FROM 关键字，避免命中类似 from_warehouse_name 的字段名
            int fromIndex = sqlLower.indexOf(" from ");
            if (fromIndex > 0) {
                String afterFrom = originalSql.substring(fromIndex + 6).trim();
                // FROM 后面直接是子查询，跳过租户过滤
                if (afterFrom.startsWith("(")) {
                    return null;
                }
                // 提取第一个表名和可能的别名
                return parseTableAndAlias(afterFrom);
            }
        }
        // 处理 UPDATE 语句
        else if (sqlLower.startsWith("update")) {
            String afterUpdate = originalSql.substring(6).trim();
            return parseTableAndAlias(afterUpdate);
        }
        // 处理 DELETE 语句
        else if (sqlLower.startsWith("delete")) {
            // 必须匹配独立的 FROM 关键字
            int fromIndex = sqlLower.indexOf(" from ");
            if (fromIndex > 0) {
                String afterFrom = originalSql.substring(fromIndex + 6).trim();
                return parseTableAndAlias(afterFrom);
            }
        }

        return null;
    }

    /**
     * 解析表名和别名
     * 
     * @param afterFrom FROM/UPDATE 后面的部分
     * @return 表信息
     */
    private TableInfo parseTableAndAlias(String afterFrom) {
        // 移除括号和其他符号
        String cleanPart = afterFrom.replaceAll("[()]", " ").trim();
        
        // 按空格、逗号、AS 分割
        String[] parts = cleanPart.split("[\\s,]+");
        if (parts.length == 0) {
            return null;
        }
        
        String tableName = parts[0];
        String alias = null;
        
        // 检查是否有别名（可能有 AS 关键字）
        if (parts.length >= 2) {
            String secondPart = parts[1].toLowerCase();
            if (!secondPart.equals("as") && !secondPart.equals("on") && !secondPart.equals("left") 
                && !secondPart.equals("right") && !secondPart.equals("inner") && !secondPart.equals("outer")
                && !secondPart.equals("join") && !secondPart.equals("where")) {
                // 如果第二个部分不是关键字，可能是别名
                if (!secondPart.equals("as")) {
                    alias = parts[1];
                } else if (parts.length >= 3) {
                    // 如果有 AS 关键字，第三个部分是别名
                    alias = parts[2];
                }
            }
        }
        
        return new TableInfo(tableName, alias);
    }

    /**
     * 检查表是否在白名单中
     *
     * @param tableName 表名
     * @return true-在白名单中，false-不在白名单中
     */
    private boolean isWhitelistTable(String tableName) {
        if (tableName == null) {
            return false;
        }

        // 懒加载白名单集合
        if (whitelistTableSet == null) {
            whitelistTableSet = properties.getTenant().getWhitelistTables().stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        }

        return whitelistTableSet.contains(tableName.toLowerCase());
    }
}