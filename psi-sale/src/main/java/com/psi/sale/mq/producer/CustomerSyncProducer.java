package com.psi.sale.mq.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.context.VirtualThreadContextWrapper;
import com.psi.common.message.MessageFactory;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.util.IdUtils;
import com.psi.common.dto.sync.SyncBatchDTO;
import com.psi.common.dto.sync.SyncDataDTO;
import com.psi.sale.entity.CustomerEntity;
import com.psi.sale.entity.CustomerSyncLogEntity;
import com.psi.sale.mapper.CustomerMapper;
import com.psi.sale.mapper.CustomerSyncLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 客户微服务MQ生产者
 * 参照GoodsSyncProducer实现客户数据上传到sync-ms
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerSyncProducer {

    private final MqMessageFacade mqMessageFacade;
    private final ObjectMapper objectMapper;

    private final CustomerMapper customerMapper;
    private final CustomerSyncLogMapper syncLogMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int BATCH_SIZE = 1000;
    private static final int MAX_PAGES = 1000;

    // ==================== 批量全量同步 ====================

    /**
     * 异步同步所有客户数据到sync-ms
     * 从sync_log表获取上次上传时间，查询update_time大于该时间的所有数据
     */
    public void syncAllAsync() {
        VirtualThreadContextWrapper.executeAsync(() -> {
            try {
                String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
                String lastUploadTime = getLastUploadTime();

                log.info("客户数据同步开始: lastUploadTime={}", lastUploadTime);

                syncCustomersByPage(lastUploadTime, currentTime);

                updateLastUploadTime(currentTime);

                log.info("客户数据同步全部完成");
            } catch (Exception e) {
                log.error("客户数据同步消息发送失败", e);
            }
        });
    }

    // ==================== 实时单条同步 ====================

    /**
     * 实时发送单条客户变更到MQ
     * 在save/update/delete/statusChange时调用
     */
    public void sendCustomer(CustomerEntity entity) {
        try {
            String batchUuid = IdUtils.snowflakeIdStr();
            String currentTime = LocalDateTime.now().format(TIME_FORMATTER);

            SyncDataDTO dataDTO = buildSyncData(batchUuid, String.valueOf(entity.getTenantId()), null,
                    "customer", entity);

            SyncBatchDTO batchDTO = new SyncBatchDTO();
            batchDTO.setBatchUuid(batchUuid);
            batchDTO.setTenantId(String.valueOf(entity.getTenantId()));
            batchDTO.setCreateTime(currentTime);
            batchDTO.setDataList(List.of(dataDTO));

            sendSyncMessage(batchDTO, RabbitMQConstant.SYNC_UP_CUSTOMER_ROUTING_KEY);
            log.info("客户实时同步消息已发送: id={}, customerCode={}, dataUuid={}",
                    entity.getId(), entity.getCustomerCode(), entity.getDataUuid());
        } catch (Exception e) {
            log.error("客户实时同步消息发送失败: id={}, customerCode={}",
                    entity.getId(), entity.getCustomerCode(), e);
        }
    }

    // ==================== 私有方法 ====================

    private String getLastUploadTime() {
        CustomerSyncLogEntity logEntity = syncLogMapper.selectByType("up");
        if (logEntity != null) {
            return logEntity.getLastTime();
        }
        return "2024-01-01 00:00:00";
    }

    private void updateLastUploadTime(String lastTime) {
        CustomerSyncLogEntity logEntity = syncLogMapper.selectByType("up");
        if (logEntity != null) {
            syncLogMapper.updateLastTime("up", lastTime);
        } else {
            logEntity = new CustomerSyncLogEntity();
            logEntity.setType("up");
            logEntity.setLastTime(lastTime);
            syncLogMapper.insert(logEntity);
        }
    }

    private void syncCustomersByPage(String lastUploadTime, String currentTime) {
        int pageNum = 1;
        boolean hasMoreData = true;

        while (hasMoreData && pageNum <= MAX_PAGES) {
            int offset = (pageNum - 1) * BATCH_SIZE;

            try {
                List<CustomerEntity> customerList = customerMapper.selectByUpdateTimeAfterPage(
                        lastUploadTime, offset, BATCH_SIZE);

                if (customerList == null || customerList.isEmpty()) {
                    log.info("客户数据同步完成，共处理 {} 页", pageNum - 1);
                    hasMoreData = false;
                    continue;
                }

                String batchUuid = IdUtils.snowflakeIdStr();
                List<SyncDataDTO> dataList = new ArrayList<>();

                for (CustomerEntity customer : customerList) {
                    dataList.add(buildSyncData(batchUuid, String.valueOf(customer.getTenantId()), null,
                            "customer", customer));
                }

                sendBatchData(batchUuid, dataList, currentTime);
                log.info("客户数据同步: page={}, count={}", pageNum, dataList.size());

            } catch (Exception e) {
                log.error("客户数据同步失败: page={}", pageNum, e);
            }

            pageNum++;
        }

        if (pageNum > MAX_PAGES) {
            log.warn("客户数据同步达到最大页数限制: {}", MAX_PAGES);
        }
    }

    private SyncDataDTO buildSyncData(String batchUuid, String tenantId, String shopCode,
                                       String tableName, Object entity) throws Exception {
        SyncDataDTO dataDTO = new SyncDataDTO();
        dataDTO.setRecordId(IdUtils.snowflakeIdStr());
        dataDTO.setBatchUuid(batchUuid);
        dataDTO.setTenantId(tenantId);
        dataDTO.setShopCode(shopCode);
        dataDTO.setTableName(tableName);
        dataDTO.setJsonData(objectMapper.writeValueAsString(entity));
        dataDTO.setCreateTime(LocalDateTime.now().format(TIME_FORMATTER));
        return dataDTO;
    }

    private void sendBatchData(String batchUuid, List<SyncDataDTO> dataList, String currentTime) {
        try {
            SyncBatchDTO batchDTO = new SyncBatchDTO();
            batchDTO.setBatchUuid(batchUuid);
            batchDTO.setCreateTime(currentTime);
            batchDTO.setDataList(dataList);

            if (!dataList.isEmpty()) {
                batchDTO.setTenantId(dataList.get(0).getTenantId());
            }

            sendSyncMessage(batchDTO, RabbitMQConstant.SYNC_UP_CUSTOMER_ROUTING_KEY);
        } catch (Exception e) {
            log.error("发送批次消息失败: batchUuid={}, error={}", batchUuid, e.getMessage(), e);
        }
    }

    private void sendSyncMessage(SyncBatchDTO batchDTO, String routingKey) throws JsonProcessingException {
        MqCommonMessage<SyncBatchDTO> message = MessageFactory.create(
                batchDTO,
                RabbitMQConstant.SYNC_UP_EXCHANGE,
                routingKey,
                "CUSTOMER_SYNC"
        );

        Map<String, String> extParams = new HashMap<>();
        extParams.put("batchUuid", batchDTO.getBatchUuid());
        extParams.put("dataCount", String.valueOf(batchDTO.getDataList().size()));
        message.setExtParams(extParams);

        mqMessageFacade.sendAsync(message);

        log.debug("客户同步消息发送成功: batchUuid={}, dataCount={}",
                batchDTO.getBatchUuid(), batchDTO.getDataList().size());
    }
}