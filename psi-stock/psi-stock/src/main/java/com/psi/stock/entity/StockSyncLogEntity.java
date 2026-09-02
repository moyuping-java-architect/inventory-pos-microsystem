package com.psi.stock.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("stock_sync_log")
public class StockSyncLogEntity {
    @TableField("tenant_id")
    private String tenantId;

    @TableField("type")
    private String type;

    @TableField("last_download_time")
    private String lastTime;
}