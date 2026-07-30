package com.psi.sync.service;

import com.psi.sync.entity.UpSyncEntity;

import java.util.List;

/**
 * 上行同步服务接口
 * POS → 进销存
 */
public interface UpSyncService {

    /**
     * 插入上行同步数据
     *
     * @param entity 上行同步实体
     * @return 是否插入成功
     */
    boolean insert(UpSyncEntity entity);

    /**
     * 批量插入上行同步数据
     *
     * @param entities 上行同步实体列表
     * @return 插入数量
     */
    int batchInsert(List<UpSyncEntity> entities);

    /**
     * 查询待处理的单据
     *
     * @return 待处理单据列表
     */
    List<UpSyncEntity> getPendingProcess();

    /**
     * 查询待处理的上行数据（支持增量时间过滤）
     *
     * @param lastTime 上次拉取时间
     * @return 待处理数据列表
     */
    List<UpSyncEntity> getPendingProcess(String lastTime);

    /**
     * 更新处理状态
     *
     * @param id         主键ID
     * @param syncStatus 同步状态
     * @return 是否更新成功
     */
    boolean updateProcessStatus(Long id, Integer syncStatus);

    /**
     * 批量更新处理状态（根据批次UUID）
     *
     * @param batchUuids 批次UUID列表
     * @param syncStatus 同步状态：0待处理 1成功 2失败
     * @return 更新数量
     */
    int batchUpdateProcessStatus(List<String> batchUuids, Integer syncStatus);

    /**
     * 根据批次UUID查询
     *
     * @param batchUuid 批次UUID
     * @return 上行同步实体
     */
    UpSyncEntity getByBatchUuid(String batchUuid);

    /**
     * 检查recordId是否已存在（幂等性校验）
     *
     * @param recordId 记录唯一ID
     * @return 是否已存在
     */
    boolean existsByRecordId(String recordId);

    /**
     * 插入上行同步数据（使用recordId作为唯一约束）
     *
     * @param entity   上行同步实体
     * @param recordId 记录唯一ID
     * @return 是否插入成功
     */
    boolean insertWithRecordId(UpSyncEntity entity, String recordId);

    /**
     * 幂等插入：利用数据库唯一索引 IGNORE 重复记录
     *
     * @param entity 上行同步实体（需已设置 recordId）
     * @return true=插入成功，false=已存在被忽略
     */
    boolean insertIgnore(UpSyncEntity entity);

    /**
     * 递增重试次数
     *
     * @param id 记录ID
     * @return 是否成功
     */
    boolean incrementRetryCount(Long id);

    /**
     * 按记录ID更新状态（逐条确认，事务安全）
     *
     * @param id         记录ID
     * @param syncStatus 同步状态
     * @param errorMsg   错误信息（可选）
     * @return 是否成功
     */
    boolean updateStatusById(Long id, Integer syncStatus, String errorMsg);
}
