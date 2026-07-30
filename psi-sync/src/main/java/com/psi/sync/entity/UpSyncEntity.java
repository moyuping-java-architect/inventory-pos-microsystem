package com.psi.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 上行单据中间表实体类
 * POS → 进销存
 */
@Data
@TableName("up_sync")
public class UpSyncEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 记录唯一ID（用于幂等性校验）
     */
    private String recordId;

    /**
     * 批次唯一编号
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
     * 收银机设备编码
     */
    private String posSn;

    /**
     * 单据对应表名
     */
    private String tableName;

    /**
     * 业务主键（如订单号），用于幂等和冲突判断
     */
    private String businessKey;

    /**
     * 数据版本号（用于冲突解决）
     */
    private Long dataVersion;

    /**
     * 单据集合JSON
     */
    private String jsonData;

    /**
     * 同步状态：0待处理 1成功 2失败
     */
    private Integer syncStatus;

    /**
     * 单据写入中间表时间
     */
    private String createTime;

    /**
     * 处理时间
     */
    private String processTime;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 重试次数
     */
    private Integer retryCount;
}