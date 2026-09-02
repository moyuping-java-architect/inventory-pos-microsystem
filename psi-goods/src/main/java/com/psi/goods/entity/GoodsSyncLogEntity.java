package com.psi.goods.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("goods_sync_log")
public class GoodsSyncLogEntity {

    @TableField("data_uuid")
    private String dataUuid;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("type")
    private String type;

    @TableField("last_download_time")
    private String lastTime;
}