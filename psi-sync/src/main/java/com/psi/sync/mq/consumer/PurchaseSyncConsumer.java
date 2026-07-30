package com.psi.sync.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.dto.sync.SyncBatchDTO;
import com.psi.common.dto.sync.SyncDataDTO;
import com.psi.common.message.MqCommonMessage;
import com.psi.sync.entity.UpSyncEntity;
import com.psi.sync.service.UpSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 采购上行同步数据消费者
 *
 * <p>监听采购微服务发送的上行同步数据，存储到 up_sync 表等待下游处理。</p>
 * <p>TODO：收银端相关表未创建，目前只保存到 up_sync 中间库，后续按需解析到业务表。</p>
 */
@Slf4j
@Component
public class PurchaseSyncConsumer {

    private final UpSyncService upSyncService;
    private final ObjectMapper objectMapper;

    private final Set<String> processingRecords = ConcurrentHashMap.newKeySet();
    private static final int MAX_CACHE_SIZE = 10000;

    public PurchaseSyncConsumer(UpSyncService upSyncService, ObjectMapper objectMapper) {
        this.upSyncService = upSyncService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConstant.SYNC_UP_PURCHASE_QUEUE)
    public void handleSyncMessage(MqCommonMessage<?> message) {
        SyncBatchDTO batchDTO;
        if (message.getData() instanceof SyncBatchDTO) {
            batchDTO = (SyncBatchDTO) message.getData();
        } else {
            batchDTO = objectMapper.convertValue(message.getData(), SyncBatchDTO.class);
        }

        String batchUuid = batchDTO != null ? batchDTO.getBatchUuid() : null;
        log.info("收到采购同步消息: batchUuid={}, tenantId={}, dataCount={}",
                batchUuid,
                batchDTO != null ? batchDTO.getTenantId() : null,
                batchDTO != null && batchDTO.getDataList() != null ? batchDTO.getDataList().size() : 0);

        try {
            if (batchDTO == null || batchDTO.getDataList() == null || batchDTO.getDataList().isEmpty()) {
                log.warn("采购同步消息数据为空: batchUuid={}", batchUuid);
                return;
            }

            List<SyncDataDTO> deduplicatedList = deduplicateByRecordId(batchDTO.getDataList());
            ProcessResult result = processBatch(batchDTO, deduplicatedList);

            log.info("采购批次处理完成: batchUuid={}, 成功={}, 失败={}, 跳过={}",
                    batchUuid, result.successCount, result.failCount, result.skipCount);

            if (result.failCount > 0 && (double) result.failCount / deduplicatedList.size() > 0.3) {
                log.error("采购批次处理失败率过高: batchUuid={}, failRate={}%",
                        batchUuid, (double) result.failCount / deduplicatedList.size() * 100);
            }

        } catch (Exception e) {
            log.error("处理采购同步批次消息异常: batchUuid={}, error={}", batchUuid, e.getMessage(), e);
            throw e;
        }
    }

    private ProcessResult processBatch(SyncBatchDTO batchDTO, List<SyncDataDTO> dataList) {
        ProcessResult result = new ProcessResult();

        for (SyncDataDTO dataDTO : dataList) {
            String recordId = dataDTO.getRecordId();

            try {
                if (!tryAcquireRecord(recordId)) {
                    result.skipCount++;
                    log.debug("采购记录已处理或正在处理: recordId={}", recordId);
                    continue;
                }

                try {
                    boolean success = processSyncData(dataDTO, batchDTO);
                    if (success) {
                        result.successCount++;
                    } else {
                        result.failCount++;
                        log.warn("采购处理失败: recordId={}", recordId);
                    }
                } finally {
                    releaseRecord(recordId);
                }

            } catch (Exception e) {
                result.failCount++;
                releaseRecord(recordId);
                log.error("采购同步数据写入失败: recordId={}, tableName={}, error={}",
                        recordId, dataDTO.getTableName(), e.getMessage(), e);
            }
        }

        return result;
    }

    private boolean tryAcquireRecord(String recordId) {
        if (recordId == null || recordId.isEmpty()) {
            return true;
        }
        if (!processingRecords.add(recordId)) {
            return false;
        }
        if (upSyncService.existsByRecordId(recordId)) {
            processingRecords.remove(recordId);
            return false;
        }
        if (processingRecords.size() > MAX_CACHE_SIZE) {
            processingRecords.clear();
        }
        return true;
    }

    private void releaseRecord(String recordId) {
        if (recordId != null && !recordId.isEmpty()) {
            processingRecords.remove(recordId);
        }
    }

    private boolean processSyncData(SyncDataDTO dataDTO, SyncBatchDTO batchDTO) {
        try {
            UpSyncEntity entity = new UpSyncEntity();
            entity.setBatchUuid(dataDTO.getBatchUuid() != null ? dataDTO.getBatchUuid() : batchDTO.getBatchUuid());
            entity.setTenantId(dataDTO.getTenantId() != null ? dataDTO.getTenantId() : batchDTO.getTenantId());
            entity.setShopCode(dataDTO.getShopCode() != null ? dataDTO.getShopCode() : batchDTO.getShopCode());
            entity.setTableName(dataDTO.getTableName());
            entity.setJsonData(dataDTO.getJsonData());
            entity.setSyncStatus(0);
            entity.setProcessTime(null);
            entity.setErrorMsg(null);
            entity.setRetryCount(0);

            return upSyncService.insertWithRecordId(entity, dataDTO.getRecordId());
        } catch (Exception e) {
            log.error("采购数据插入 up_sync 失败: recordId={}, tableName={}, error={}",
                    dataDTO.getRecordId(), dataDTO.getTableName(), e.getMessage());
            return false;
        }
    }

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

    private static class ProcessResult {
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;
    }
}
