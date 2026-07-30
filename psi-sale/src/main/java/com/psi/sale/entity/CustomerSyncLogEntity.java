package com.psi.sale.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 同步日志实体
 * 记录各类型数据的最后同步时间
 */
@Data
@TableName("sync_log")
public class CustomerSyncLogEntity {

    @TableField("data_uuid")
    private String dataUuid;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("type")
    private String type;

    @TableField("last_download_time")
    private String lastTime;
}