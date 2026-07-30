package com.psi.sale.mq.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MessageFactory;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.util.IdUtils;
import com.psi.common.dto.sync.SyncBatchDTO;
import com.psi.common.dto.sync.SyncDataDTO;
import com.psi.sale.entity.CustomerEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerDownSyncProducer {

    private final MqMessageFacade mqMessageFacade;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void sendCustomer(CustomerEntity entity) {
        try {
            String batchUuid = IdUtils.snowflakeIdStr();
            String currentTime = LocalDateTime.now().format(TIME_FORMATTER);

            SyncDataDTO dataDTO = new SyncDataDTO();
            dataDTO.setRecordId(IdUtils.snowflakeIdStr());
            dataDTO.setBatchUuid(batchUuid);
            dataDTO.setTableName("customer");
            dataDTO.setJsonData(objectMapper.writeValueAsString(entity));
            dataDTO.setTenantId(String.valueOf(entity.getTenantId()));
            dataDTO.setShopCode(null);
            dataDTO.setCreateTime(currentTime);

            SyncBatchDTO batchDTO = new SyncBatchDTO();
            batchDTO.setBatchUuid(batchUuid);
            batchDTO.setTenantId(String.valueOf(entity.getTenantId()));
            batchDTO.setShopCode(null);
            batchDTO.setCreateTime(currentTime);
            batchDTO.setDataList(List.of(dataDTO));

            MqCommonMessage<SyncBatchDTO> message = MessageFactory.create(
                    batchDTO,
                    RabbitMQConstant.SYNC_DOWN_EXCHANGE,
                    RabbitMQConstant.SYNC_DOWN_ROUTING_KEY,
                    "CUSTOMER_DOWN_SYNC"
            );

            Map<String, String> extParams = new HashMap<>();
            extParams.put("batchUuid", batchUuid);
            extParams.put("dataCount", "1");
            extParams.put("tableName", "customer");
            message.setExtParams(extParams);

            mqMessageFacade.sendAsync(message);

            log.info("[sale] 客户下行同步消息已发送: id={}, customerCode={}, dataUuid={}",
                    entity.getId(), entity.getCustomerCode(), entity.getDataUuid());
        } catch (Exception e) {
            log.error("[sale] 客户下行同步消息发送失败: id={}, customerCode={}",
                    entity.getId(), entity.getCustomerCode(), e);
        }
    }
}
