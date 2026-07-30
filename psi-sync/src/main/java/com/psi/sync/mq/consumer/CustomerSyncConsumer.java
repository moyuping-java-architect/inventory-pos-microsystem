package com.psi.sync.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.dto.sync.SyncBatchDTO;
import com.psi.common.dto.sync.SyncDataDTO;
import com.psi.sync.service.UpSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 客户同步数据消费者
 *
 * 监听销售微服务发送的客户同步数据，存储到 up_sync 表等待下游处理
 * 基于 recordId 实现幂等性消费
 */
@Slf4j
@Component
public class CustomerSyncConsumer {

    private final UpSyncService upSyncService;
    private final ObjectMapper objectMapper;

    private final Set<String> processingRecords = ConcurrentHashMap.newKeySet();
    private static final int MAX_CACHE_SIZE = 10000;

    public CustomerSyncConsumer(UpSyncService upSyncService, ObjectMapper objectMapper) {
        this.upSyncService = upSyncService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConstant.SYNC_UP_CUSTOMER_QUEUE)
    public void handleSyncMessage(MqCommonMessage<?> message) {
        SyncBatchDTO batchDTO;
        if (message.getData() instanceof SyncBatchDTO) {
            batchDTO = (SyncBatchDTO) message.getData();
        } else {
            batchDTO = objectMapper.convertValue(message.getData(), SyncBatchDTO.class);
        }

        String batchUuid = batchDTO != null ? batchDTO.getBatchUuid() : null;
        log.info("收到客户同步消息: batchUuid={}, tenantId={}, dataCount={}",
                batchUuid,
                batchDTO != null ? batchDTO.getTenantId() : null,
                batchDTO != null && batchDTO.getDataList() != null ? batchDTO.getDataList().size() : 0);

        try {
            if (batchDTO == null || batchDTO.getDataList() == null || batchDTO.getDataList().isEmpty()) {
                log.warn("客户同步消息数据为空: batchUuid={}", batchUuid);
                return;
            }

            List<SyncDataDTO> dataList = batchDTO.getDataList();

            // 去重
            List<SyncDataDTO> deduplicatedList = deduplicateByRecordId(dataList);

            SuccessFailResult result = processBatch(batchDTO, deduplicatedList);

            log.info("客户批次处理完成: batchUuid={}, 成功={}, 失败={}, 跳过={}",
                    batchUuid, result.successCount, result.failCount, result.skipCount);

            if (result.failCount > 0 && (double) result.failCount / deduplicatedList.size() > 0.3) {
                log.error("客户批次处理失败率过高: batchUuid={}, failRate={}%",
                        batchUuid, (double) result.failCount / deduplicatedList.size() * 100);
            }

        } catch (Exception e) {
            log.error("处理客户同步批次消息异常: batchUuid={}, error={}", batchUuid, e.getMessage(), e);
            throw e;
        }
    }

    private SuccessFailResult processBatch(SyncBatchDTO batchDTO, List<SyncDataDTO> dataList) {
        SuccessFailResult result = new SuccessFailResult();

        for (SyncDataDTO dataDTO : dataList) {
            String recordId = dataDTO.getRecordId();

            try {
                if (recordId != null && !processingRecords.add(recordId)) {
                    result.skipCount++;
                    continue;
                }

                boolean success = processSyncData(dataDTO, batchDTO);

                if (success) {
                    result.successCount++;
                } else {
                    result.failCount++;
                }

            } catch (Exception e) {
                log.error("客户同步数据写入失败: recordId={}, tableName={}, error={}",
                        recordId, dataDTO.getTableName(), e.getMessage());
                result.failCount++;
            }
        }

        return result;
    }

    private boolean processSyncData(SyncDataDTO dataDTO, SyncBatchDTO batchDTO) {
        com.psi.sync.entity.UpSyncEntity entity = new com.psi.sync.entity.UpSyncEntity();
        entity.setBatchUuid(dataDTO.getBatchUuid() != null ? dataDTO.getBatchUuid() : batchDTO.getBatchUuid());
        entity.setTenantId(dataDTO.getTenantId() != null ? dataDTO.getTenantId() : batchDTO.getTenantId());
        entity.setShopCode(dataDTO.getShopCode());
        entity.setTableName(dataDTO.getTableName());
        entity.setJsonData(dataDTO.getJsonData());

        return upSyncService.insert(entity);
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

    private static class SuccessFailResult {
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;
    }
}