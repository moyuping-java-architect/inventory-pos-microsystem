package com.psi.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收银机配置实体
 * 后台管理收银机，数据通过 psi-sync 下行同步到 POS 机本地 SQLite
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pos_config")
public class PosConfig extends BaseEntity {

    /** POS硬件序列号(全球唯一) */
    private String posSn;

    /** 门店编码 */
    private String shopCode;

    /** 收银机编号(如POS01) */
    private String posId;

    /** 收银机名称 */
    private String posName;
}