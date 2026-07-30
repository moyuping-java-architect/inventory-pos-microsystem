package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sync_log")
public class SyncLogEntity {
    @TableField("tenant_id")
    private String tenantId;

    @TableField("type")
    private String type;

    @TableField("last_download_time")
    private String lastTime;
}