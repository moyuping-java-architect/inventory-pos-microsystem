package com.psi.sync.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.dto.sync.SyncBatchDTO;
import com.psi.common.dto.sync.SyncDataDTO;
import com.psi.sync.entity.UpSyncEntity;
import com.psi.sync.service.UpSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 商品同步数据消费者
 * 
 * 监听商品微服务发送的上行同步数据，存储到 up_sync 表等待下游处理
 * 基于 recordId 实现幂等性消费
 */
@Slf4j
@Component
public class GoodsSyncConsumer {

    private final UpSyncService upSyncService;
    private final ObjectMapper objectMapper;
    
    // 内存去重缓存（用于快速判断重复消息）
    private final Set<String> processingRecords = ConcurrentHashMap.newKeySet();
    // 去重缓存最大容量
    private static final int MAX_CACHE_SIZE = 10000;

    public GoodsSyncConsumer(UpSyncService upSyncService, ObjectMapper objectMapper) {
        this.upSyncService = upSyncService;
        this.objectMapper = objectMapper;
    }

    /**
     * 监听商品上行同步队列
     * 使用 recordId 实现幂等性消费
     */
    @RabbitListener(queues = RabbitMQConstant.SYNC_UP_GOODS_QUEUE)
    public void handleSyncMessage(MqCommonMessage<?> message) {
        // 由于Java泛型擦除，data字段可能被反序列化为LinkedHashMap，需要手动转换
        SyncBatchDTO batchDTO;
        if (message.getData() instanceof SyncBatchDTO) {
            batchDTO = (SyncBatchDTO) message.getData();
        } else {
            batchDTO = objectMapper.convertValue(message.getData(), SyncBatchDTO.class);
        }
        
        String batchUuid = batchDTO != null ? batchDTO.getBatchUuid() : null;
        log.info("收到商品同步消息: batchUuid={}, tenantId={}, dataCount={}",
                batchUuid,
                batchDTO != null ? batchDTO.getTenantId() : null,
                batchDTO != null && batchDTO.getDataList() != null ? batchDTO.getDataList().size() : 0);

        try {
            if (batchDTO == null) {
                log.warn("商品同步消息数据为空: messageId={}", message.getMessageId());
                return;
            }
            List<SyncDataDTO> dataList = batchDTO.getDataList();
            if (dataList == null || dataList.isEmpty()) {
                log.warn("商品同步数据列表为空: batchUuid={}", batchUuid);
                return;
            }

            // 1. 内存去重（防止同一批次内重复）
            List<SyncDataDTO> deduplicatedList = deduplicateByRecordId(dataList);
            log.debug("内存去重完成: batchUuid={}, before={}, after={}",
                    batchUuid, dataList.size(), deduplicatedList.size());

            // 2. 批次处理
            ProcessResult result = processBatch(batchDTO, deduplicatedList);

            // 3. 记录处理结果
            log.info("商品批次处理完成: batchUuid={}, 成功={}, 失败={}, 跳过={}",
                    batchUuid, result.successCount, result.failCount, result.skipCount);

            // 4. 如果失败率过高，触发告警
            if (result.failCount > 0 && (double) result.failCount / deduplicatedList.size() > 0.3) {
                log.error("商品批次处理失败率过高: batchUuid={}, failRate={}%",
                        batchUuid, (double) result.failCount / deduplicatedList.size() * 100);
            }

        } catch (Exception e) {
            log.error("处理商品同步批次消息异常: batchUuid={}, error={}", batchUuid, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 处理批次数据
     */
    private ProcessResult processBatch(SyncBatchDTO batchDTO, List<SyncDataDTO> dataList) {
        ProcessResult result = new ProcessResult();
        
        for (SyncDataDTO dataDTO : dataList) {
            String recordId = dataDTO.getRecordId();
            
            try {
                // 幂等性校验：先检查内存缓存，再检查数据库
                if (!tryAcquireRecord(recordId)) {
                    result.skipCount++;
                    log.debug("商品记录已处理或正在处理: recordId={}", recordId);
                    continue;
                }

                try {
                    boolean success = processSyncData(dataDTO, batchDTO);
                    
                    if (success) {
                        result.successCount++;
                        log.debug("商品处理成功: recordId={}, businessKey={}, tableName={}",
                                recordId, dataDTO.getBusinessKey(), dataDTO.getTableName());
                    } else {
                        result.failCount++;
                        log.warn("商品处理失败: recordId={}", recordId);
                    }
                } finally {
                    releaseRecord(recordId);
                }
                
            } catch (Exception e) {
                result.failCount++;
                releaseRecord(recordId);
                log.error("商品处理单条数据异常: recordId={}, error={}", recordId, e.getMessage(), e);
            }
        }
        
        return result;
    }

    /**
     * 尝试获取记录处理权（幂等性校验）
     */
    private boolean tryAcquireRecord(String recordId) {
        if (recordId == null || recordId.isEmpty()) {
            return true;
        }

        // 1. 检查内存缓存
        if (!processingRecords.add(recordId)) {
            return false;
        }

        // 2. 检查数据库是否已存在
        if (upSyncService.existsByRecordId(recordId)) {
            processingRecords.remove(recordId);
            return false;
        }

        // 3. 清理过期缓存（防止内存溢出）
        if (processingRecords.size() > MAX_CACHE_SIZE) {
            processingRecords.clear();
        }

        return true;
    }

    /**
     * 释放记录处理权
     */
    private void releaseRecord(String recordId) {
        if (recordId != null && !recordId.isEmpty()) {
            processingRecords.remove(recordId);
        }
    }

    /**
     * 处理单条同步数据
     */
    private boolean processSyncData(SyncDataDTO dataDTO, SyncBatchDTO batchDTO) {
        try {
            UpSyncEntity entity = new UpSyncEntity();
            entity.setBatchUuid(dataDTO.getBatchUuid() != null ? dataDTO.getBatchUuid() : batchDTO.getBatchUuid());
            entity.setTenantId(dataDTO.getTenantId() != null ? dataDTO.getTenantId() : batchDTO.getTenantId());
            entity.setShopCode(dataDTO.getShopCode() != null ? dataDTO.getShopCode() : batchDTO.getShopCode());
            entity.setTableName(dataDTO.getTableName());
            entity.setJsonData(dataDTO.getJsonData());
            entity.setSyncStatus(0); // 0: 待处理
            entity.setProcessTime(null);
            entity.setErrorMsg(null);
            entity.setRetryCount(0);
            
            // 插入数据库（使用recordId作为唯一约束）
            return upSyncService.insertWithRecordId(entity, dataDTO.getRecordId());
            
        } catch (Exception e) {
            log.error("商品数据插入数据库失败: recordId={}, tableName={}, error={}",
                    dataDTO.getRecordId(), dataDTO.getTableName(), e.getMessage());
            return false;
        }
    }

    /**
     * 基于 recordId 去重
     */
    private List<SyncDataDTO> deduplicateByRecordId(List<SyncDataDTO> dataList) {
        Set<String> seen = ConcurrentHashMap.newKeySet();
        return dataList.stream()
                .filter(dto -> {
                    String recordId = dto.getRecordId();
                    if (recordId == null || recordId.isEmpty()) {
                        return true;
                    }
                    return seen.add(recordId);
                })
                .collect(Collectors.toList());
    }

    /**
     * 处理结果内部类
     */
    private static class ProcessResult {
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;
    }
}