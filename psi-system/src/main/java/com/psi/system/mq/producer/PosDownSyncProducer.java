package com.psi.system.mq.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MessageFactory;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.util.IdUtils;
import com.psi.system.entity.PosConfig;
import com.psi.system.entity.PosOperator;
import com.psi.common.dto.sync.SyncBatchDTO;
import com.psi.common.dto.sync.SyncDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * POS下行同步MQ生产者
 * 后台管理端数据变更时，发送MQ消息触发下行同步到POS机
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PosDownSyncProducer {

    private final MqMessageFacade mqMessageFacade;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 发送收银机配置变更消息
     */
    public void sendPosConfig(PosConfig entity) {
        try {
            SyncBatchDTO batchDTO = buildBatchDTO(entity.getTenantId(), entity.getShopCode());
            SyncDataDTO dataDTO = buildSyncData(entity, "pos_config", entity.getTenantId(), entity.getShopCode());
            batchDTO.setDataList(List.of(dataDTO));

            sendSyncMessage(batchDTO, RabbitMQConstant.SYNC_DOWN_POS_CONFIG_ROUTING_KEY);
            log.info("收银机配置下行同步消息已发送: posSn={}, dataUuid={}", entity.getPosSn(), entity.getDataUuid());
        } catch (Exception e) {
            log.error("收银机配置下行同步消息发送失败: posSn={}", entity.getPosSn(), e);
        }
    }

    /**
     * 发送收银员变更消息
     */
    public void sendPosOperator(PosOperator entity) {
        try {
            SyncBatchDTO batchDTO = buildBatchDTO(entity.getTenantId(), entity.getShopCode());
            SyncDataDTO dataDTO = buildSyncData(entity, "pos_operator", entity.getTenantId(), entity.getShopCode());
            batchDTO.setDataList(List.of(dataDTO));

            sendSyncMessage(batchDTO, RabbitMQConstant.SYNC_DOWN_POS_OPERATOR_ROUTING_KEY);
            log.info("收银员下行同步消息已发送: username={}, dataUuid={}", entity.getUsername(), entity.getDataUuid());
        } catch (Exception e) {
            log.error("收银员下行同步消息发送失败: username={}", entity.getUsername(), e);
        }
    }

    /**
     * 构建批量DTO
     */
    private SyncBatchDTO buildBatchDTO(Long tenantId, String shopCode) {
        SyncBatchDTO batchDTO = new SyncBatchDTO();
        batchDTO.setBatchUuid(IdUtils.snowflakeIdStr());
        batchDTO.setTenantId(String.valueOf(tenantId));
        batchDTO.setShopCode(shopCode);
        batchDTO.setCreateTime(LocalDateTime.now().format(TIME_FORMATTER));
        return batchDTO;
    }

    /**
     * 构建单条同步数据DTO
     */
    private SyncDataDTO buildSyncData(Object entity, String tableName, Long tenantId, String shopCode) throws Exception {
        SyncDataDTO dataDTO = new SyncDataDTO();
        dataDTO.setRecordId(IdUtils.snowflakeIdStr());
        dataDTO.setTableName(tableName);
        dataDTO.setJsonData(objectMapper.writeValueAsString(entity));
        dataDTO.setTenantId(String.valueOf(tenantId));
        dataDTO.setShopCode(shopCode);
        dataDTO.setCreateTime(LocalDateTime.now().format(TIME_FORMATTER));
        return dataDTO;
    }

    /**
     * 发送MQ消息
     */
    private void sendSyncMessage(SyncBatchDTO batchDTO, String routingKey) {
        MqCommonMessage<SyncBatchDTO> message = MessageFactory.create(
                batchDTO,
                RabbitMQConstant.SYNC_DOWN_EXCHANGE,
                routingKey,
                "POS_DOWN_SYNC"
        );

        Map<String, String> extParams = new HashMap<>();
        extParams.put("batchUuid", batchDTO.getBatchUuid());
        extParams.put("dataCount", String.valueOf(batchDTO.getDataList().size()));
        message.setExtParams(extParams);

        mqMessageFacade.sendAsync(message);
    }
}