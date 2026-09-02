package com.psi.finance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("finance_sync_log")
public class FinanceSyncLogEntity {

    @TableField("tenant_id")
    private String tenantId;

    @TableField("type")
    private String type;

    @TableField("last_download_time")
    private String lastTime;
}