package com.psi.sync.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.sync.entity.UpSyncEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 上行单据中间表Mapper接口
 */
@Mapper
public interface UpSyncMapper extends BaseMapper<UpSyncEntity> {

    /**
     * 查询待处理的单据
     *
     * @return 待处理单据列表
     */
    List<UpSyncEntity> selectPendingProcess();

    /**
     * 查询待处理的上行数据（支持增量时间过滤）
     *
     * @param lastTime 上次拉取时间
     * @return 待处理数据列表
     */
    List<UpSyncEntity> selectPendingProcessWithTime(@Param("lastTime") String lastTime);

    /**
     * 根据批次UUID列表更新处理状态
     *
     * @param batchUuids 批次UUID列表
     * @param syncStatus 同步状态：0待处理 1成功 2失败
     * @return 更新数量
     */
    int batchUpdateStatusByBatchUuids(@Param("batchUuids") List<String> batchUuids,
                                      @Param("syncStatus") Integer syncStatus);

    /**
     * 幂等插入：INSERT IGNORE，返回受影响行数
     * 0 表示记录已存在（幂等跳过）
     *
     * @param entity 上行同步实体
     * @return 受影响行数
     */
    int insertIgnore(@Param("entity") UpSyncEntity entity);

    /**
     * 递增重试次数
     *
     * @param id 记录ID
     * @return 受影响行数
     */
    int incrementRetryCount(@Param("id") Long id);

    /**
     * 按记录ID更新状态（逐条确认，事务安全）
     *
     * @param id         记录ID
     * @param syncStatus 同步状态
     * @param errorMsg   错误信息（可选）
     * @return 受影响行数
     */
    int updateStatusById(@Param("id") Long id,
                         @Param("syncStatus") Integer syncStatus,
                         @Param("errorMsg") String errorMsg);
}
