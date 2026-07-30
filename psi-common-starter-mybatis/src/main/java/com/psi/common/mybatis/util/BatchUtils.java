package com.psi.common.mybatis.util;

import com.psi.common.mybatis.properties.MyBatisProperties;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
     * 批量操作工具类
     * 支持通过 Nacos 配置管理批次大小
     * 支持为不同操作类型（保存、更新、查询、删除）配置独立的批次大小
     * 
     * @author PSI
     * @version 1.0.0
     */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchUtils {

    private final MyBatisProperties myBatisProperties;

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        SAVE, UPDATE, QUERY, DELETE
    }

    /**
     * 批量保存数据
     * 使用配置的保存操作批次大小
     *
     * @param service    Service 实例
     * @param entityList 实体列表
     * @param <T>        实体类型
     * @return 是否全部保存成功
     */
    public <T> boolean saveBatch(IService<T> service, List<T> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return true;
        }
        
        int batchSize = getBatchSize(entityList.size(), OperationType.SAVE);
        log.debug("Batch save with size: {}, data count: {}", batchSize, entityList.size());
        
        return service.saveBatch(entityList, batchSize);
    }

    /**
     * 批量保存数据（指定批次大小）
     *
     * @param service    Service 实例
     * @param entityList 实体列表
     * @param batchSize  批次大小（会被限制在配置的最大/最小值之间）
     * @param <T>        实体类型
     * @return 是否全部保存成功
     */
    public <T> boolean saveBatch(IService<T> service, List<T> entityList, int batchSize) {
        if (entityList == null || entityList.isEmpty()) {
            return true;
        }
        
        int actualBatchSize = constrainBatchSize(batchSize);
        log.debug("Batch save with specified size: {} (constrained to: {}), data count: {}", 
                batchSize, actualBatchSize, entityList.size());
        
        return service.saveBatch(entityList, actualBatchSize);
    }

    /**
     * 批量更新数据
     * 使用配置的更新操作批次大小
     *
     * @param service    Service 实例
     * @param entityList 实体列表
     * @param <T>        实体类型
     * @return 是否全部更新成功
     */
    public <T> boolean updateBatchById(IService<T> service, List<T> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return true;
        }
        
        int batchSize = getBatchSize(entityList.size(), OperationType.UPDATE);
        log.debug("Batch update with size: {}, data count: {}", batchSize, entityList.size());
        
        return service.updateBatchById(entityList, batchSize);
    }

    /**
     * 批量更新数据（指定批次大小）
     *
     * @param service    Service 实例
     * @param entityList 实体列表
     * @param batchSize  批次大小（会被限制在配置的最大/最小值之间）
     * @param <T>        实体类型
     * @return 是否全部更新成功
     */
    public <T> boolean updateBatchById(IService<T> service, List<T> entityList, int batchSize) {
        if (entityList == null || entityList.isEmpty()) {
            return true;
        }
        
        int actualBatchSize = constrainBatchSize(batchSize);
        log.debug("Batch update with specified size: {} (constrained to: {}), data count: {}", 
                batchSize, actualBatchSize, entityList.size());
        
        return service.updateBatchById(entityList, actualBatchSize);
    }

    /**
     * 批量删除数据（按ID列表）
     * 使用配置的删除操作批次大小
     *
     * @param service Service 实例
     * @param idList  ID列表
     * @param <T>     实体类型
     * @return 是否全部删除成功
     */
    public <T> boolean removeByIds(IService<T> service, List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return true;
        }
        
        int batchSize = getBatchSize(idList.size(), OperationType.DELETE);
        return removeByIds(service, idList, batchSize);
    }

    /**
     * 批量删除数据（按ID列表，指定批次大小）
     *
     * @param service    Service 实例
     * @param idList     ID列表
     * @param batchSize  批次大小（会被限制在配置的最大/最小值之间）
     * @param <T>        实体类型
     * @return 是否全部删除成功
     */
    public <T> boolean removeByIds(IService<T> service, List<Long> idList, int batchSize) {
        if (idList == null || idList.isEmpty()) {
            return true;
        }
        
        int actualBatchSize = constrainBatchSize(batchSize);
        log.debug("Batch remove by ids with size: {}, data count: {}", actualBatchSize, idList.size());
        
        int totalSize = idList.size();
        for (int i = 0; i < totalSize; i += actualBatchSize) {
            int end = Math.min(i + actualBatchSize, totalSize);
            List<Long> subList = idList.subList(i, end);
            if (!service.removeByIds(subList)) {
                log.warn("Batch remove failed at index: {}, size: {}", i, subList.size());
                return false;
            }
        }
        
        return true;
    }

    /**
     * 批量查询数据（按ID列表）
     * 使用配置的查询操作批次大小
     *
     * @param service Service 实例
     * @param idList  ID列表
     * @param <T>     实体类型
     * @return 查询结果列表
     */
    public <T> List<T> listByIds(IService<T> service, List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return List.of();
        }
        
        int batchSize = getBatchSize(idList.size(), OperationType.QUERY);
        return listByIds(service, idList, batchSize);
    }

    /**
     * 批量查询数据（按ID列表，指定批次大小）
     *
     * @param service   Service 实例
     * @param idList    ID列表
     * @param batchSize 批次大小（会被限制在配置的最大/最小值之间）
     * @param <T>       实体类型
     * @return 查询结果列表
     */
    public <T> List<T> listByIds(IService<T> service, List<Long> idList, int batchSize) {
        if (idList == null || idList.isEmpty()) {
            return List.of();
        }
        
        int actualBatchSize = constrainBatchSize(batchSize);
        log.debug("Batch query by ids with size: {}, data count: {}", actualBatchSize, idList.size());
        
        List<T> resultList = new ArrayList<>();
        int totalSize = idList.size();
        
        for (int i = 0; i < totalSize; i += actualBatchSize) {
            int end = Math.min(i + actualBatchSize, totalSize);
            List<Long> subList = idList.subList(i, end);
            List<T> batchResult = service.listByIds(subList);
            if (batchResult != null && !batchResult.isEmpty()) {
                resultList.addAll(batchResult);
            }
        }
        
        return resultList;
    }

    /**
     * 批量逻辑删除数据（按ID列表）
     * 使用配置的删除操作批次大小，只更新逻辑删除字段
     *
     * @param service Service 实例
     * @param idList  ID列表
     * @param <T>     实体类型
     * @return 是否全部删除成功
     */
    public <T> boolean removeByIdsLogic(IService<T> service, List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return true;
        }
        
        int batchSize = getBatchSize(idList.size(), OperationType.DELETE);
        return removeByIdsLogic(service, idList, batchSize);
    }

    /**
     * 批量逻辑删除数据（按ID列表，指定批次大小）
     * 只更新逻辑删除字段，不会真正删除数据
     *
     * @param service    Service 实例
     * @param idList     ID列表
     * @param batchSize  批次大小（会被限制在配置的最大/最小值之间）
     * @param <T>        实体类型
     * @return 是否全部删除成功
     */
    public <T> boolean removeByIdsLogic(IService<T> service, List<Long> idList, int batchSize) {
        if (idList == null || idList.isEmpty()) {
            return true;
        }
        
        int actualBatchSize = constrainBatchSize(batchSize);
        log.debug("Batch logic remove by ids with size: {}, data count: {}", actualBatchSize, idList.size());
        
        int totalSize = idList.size();
        for (int i = 0; i < totalSize; i += actualBatchSize) {
            int end = Math.min(i + actualBatchSize, totalSize);
            List<Long> subList = idList.subList(i, end);
            if (!service.removeByIds(subList)) {
                log.warn("Batch logic remove failed at index: {}, size: {}", i, subList.size());
                return false;
            }
        }
        
        return true;
    }

    /**
     * 批量物理删除数据（按ID列表）
     * 使用配置的删除操作批次大小，直接从数据库删除数据
     *
     * @param service Service 实例
     * @param idList  ID列表
     * @param <T>     实体类型
     * @return 是否全部删除成功
     */
    public <T> boolean removeByIdsPhysical(IService<T> service, List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return true;
        }
        
        int batchSize = getBatchSize(idList.size(), OperationType.DELETE);
        return removeByIdsPhysical(service, idList, batchSize);
    }

    /**
     * 批量物理删除数据（按ID列表，指定批次大小）
     * 直接从数据库删除数据，不可恢复，请谨慎使用
     *
     * @param service    Service 实例
     * @param idList     ID列表
     * @param batchSize  批次大小（会被限制在配置的最大/最小值之间）
     * @param <T>        实体类型
     * @return 是否全部删除成功
     */
    public <T> boolean removeByIdsPhysical(IService<T> service, List<Long> idList, int batchSize) {
        if (idList == null || idList.isEmpty()) {
            return true;
        }
        
        int actualBatchSize = constrainBatchSize(batchSize);
        log.debug("Batch physical remove by ids with size: {}, data count: {}", actualBatchSize, idList.size());
        
        int totalSize = idList.size();
        for (int i = 0; i < totalSize; i += actualBatchSize) {
            int end = Math.min(i + actualBatchSize, totalSize);
            List<Long> subList = idList.subList(i, end);
            // 使用 MyBatis-Plus 的 removeByIds 方法，在配置了逻辑删除时需要特殊处理
            // 这里直接使用 baseMapper 的 deleteBatchIds 来绕过逻辑删除
            boolean success = service.getBaseMapper().deleteBatchIds(subList) > 0;
            if (!success) {
                log.warn("Batch physical remove failed at index: {}, size: {}", i, subList.size());
                return false;
            }
        }
        
        return true;
    }

    /**
     * 根据数据量获取合适的批次大小（使用默认操作类型）
     *
     * @param dataSize 数据量
     * @return 批次大小
     */
    public int getBatchSize(int dataSize) {
        return getBatchSize(dataSize, OperationType.SAVE);
    }

    /**
     * 根据数据量和操作类型获取合适的批次大小
     * 支持为不同操作类型配置独立的批次大小
     *
     * @param dataSize       数据量
     * @param operationType  操作类型
     * @return 批次大小
     */
    public int getBatchSize(int dataSize, OperationType operationType) {
        MyBatisProperties.Batch batchConfig = myBatisProperties.getBatch();
        
        if (!batchConfig.isEnabled()) {
            return dataSize;
        }
        
        // 获取操作类型对应的批次大小配置
        int defaultSize = getOperationBatchSize(batchConfig, operationType);
        
        // 如果数据量小于默认批次大小，使用数据量作为批次大小
        if (dataSize <= defaultSize) {
            return constrainBatchSize(dataSize);
        }
        
        return constrainBatchSize(defaultSize);
    }

    /**
     * 获取指定操作类型的批次大小配置
     * 如果操作类型未启用独立配置，则使用全局默认配置
     *
     * @param batchConfig    批量配置
     * @param operationType  操作类型
     * @return 批次大小
     */
    private int getOperationBatchSize(MyBatisProperties.Batch batchConfig, OperationType operationType) {
        MyBatisProperties.OperationBatch operationBatch = null;
        
        switch (operationType) {
            case SAVE:
                operationBatch = batchConfig.getSave();
                break;
            case UPDATE:
                operationBatch = batchConfig.getUpdate();
                break;
            case QUERY:
                operationBatch = batchConfig.getQuery();
                break;
            case DELETE:
                operationBatch = batchConfig.getDelete();
                break;
        }
        
        // 如果操作类型配置未启用或为null，使用全局默认配置
        if (operationBatch == null || !operationBatch.isEnabled()) {
            return batchConfig.getDefaultSize();
        }
        
        return operationBatch.getSize();
    }

    /**
     * 将批次大小限制在配置的范围内
     *
     * @param batchSize 输入的批次大小
     * @return 受限后的批次大小
     */
    private int constrainBatchSize(int batchSize) {
        MyBatisProperties.Batch batchConfig = myBatisProperties.getBatch();
        
        int maxSize = batchConfig.getMaxSize();
        int minSize = batchConfig.getMinSize();
        
        return Math.max(minSize, Math.min(batchSize, maxSize));
    }

    /**
     * 获取当前配置的默认批次大小
     *
     * @return 默认批次大小
     */
    public int getDefaultBatchSize() {
        return myBatisProperties.getBatch().getDefaultSize();
    }

    /**
     * 获取当前配置的最大批次大小
     *
     * @return 最大批次大小
     */
    public int getMaxBatchSize() {
        return myBatisProperties.getBatch().getMaxSize();
    }

    /**
     * 获取当前配置的最小批次大小
     *
     * @return 最小批次大小
     */
    public int getMinBatchSize() {
        return myBatisProperties.getBatch().getMinSize();
    }
}