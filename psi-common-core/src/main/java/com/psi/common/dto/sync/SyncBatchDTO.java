package com.psi.common.dto.sync;

import lombok.Data;

import java.util.List;

/**
 * 同步批次DTO
 * 用于封装一个批次的同步数据
 * 统一版本，所有微服务共用此类
 */
@Data
public class SyncBatchDTO {

    /** 批次UUID */
    private String batchUuid;

    /** 租户ID */
    private String tenantId;

    /** 店铺编码 */
    private String shopCode;

    /** 创建时间 */
    private String createTime;

    /** 同步数据列表 */
    private List<SyncDataDTO> dataList;
}