package com.psi.common.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.psi.common.mybatis.entity.BaseEntity;
import com.psi.common.context.UserContext;
import com.psi.common.context.UserInfo;
import com.psi.common.mybatis.properties.MyBatisProperties;
import com.psi.common.util.IdUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 实体字段自动填充处理器
 * 自动填充创建时间、创建人、更新时间、更新人等字段
 * 与 BaseEntity 配合使用
 *
 * @author PSI
 * @version 1.0.0
 */
@Component
public class EntityMetaObjectHandler implements MetaObjectHandler {

    private final MyBatisProperties properties;

    public EntityMetaObjectHandler(MyBatisProperties properties) {
        this.properties = properties;
    }

    /**
     * 插入操作时自动填充字段
     * 自动填充 BaseEntity 中定义的公共字段
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 检查是否启用自动填充
        if (!properties.getAutoFill().isEnabled()) {
            return;
        }

        // 检查实体是否继承自 BaseEntity
        if (!(metaObject.getOriginalObject() instanceof BaseEntity)) {
            return;
        }

        // 获取当前用户信息
        UserInfo currentUser = UserContext.get();
        Long userId = currentUser != null ? parseLong(currentUser.getUpdateUserId()) : null;
        Long tenantId = currentUser != null ? parseLong(currentUser.getTenantId()) : null;
        LocalDateTime now = LocalDateTime.now();

        // 填充创建时间
        if (properties.getAutoFill().isCreateTime() && isFieldExists(metaObject, "createTime")) {
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        }

        // 填充创建人
        if (properties.getAutoFill().isCreateBy() && userId != null && isFieldExists(metaObject, "createBy")) {
            this.strictInsertFill(metaObject, "createBy", Long.class, userId);
        }

        // 填充更新时间（插入时也需要填充）
        if (properties.getAutoFill().isUpdateTime() && isFieldExists(metaObject, "updateTime")) {
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        }

        // 填充更新人（插入时也需要填充）
        if (properties.getAutoFill().isUpdateBy() && userId != null && isFieldExists(metaObject, "updateBy")) {
            this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
        }

        // 填充租户 ID
        if (properties.getAutoFill().isTenantId() && tenantId != null && isFieldExists(metaObject, "tenantId")) {
            this.strictInsertFill(metaObject, "tenantId", Long.class, tenantId);
        }

        // 填充删除标志（默认未删除：0）
        if (properties.getAutoFill().isDelFlag() && isFieldExists(metaObject, "delFlag")) {
            Object delFlag = metaObject.getValue("delFlag");
            if (delFlag == null) {
                this.strictInsertFill(metaObject, "delFlag", Integer.class, 0);
            }
        }

        // 填充状态（默认启用：1）
        if (properties.getAutoFill().isStatus() && isFieldExists(metaObject, "status")) {
            Object status = metaObject.getValue("status");
            if (status == null) {
                this.strictInsertFill(metaObject, "status", Integer.class, 1);
            }
        }

        // 填充版本号（乐观锁，默认：1）
        if (isFieldExists(metaObject, "version")) {
            Object version = metaObject.getValue("version");
            if (version == null) {
                this.strictInsertFill(metaObject, "version", Integer.class, 1);
            }
        }

        // 填充数据唯一标识（雪花算法生成），用于分布式数据同步
        if (isFieldExists(metaObject, "dataUuid")) {
            Object dataUuid = metaObject.getValue("dataUuid");
            if (dataUuid == null) {
                this.strictInsertFill(metaObject, "dataUuid", String.class, IdUtils.snowflakeIdStr());
            }
        }
    }

    /**
     * 更新操作时自动填充字段
     * 自动更新 BaseEntity 中定义的更新相关字段
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 检查是否启用自动填充
        if (!properties.getAutoFill().isEnabled()) {
            return;
        }

        // 检查实体是否继承自 BaseEntity
        if (!(metaObject.getOriginalObject() instanceof BaseEntity)) {
            return;
        }

        // 获取当前用户信息
        UserInfo currentUser = UserContext.get();
        Long userId = currentUser != null ? parseLong(currentUser.getUpdateUserId()) : null;
        LocalDateTime now = LocalDateTime.now();

        // 填充更新时间
        if (properties.getAutoFill().isUpdateTime() && isFieldExists(metaObject, "updateTime")) {
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
        }

        // 填充更新人
        if (properties.getAutoFill().isUpdateBy() && userId != null && isFieldExists(metaObject, "updateBy")) {
            this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
        }
    }

    /**
     * 检查指定字段是否存在于实体中
     *
     * @param metaObject 元对象
     * @param fieldName  字段名称
     * @return true-字段存在，false-字段不存在
     */
    private boolean isFieldExists(MetaObject metaObject, String fieldName) {
        if (metaObject == null || fieldName == null) {
            return false;
        }
        try {
            return metaObject.hasGetter(fieldName) && metaObject.hasSetter(fieldName);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 安全的字符串转 Long
     *
     * @param value 字符串值
     * @return Long 值，转换失败返回 null
     */
    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}