package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 操作员实体
 * 对应数据库表：operator
 */
@Data
@TableName("operator")
public class OperatorEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 操作员ID
     */
    @TableId(type = IdType.AUTO)
    private Integer operatorId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 数据UUID（跨服务比对用）
     */
    private String dataUuid;

    /**
     * 数据版本号（用于冲突解决）
     */
    private Long dataVersion;

    /**
     * 门店编码
     */
    private String shopCode;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 删除标识
     */
    private Integer delFlag;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色：1-收银员 2-管理员
     */
    private Integer role;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private String createTime;
}