package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 系统配置实体类
 * 存储收银机唯一身份信息
 * 一台收银机只能有一条配置记录
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
@TableName("sys_config")
public class SysConfigEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 数据UUID（跨服务比对用）
     */
    private String dataUuid;

    /**
     * 数据版本号（用于冲突解决）
     */
    private Long dataVersion;

    /**
     * POS硬件序列号(全球唯一)
     */
    private String posSn;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 门店编码
     */
    private String shopCode;

    /**
     * 门店名称
     */
    private String shopName;

    /**
     * 收银机编号(如POS01)
     */
    private String posId;

    /**
     * 收银机名称
     */
    private String posName;

    /**
     * 最后同步时间
     */
    private String updateTime;

    /**
     * 强制整表只能存1条数据（固定为1）
     */
    private Integer onlyOne;
}