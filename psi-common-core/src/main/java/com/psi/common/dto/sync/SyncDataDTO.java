package com.psi.common.dto.sync;

import lombok.Data;

/**
 * 同步数据DTO
 * 封装单条记录的同步数据
 * 统一版本，所有微服务共用此类
 */
@Data
public class SyncDataDTO {

    /** 记录唯一ID（用于重试去重） */
    private String recordId;

    /** 批次UUID */
    private String batchUuid;

    /** 租户ID */
    private String tenantId;

    /** 商铺编码 */
    private String shopCode;

    /** 表名（用于区分数据类型） */
    private String tableName;

    /** JSON格式的数据内容 */
    private String jsonData;

    /** 创建时间 */
    private String createTime;

    /** 业务主键 */
    private String businessKey;

    /** 数据类型 */
    private String dataType;

    /** 原始数据ID */
    private Long originalId;

    /** 数据版本号（用于冲突解决） */
    private Long dataVersion;

    /** 业务数据UUID（用于幂等和冲突判断） */
    private String dataUuid;
}