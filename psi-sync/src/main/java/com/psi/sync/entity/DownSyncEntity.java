package com.psi.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 下行同步中间表实体类
 * 进销存 → POS
 */
@Data
@TableName("down_sync")
public class DownSyncEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 整批批次唯一UUID
     */
    private String batchUuid;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 商铺编码
     */
    private String shopCode;

    /**
     * 目标业务表名
     */
    private String tableName;

    /**
     * 业务数据UUID（用于幂等和冲突判断）
     */
    private String dataUuid;

    /**
     * 数据版本号（用于冲突解决）
     */
    private Long dataVersion;

    /**
     * 批量明细JSON List
     */
    private String jsonData;

    /**
     * 同步状态：0待下载 1已下载
     */
    private Integer syncStatus;

    /**
     * 插入中间表自动生成时间(关键增量字段)
     */
    private String createTime;

    /**
     * POS拉取完毕时间
     */
    private String lastDownloadTime;
}