package com.psi.sync.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.dto.sync.SyncBatchDTO;
import com.psi.common.dto.sync.SyncDataDTO;
import com.psi.sync.entity.DownSyncEntity;
import com.psi.sync.service.DownSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * POS下行同步数据消费者
 * 接收后台系统发送的下行同步数据（收银机配置、收银员等），写入 down_sync 表供 POS 拉取
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PosDownSyncConsumer {

    private final DownSyncService downSyncService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConstant.SYNC_DOWN_QUEUE)
    public void handleDownSyncMessage(MqCommonMessage<?> message, Message amqpMessage, Channel channel) {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();

        try {
            SyncBatchDTO batchDTO;
            if (message.getData() instanceof SyncBatchDTO) {
                batchDTO = (SyncBatchDTO) message.getData();
            } else {
                batchDTO = objectMapper.convertValue(message.getData(), SyncBatchDTO.class);
            }

            String batchUuid = batchDTO != null ? batchDTO.getBatchUuid() : null;
            log.info("收到下行同步消息: batchUuid={}, dataCount={}",
                    batchUuid,
                    batchDTO != null && batchDTO.getDataList() != null ? batchDTO.getDataList().size() : 0);

            if (batchDTO == null || batchDTO.getDataList() == null || batchDTO.getDataList().isEmpty()) {
                log.warn("下行同步消息数据为空: batchUuid={}", batchUuid);
                channel.basicAck(deliveryTag, false);
                return;
            }

            List<SyncDataDTO> dataList = batchDTO.getDataList();
            for (SyncDataDTO dataDTO : dataList) {
                processDownSyncData(dataDTO, batchDTO);
            }

            log.info("下行同步处理完成: batchUuid={}, count={}", batchUuid, dataList.size());
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("下行同步处理失败: error={}", e.getMessage(), e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioEx) {
                log.error("NACK 失败: {}", ioEx.getMessage());
            }
        }
    }

    /**
     * 处理单条下行同步数据
     */
    private void processDownSyncData(SyncDataDTO dataDTO, SyncBatchDTO batchDTO) {
        DownSyncEntity entity = new DownSyncEntity();
        entity.setBatchUuid(dataDTO.getBatchUuid() != null ? dataDTO.getBatchUuid() : batchDTO.getBatchUuid());
        entity.setTenantId(dataDTO.getTenantId() != null ? dataDTO.getTenantId() : batchDTO.getTenantId());
        entity.setShopCode(dataDTO.getShopCode() != null ? dataDTO.getShopCode() : batchDTO.getShopCode());
        entity.setTableName(dataDTO.getTableName());
        entity.setDataUuid(dataDTO.getDataUuid());
        entity.setDataVersion(dataDTO.getDataVersion() != null ? dataDTO.getDataVersion() : 0L);
        entity.setJsonData(dataDTO.getJsonData());

        boolean result = downSyncService.insert(entity);
        if (result) {
            log.debug("下行同步数据写入成功: tableName={}, batchUuid={}", dataDTO.getTableName(), entity.getBatchUuid());
        } else {
            log.warn("下行同步数据写入失败: tableName={}, batchUuid={}", dataDTO.getTableName(), entity.getBatchUuid());
        }
    }
}