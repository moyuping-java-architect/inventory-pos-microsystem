package com.psi.common.dto.sync;

import lombok.Data;

/**
 * 下行同步数据 DTO（Feign 调用使用）
 * 收银微服务通过此 DTO 接收从中间同步服务拉取的下行数据
 */
@Data
public class DownSyncDTO {

    /** 主键 */
    private Long id;

    /** 整批批次唯一UUID */
    private String batchUuid;

    /** 租户ID */
    private String tenantId;

    /** 商铺编码 */
    private String shopCode;

    /** 目标业务表名（如：pos_operator, pos_config, customer） */
    private String tableName;

    /** 业务数据UUID（用于幂等和冲突判断） */
    private String dataUuid;

    /** 数据版本号（用于冲突解决） */
    private Long dataVersion;

    /** 批量明细JSON List */
    private String jsonData;

    /** 同步状态：0待下载 1已下载 */
    private Integer syncStatus;

    /** 创建时间 */
    private String createTime;

    /** POS拉取完毕时间 */
    private String lastDownloadTime;
}