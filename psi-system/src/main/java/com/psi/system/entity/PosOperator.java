package com.psi.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收银员实体
 * 后台管理收银员，数据通过 psi-sync 下行同步到 POS 机本地 SQLite
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pos_operator")
public class PosOperator extends BaseEntity {

    /** 门店编码 */
    private String shopCode;

    /** 登录账号 */
    private String username;

    /** 登录密码 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 角色 1-收银员 2-管理员 */
    private Integer role;
}